package com.gandhasiri.app;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.gandhasiri.app.databinding.FragmentRegisterBinding;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class RegisterFragment extends Fragment {
    private FragmentRegisterBinding binding;
    private TreeViewModel treeViewModel;
    private FusedLocationProviderClient fusedLocationClient;
    private double currentLat = 0, currentLng = 0;
    private Uri photoUri;
    private long selectedDateMillis;
    private final Calendar calendar = Calendar.getInstance();

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    binding.ivTreePhoto.setImageURI(photoUri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        treeViewModel = new ViewModelProvider(this).get(TreeViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        
        selectedDateMillis = System.currentTimeMillis();
        updateDateLabel();

        binding.btnGetLocation.setOnClickListener(v -> captureLocation());
        binding.btnSaveTree.setOnClickListener(v -> saveTree());
        binding.btnTakePhoto.setOnClickListener(v -> checkCameraPermission());
        binding.btnPickDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            selectedDateMillis = calendar.getTimeInMillis();
            updateDateLabel();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        binding.tvSelectedDate.setText("Planting Date: " + sdf.format(calendar.getTime()));
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 101);
        } else {
            launchCamera();
        }
    }

    private void launchCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Tree Photo");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Gandha-Siri App");
        photoUri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        cameraLauncher.launch(intent);
    }

    private void captureLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        binding.tvLocation.setText("Locating...");
        CurrentLocationRequest request = new CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();

        fusedLocationClient.getCurrentLocation(request, null).addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                currentLat = location.getLatitude();
                currentLng = location.getLongitude();
                binding.tvLocation.setText(String.format(Locale.getDefault(), "Location: %.4f, %.4f", currentLat, currentLng));
            } else {
                binding.tvLocation.setText("Location: Failed (Check GPS)");
            }
        });
    }

    private void saveTree() {
        String girthStr = binding.etGirth.getText().toString();
        if (girthStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter girth", Toast.LENGTH_SHORT).show();
            return;
        }

        double girth = Double.parseDouble(girthStr);
        String treeId = "SAN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        Tree newTree = new Tree(
                treeId,
                photoUri != null ? photoUri.toString() : "",
                currentLat,
                currentLng,
                girth,
                selectedDateMillis,
                "Healthy"
        );

        treeViewModel.insert(newTree, girth);
        Toast.makeText(getContext(), "Successfully Registered: " + treeId, Toast.LENGTH_SHORT).show();
        
        // Navigate to Tracker to see the newly registered tree
        Navigation.findNavController(requireView()).navigate(R.id.navigation_tracker);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            captureLocation();
        } else if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
