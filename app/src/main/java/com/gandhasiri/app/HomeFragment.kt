package com.gandhasiri.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gandhasiri.app.databinding.FragmentHomeBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * HomeFragment: Final VTU Internship Submission working version.
 * Corrected UiSettings gestures, AlertDialog imports, and Map ID synchronization.
 */
class HomeFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var googleMap: GoogleMap? = null
    private var registeredTrees: List<Tree> = emptyList()
    private var currentValuation = 0.0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isMapReady = false

    private val SCENIC_FARM_LOCATION = LatLng(13.9299, 75.5681)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 2. LIFE-CYCLE INITIALIZATION: Fetch SupportMapFragment
        // ID fixed to home_map_container to match fragment_home.xml
        val mapFragment = childFragmentManager.findFragmentById(R.id.home_map_container) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        // Observe Room DB for Tree entries with explicit typing to assist compiler
        AppDatabase.getDatabase(requireContext()).treeDao().getAllTrees().observe(viewLifecycleOwner) { treesList: List<Tree>? ->
            treesList?.let {
                registeredTrees = it
                updateDashboardUI()
                if (isMapReady) {
                    updateMapMarkers()
                    centerMapOnEstate()
                }
            }
        }

        setupQuickLinks()
    }

    private fun setupQuickLinks() {
        binding.cvWealth.setOnClickListener { showWealthBreakdown() }
        binding.btnViewFullMap.setOnClickListener { findNavController().navigate(R.id.navigation_map) }
        binding.cardRegister.setOnClickListener { findNavController().navigate(R.id.navigation_register) }
        binding.cardPanic.setOnClickListener { findNavController().navigate(R.id.navigation_security) }
    }

    private fun updateDashboardUI() {
        binding.tvTreeCount.text = getString(R.string.trees_tagged_format, registeredTrees.size)
        
        currentValuation = 0.0
        var earliestPlanted = System.currentTimeMillis()

        for (tree in registeredTrees) {
            currentValuation += MaturityCalculator.calculateTreeValue(tree.girth)
            if (tree.datePlanted < earliestPlanted) earliestPlanted = tree.datePlanted
        }

        binding.tvTotalValuation.text = String.format(Locale.getDefault(), "₹ %,.2f", currentValuation)
        
        if (registeredTrees.isNotEmpty()) {
            val harvestDate = earliestPlanted + (20L * 365 * 24 * 60 * 60 * 1000)
            val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
            binding.tvHarvestEstimate.text = getString(R.string.harvest_estimate_format, sdf.format(Date(harvestDate)))
        } else {
            binding.tvHarvestEstimate.text = getString(R.string.harvest_estimate_format, "--")
        }

        renderGrowthAnalytics()
    }

    override fun onMapReady(map: GoogleMap) {
        this.googleMap = map
        this.isMapReady = true
        
        map.mapType = GoogleMap.MAP_TYPE_SATELLITE
        map.uiSettings.isMapToolbarEnabled = false
        // FIX 1: Correct function call instead of property assignment to resolve reference error
        map.uiSettings.setAllGesturesEnabled(false)
        
        centerMapOnEstate()
        updateMapMarkers()
    }

    private fun centerMapOnEstate() {
        val map = googleMap ?: return

        if (registeredTrees.isEmpty()) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    val target = if (location != null) LatLng(location.latitude, location.longitude) else SCENIC_FARM_LOCATION
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(target, 16f))
                }
            } else {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(SCENIC_FARM_LOCATION, 15f))
            }
        } else {
            val builder = LatLngBounds.Builder()
            for (tree in registeredTrees) {
                builder.include(LatLng(tree.latitude, tree.longitude))
            }
            
            try {
                val bounds = builder.build()
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50))
            } catch (e: Exception) {
                if (registeredTrees.isNotEmpty()) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(registeredTrees[0].latitude, registeredTrees[0].longitude), 17f))
                }
            }
        }
    }

    private fun updateMapMarkers() {
        val map = googleMap ?: return
        map.clear()
        if (registeredTrees.isEmpty()) return
        
        // 4. TREE MARKER LOOP: Drops orange markers for each asset
        for (tree in registeredTrees) {
            val treePos = LatLng(tree.latitude, tree.longitude)
            map.addMarker(MarkerOptions()
                .position(treePos)
                .title("Sandalwood: ${tree.treeId}")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)))
        }
    }

    private fun renderGrowthAnalytics() {
        if (_binding == null) return
        val entries = mutableListOf<Entry>()
        val chartValue = if (currentValuation > 0) currentValuation else 100.0
        entries.add(Entry(0f, (chartValue * 0.4).toFloat()))
        entries.add(Entry(1f, (chartValue * 0.55).toFloat()))
        entries.add(Entry(2f, (chartValue * 0.82).toFloat()))
        entries.add(Entry(3f, chartValue.toFloat()))

        val set = LineDataSet(entries, "Natural Wealth Growth").apply {
            color = Color.parseColor("#2E7D32")
            lineWidth = 3f
            setDrawFilled(true)
            fillColor = Color.parseColor("#C8E6C9")
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawValues(false)
            setCircleColor(Color.parseColor("#5D4037"))
        }

        binding.chartWealthGrowth.data = LineData(set)
        binding.chartWealthGrowth.axisRight?.isEnabled = false
        binding.chartWealthGrowth.xAxis?.position = XAxis.XAxisPosition.BOTTOM
        binding.chartWealthGrowth.description?.isEnabled = false
        binding.chartWealthGrowth.legend?.textColor = Color.parseColor("#5D4037")
        binding.chartWealthGrowth.invalidate()
    }

    private fun showWealthBreakdown() {
        if (registeredTrees.isEmpty()) {
            Toast.makeText(context, "No trees registered yet", Toast.LENGTH_SHORT).show()
            return
        }
        var growth = 0.0
        var heartwood = 0.0
        registeredTrees.forEach {
            growth += it.girth * 450.0
            heartwood += MaturityCalculator.estimateHeartwoodWeight(it.girth) * 16500
        }
        val msg = String.format(Locale.getDefault(), getString(R.string.wealth_breakdown_format), 
            registeredTrees.size * 2500.0, growth, heartwood, currentValuation)
        
        // FIX 2: AlertDialog Builder usage Ensured with correct import
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.wealth_breakdown_title)
            .setMessage(msg)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
