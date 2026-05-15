package com.gandhasiri.app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.gandhasiri.app.databinding.FragmentMapBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private FragmentMapBinding binding;
    private GoogleMap googleMap;
    private TreeViewModel treeViewModel;
    private TreeAdapter adapter;
    private FusedLocationProviderClient fusedLocationClient;
    private List<Tree> registeredTrees = new ArrayList<>();
    private boolean isInitialZoomDone = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new TreeAdapter();
        binding.rvNearbyTrees.setAdapter(adapter);

        adapter.setOnTreeClickListener(tree -> {
            Bundle bundle = new Bundle();
            bundle.putInt("treeId", tree.getId());
            Navigation.findNavController(view).navigate(R.id.navigation_tree_detail, bundle);
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_container);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        treeViewModel = new ViewModelProvider(this).get(TreeViewModel.class);
        treeViewModel.getAllTrees().observe(getViewLifecycleOwner(), trees -> {
            if (trees != null) {
                registeredTrees = trees;
                if (trees.isEmpty()) {
                    if (binding != null) binding.tvUserLocation.setText("Your digital tree register is empty.");
                } else {
                    updateMapMarkers();
                }
                adapter.setTrees(trees);
                refreshUserLocation();
            }
        });

        binding.fabRefreshMap.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Scanning for nearby trees...", Toast.LENGTH_SHORT).show();
            refreshUserLocation();
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        // Using Hybrid/Satellite for the "Estate" view
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
        
        if (!registeredTrees.isEmpty()) {
            updateMapMarkers();
        } else {
            centerOnCurrentLocation();
        }
    }

    private void updateMapMarkers() {
        if (googleMap == null || registeredTrees.isEmpty()) return;
        
        googleMap.clear();
        BitmapDescriptor customIcon = createTreeMarkerIcon(requireContext());
        LatLng firstTree = null;

        for (Tree tree : registeredTrees) {
            LatLng pos = new LatLng(tree.getLatitude(), tree.getLongitude());
            if (firstTree == null) firstTree = pos;
            
            googleMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title("ID: " + tree.getTreeId())
                    .icon(customIcon)
                    .anchor(0.5f, 0.5f));
        }
        
        if (firstTree != null && !isInitialZoomDone) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(firstTree, 18f));
            isInitialZoomDone = true;
        }
    }

    private BitmapDescriptor createTreeMarkerIcon(Context context) {
        int size = 45;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        // White border
        paint.setColor(Color.WHITE);
        canvas.drawCircle(size/2f, size/2f, size/2f, paint);
        
        // Leaf Green Core
        paint.setColor(Color.parseColor("#4CAF50"));
        canvas.drawCircle(size/2f, size/2f, size/2f - 5, paint);
        
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void centerOnCurrentLocation() {
        if (googleMap == null) return;
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null && googleMap != null && !isInitialZoomDone) {
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(location.getLatitude(), location.getLongitude()), 17f));
                            isInitialZoomDone = true;
                        }
                    });
        }
    }

    private void refreshUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(requireActivity(), location -> {
                    // CRITICAL FIX: Check if binding is null before accessing views in async callback
                    if (location != null && binding != null) {
                        binding.tvUserLocation.setText(String.format(Locale.getDefault(), 
                                "Sync: %.4f, %.4f", location.getLatitude(), location.getLongitude()));
                        calculateDistancesAndSort(location);
                        
                        if (googleMap != null && registeredTrees.isEmpty() && !isInitialZoomDone) {
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(location.getLatitude(), location.getLongitude()), 17f));
                            isInitialZoomDone = true;
                        }
                    }
                });
    }

    private void calculateDistancesAndSort(Location userLoc) {
        if (registeredTrees.isEmpty() || adapter == null) return;

        List<TreeWithDistance> sortedList = new ArrayList<>();
        for (Tree tree : registeredTrees) {
            float[] results = new float[1];
            Location.distanceBetween(userLoc.getLatitude(), userLoc.getLongitude(), 
                    tree.getLatitude(), tree.getLongitude(), results);
            sortedList.add(new TreeWithDistance(tree, results[0]));
        }

        Collections.sort(sortedList, (a, b) -> Float.compare(a.distance, b.distance));

        List<Tree> sortedTrees = new ArrayList<>();
        List<Float> sortedDistances = new ArrayList<>();
        for (TreeWithDistance twd : sortedList) {
            sortedTrees.add(twd.tree);
            sortedDistances.add(twd.distance);
        }
        adapter.setTreesWithDistances(sortedTrees, sortedDistances);
    }

    private static class TreeWithDistance {
        Tree tree;
        float distance;
        TreeWithDistance(Tree t, float d) {
            this.tree = t;
            this.distance = d;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
