package com.gandhasiri.app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.gandhasiri.app.databinding.FragmentHomeBinding;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * HomeFragment: Professional Dashboard for Gandha-Siri.
 * Implements a high-hierarchy layout with "Natural Wealth" summary and 
 * an integrated "Estate Preview" map.
 */
public class HomeFragment extends Fragment implements OnMapReadyCallback {
    private FragmentHomeBinding binding;
    private GoogleMap googleMap;
    private List<Tree> registeredTrees = new ArrayList<>();
    private double currentValuation = 0.0;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean isMapReady = false;

    // Scenic fallback location (Karnataka Sandalwood Hub)
    private final LatLng SCENIC_FARM_LOCATION = new LatLng(13.9299, 75.5681);

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION))) {
                    centerMapOnEstate();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Setup Map Estate Preview
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.home_map_container);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        // 2. Load and Observe Tree Data
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        db.treeDao().getAllTrees().observe(getViewLifecycleOwner(), trees -> {
            if (trees != null && binding != null) {
                registeredTrees = trees;
                updateDashboardUI();
                if (isMapReady) updateMapMarkers();
            }
        });

        // 3. Interaction Handlers
        setupQuickLinks();
    }

    private void setupQuickLinks() {
        binding.cvWealth.setOnClickListener(v -> showWealthBreakdown());
        binding.btnViewFullMap.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.navigation_map));
        binding.cardRegister.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.navigation_register));
        binding.cardLegal.setOnClickListener(v -> showLegalGuide());
        binding.cardSecurityAudit.setOnClickListener(v -> showSecurityChecklist());
        binding.cardPanic.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.navigation_security));
    }

    private void updateDashboardUI() {
        binding.tvTreeCount.setText(getString(R.string.trees_tagged_format, registeredTrees.size()));
        
        currentValuation = 0;
        long earliestPlanted = System.currentTimeMillis();

        for (Tree tree : registeredTrees) {
            currentValuation += MaturityCalculator.calculateTreeValue(tree.getGirth());
            if (tree.getDatePlanted() < earliestPlanted) earliestPlanted = tree.getDatePlanted();
        }

        binding.tvTotalValuation.setText(String.format(Locale.getDefault(), "₹ %,.2f", currentValuation));
        
        if (!registeredTrees.isEmpty()) {
            long harvestDate = earliestPlanted + (20L * 365 * 24 * 60 * 60 * 1000);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM yyyy", Locale.getDefault());
            binding.tvHarvestEstimate.setText(getString(R.string.harvest_estimate_format, sdf.format(new java.util.Date(harvestDate))));
        } else {
            binding.tvHarvestEstimate.setText(getString(R.string.harvest_estimate_format, "--"));
        }

        renderGrowthAnalytics();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        this.isMapReady = true;
        
        // Premium HD Satellite View
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        googleMap.getUiSettings().setAllGesturesEnabled(false); // Static Dashboard look
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        
        centerMapOnEstate();
        updateMapMarkers();
    }

    private void centerMapOnEstate() {
        if (googleMap == null) return;

        if (registeredTrees.isEmpty()) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener(loc -> {
                            if (googleMap == null) return;
                            LatLng target = (loc != null) ? new LatLng(loc.getLatitude(), loc.getLongitude()) : SCENIC_FARM_LOCATION;
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(target, 17f));
                        });
            } else {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SCENIC_FARM_LOCATION, 15f));
                requestPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
            }
        } else {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Tree tree : registeredTrees) builder.include(new LatLng(tree.getLatitude(), tree.getLongitude()));
            
            googleMap.setOnMapLoadedCallback(() -> {
                if (googleMap == null || registeredTrees.isEmpty()) return;
                try {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 120));
                } catch (Exception e) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(registeredTrees.get(0).getLatitude(), registeredTrees.get(0).getLongitude()), 18f));
                }
            });
        }
    }

    private void updateMapMarkers() {
        if (googleMap == null || registeredTrees.isEmpty()) return;
        googleMap.clear();
        
        BitmapDescriptor icon = createCustomCircularMarker(requireContext());
        
        for (Tree tree : registeredTrees) {
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(tree.getLatitude(), tree.getLongitude()))
                    .icon(icon)
                    .anchor(0.5f, 0.5f));
        }
    }

    private BitmapDescriptor createCustomCircularMarker(Context context) {
        int radius = 50;
        Bitmap bitmap = Bitmap.createBitmap(radius, radius, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        // Outer White border for pop
        paint.setColor(Color.WHITE);
        canvas.drawCircle(radius/2f, radius/2f, radius/2f, paint);
        
        // Inner Forest Green core (Matches reference image)
        paint.setColor(Color.parseColor("#4CAF50"));
        canvas.drawCircle(radius/2f, radius/2f, radius/2f - 6, paint);
        
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void renderGrowthAnalytics() {
        if (binding == null) return;
        List<Entry> entries = new ArrayList<>();
        double val = Math.max(100.0, currentValuation);
        entries.add(new Entry(0, (float)(val * 0.4)));
        entries.add(new Entry(1, (float)(val * 0.55)));
        entries.add(new Entry(2, (float)(val * 0.82)));
        entries.add(new Entry(3, (float)val));

        LineDataSet set = new LineDataSet(entries, "Asset Growth");
        set.setColor(Color.parseColor("#2E7D32")); // Forest Green
        set.setLineWidth(3f);
        set.setDrawFilled(true);
        set.setFillColor(Color.parseColor("#C8E6C9"));
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setDrawValues(false);
        set.setCircleColor(Color.parseColor("#5D4037")); // Deep Bark Brown

        binding.chartWealthGrowth.setData(new LineData(set));
        binding.chartWealthGrowth.getAxisRight().setEnabled(false);
        binding.chartWealthGrowth.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        binding.chartWealthGrowth.getDescription().setEnabled(false);
        binding.chartWealthGrowth.getLegend().setTextColor(Color.parseColor("#5D4037"));
        binding.chartWealthGrowth.invalidate();
    }

    private void showWealthBreakdown() {
        if (registeredTrees.isEmpty()) return;
        double base = registeredTrees.size() * 2500.0;
        double growth = 0, heartwood = 0;
        for (Tree t : registeredTrees) {
            growth += t.getGirth() * 450.0;
            heartwood += MaturityCalculator.estimateHeartwoodWeight(t.getGirth()) * 16500;
        }
        String msg = String.format(Locale.getDefault(), getString(R.string.wealth_breakdown_format), base, growth, heartwood, currentValuation);
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.wealth_breakdown_title)
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showLegalGuide() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.btn_legal_guide)
                .setMessage(R.string.legal_guide_content)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showSecurityChecklist() {
        String[] items = {"Fencing Verified", "CCTV Monitoring", "Panic Alert System", "Boundary Patrol logs", "Night Lighting"};
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.security_checklist_title)
                .setMultiChoiceItems(items, new boolean[]{true, false, true, false, true}, null)
                .setPositiveButton("Sync Status", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
