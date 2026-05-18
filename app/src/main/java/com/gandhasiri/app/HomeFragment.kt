package com.gandhasiri.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
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
 * HomeFragment: Professional Dashboard for Gandha-Siri.
 * Implements a high-hierarchy layout with "Natural Wealth" summary and 
 * an integrated "Estate Preview" map.
 */
class HomeFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private var googleMap: GoogleMap? = null
    private var registeredTrees: List<Tree> = ArrayList()
    private var currentValuation = 0.0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isMapReady = false

    // Scenic fallback location (Karnataka Sandalwood Hub)
    private val SCENIC_FARM_LOCATION = LatLng(13.9299, 75.5681)

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                centerMapOnEstate()
            }
        }

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

        // 1. Setup Map Estate Preview
        val mapFragment = childFragmentManager.findFragmentById(R.id.home_map_container) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        // 2. Load and Observe Tree Data
        val db = AppDatabase.getDatabase(requireContext())
        db.treeDao().getAllTrees().observe(viewLifecycleOwner) { trees ->
            trees?.let {
                registeredTrees = it
                updateDashboardUI()
                if (isMapReady) updateMapMarkers()
            }
        }

        // 3. Interaction Handlers
        setupQuickLinks()
    }

    private fun setupQuickLinks() {
        binding.cvWealth.setOnClickListener { showWealthBreakdown() }
        binding.btnViewFullMap.setOnClickListener { findNavController().navigate(R.id.navigation_map) }
        binding.cardRegister.setOnClickListener { findNavController().navigate(R.id.navigation_register) }
        binding.cardLegal.setOnClickListener { showLegalGuide() }
        binding.cardSecurityAudit.setOnClickListener { showSecurityChecklist() }
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
        
        // Premium HD Satellite View
        googleMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
        googleMap?.uiSettings?.isAllGesturesEnabled = false // Static Dashboard look
        googleMap?.uiSettings?.isMapToolbarEnabled = false
        
        centerMapOnEstate()
        updateMapMarkers()
    }

    private fun centerMapOnEstate() {
        val map = googleMap ?: return

        if (registeredTrees.isEmpty()) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { loc ->
                        val target = if (loc != null) LatLng(loc.latitude, loc.longitude) else SCENIC_FARM_LOCATION
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(target, 17f))
                    }
            } else {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(SCENIC_FARM_LOCATION, 15f))
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            }
        } else {
            val builder = LatLngBounds.Builder()
            for (tree in registeredTrees) {
                builder.include(LatLng(tree.latitude, tree.longitude))
            }
            
            map.setOnMapLoadedCallback {
                if (googleMap == null || registeredTrees.isEmpty()) return@setOnMapLoadedCallback
                try {
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 120))
                } catch (e: Exception) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(registeredTrees[0].latitude, registeredTrees[0].longitude), 18f))
                }
            }
        }
    }

    private fun updateMapMarkers() {
        val map = googleMap ?: return
        if (registeredTrees.isEmpty()) return
        map.clear()
        
        val icon = createCustomCircularMarker(requireContext())
        
        for (tree in registeredTrees) {
            map.addMarker(MarkerOptions()
                .position(LatLng(tree.latitude, tree.longitude))
                .icon(icon)
                .anchor(0.5f, 0.5f))
        }
    }

    private fun createCustomCircularMarker(context: Context): BitmapDescriptor {
        val radius = 50
        val bitmap = Bitmap.createBitmap(radius, radius, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        
        // Outer White border for pop
        paint.color = Color.WHITE
        canvas.drawCircle(radius / 2f, radius / 2f, radius / 2f, paint)
        
        // Inner Forest Green core (Matches reference image)
        paint.color = Color.parseColor("#4CAF50")
        canvas.drawCircle(radius / 2f, radius / 2f, radius / 2f - 6, paint)
        
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun renderGrowthAnalytics() {
        if (_binding == null) return
        val entries = ArrayList<Entry>()
        val `val` = Math.max(100.0, currentValuation)
        entries.add(Entry(0f, (`val` * 0.4).toFloat()))
        entries.add(Entry(1f, (`val` * 0.55).toFloat()))
        entries.add(Entry(2f, (`val` * 0.82).toFloat()))
        entries.add(Entry(3f, `val`.toFloat()))

        val set = LineDataSet(entries, "Asset Growth")
        set.color = Color.parseColor("#2E7D32") // Forest Green
        set.lineWidth = 3f
        set.setDrawFilled(true)
        set.fillColor = Color.parseColor("#C8E6C9")
        set.mode = LineDataSet.Mode.CUBIC_BEZIER
        set.setDrawValues(false)
        set.circleColor = Color.parseColor("#5D4037") // Deep Bark Brown

        binding.chartWealthGrowth.data = LineData(set)
        binding.chartWealthGrowth.axisRight.isEnabled = false
        binding.chartWealthGrowth.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.chartWealthGrowth.description.isEnabled = false
        binding.chartWealthGrowth.legend.textColor = Color.parseColor("#5D4037")
        binding.chartWealthGrowth.invalidate()
    }

    private fun showWealthBreakdown() {
        if (registeredTrees.isEmpty()) return
        var base = registeredTrees.size * 2500.0
        var growth = 0.0
        var heartwood = 0.0
        for (t in registeredTrees) {
            growth += t.girth * 450.0
            heartwood += MaturityCalculator.estimateHeartwoodWeight(t.girth) * 16500
        }
        val msg = String.format(Locale.getDefault(), getString(R.string.wealth_breakdown_format), base, growth, heartwood, currentValuation)
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.wealth_breakdown_title)
            .setMessage(msg)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showLegalGuide() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.btn_legal_guide)
            .setMessage(R.string.legal_guide_content)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showSecurityChecklist() {
        val items = arrayOf("Fencing Verified", "CCTV Monitoring", "Panic Alert System", "Boundary Patrol logs", "Night Lighting")
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.security_checklist_title)
            .setMultiChoiceItems(items, booleanArrayOf(true, false, true, false, true), null)
            .setPositiveButton("Sync Status", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
