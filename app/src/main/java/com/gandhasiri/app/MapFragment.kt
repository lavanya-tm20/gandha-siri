package com.gandhasiri.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.gandhasiri.app.databinding.FragmentMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    
    private var googleMap: GoogleMap? = null
    private lateinit var treeViewModel: TreeViewModel
    private lateinit var adapter: TreeAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var registeredTrees: List<Tree> = ArrayList()
    private var isInitialZoomDone = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TreeAdapter()
        binding.rvNearbyTrees.adapter = adapter

        adapter.setOnTreeClickListener { tree ->
            val bundle = Bundle()
            bundle.putInt("treeId", tree.id)
            findNavController().navigate(R.id.navigation_tree_detail, bundle)
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_container) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        treeViewModel = ViewModelProvider(this).get(TreeViewModel::class.java)
        treeViewModel.getAllTrees().observe(viewLifecycleOwner) { trees ->
            if (trees != null) {
                registeredTrees = trees
                if (trees.isEmpty()) {
                    binding.tvUserLocation.text = "Your digital tree register is empty."
                } else {
                    updateMapMarkers()
                }
                adapter.setTrees(trees)
                refreshUserLocation()
            }
        }

        binding.fabRefreshMap.setOnClickListener {
            Toast.makeText(context, "Scanning for nearby trees...", Toast.LENGTH_SHORT).show()
            refreshUserLocation()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        this.googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = false
        googleMap?.uiSettings?.isMyLocationButtonEnabled = true
        googleMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
        
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap?.isMyLocationEnabled = true
        }
        
        if (registeredTrees.isNotEmpty()) {
            updateMapMarkers()
        } else {
            centerOnCurrentLocation()
        }
    }

    private fun updateMapMarkers() {
        val map = googleMap ?: return
        if (registeredTrees.isEmpty()) return
        
        map.clear()
        val customIcon = createTreeMarkerIcon(requireContext())
        var firstTree: LatLng? = null

        for (tree in registeredTrees) {
            val pos = LatLng(tree.latitude, tree.longitude)
            if (firstTree == null) firstTree = pos
            
            map.addMarker(MarkerOptions()
                .position(pos)
                .title("ID: " + tree.treeId)
                .icon(customIcon)
                .anchor(0.5f, 0.5f))
        }
        
        if (firstTree != null && !isInitialZoomDone) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(firstTree, 18f))
            isInitialZoomDone = true
        }
    }

    private fun createTreeMarkerIcon(context: Context): BitmapDescriptor {
        val size = 45
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        
        // White border
        paint.color = Color.WHITE
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        
        // Leaf Green Core
        paint.color = Color.parseColor("#4CAF50")
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 5, paint)
        
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun centerOnCurrentLocation() {
        if (googleMap == null) return
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(requireActivity()) { location ->
                    if (location != null && googleMap != null && !isInitialZoomDone) {
                        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            LatLng(location.latitude, location.longitude), 17f))
                        isInitialZoomDone = true
                    }
                }
        }
    }

    private fun refreshUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener(requireActivity()) { location ->
                if (location != null && _binding != null) {
                    binding.tvUserLocation.text = String.format(Locale.getDefault(), 
                        "Sync: %.4f, %.4f", location.latitude, location.longitude)
                    calculateDistancesAndSort(location)
                    
                    if (googleMap != null && registeredTrees.isEmpty() && !isInitialZoomDone) {
                        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            LatLng(location.latitude, location.longitude), 17f))
                        isInitialZoomDone = true
                    }
                }
            }
    }

    private fun calculateDistancesAndSort(userLoc: Location) {
        if (registeredTrees.isEmpty()) return

        val sortedList = ArrayList<TreeWithDistance>()
        for (tree in registeredTrees) {
            val results = FloatArray(1)
            Location.distanceBetween(userLoc.latitude, userLoc.longitude, 
                tree.latitude, tree.longitude, results)
            sortedList.add(TreeWithDistance(tree, results[0]))
        }

        sortedList.sortWith { a, b -> java.lang.Float.compare(a.distance, b.distance) }

        val sortedTrees = ArrayList<Tree>()
        val sortedDistances = ArrayList<Float>()
        for (twd in sortedList) {
            sortedTrees.add(twd.tree)
            sortedDistances.add(twd.distance)
        }
        adapter.setTreesWithDistances(sortedTrees, sortedDistances)
    }

    private class TreeWithDistance(var tree: Tree, var distance: Float)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
