package com.gandhasiri.app;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.gandhasiri.app.databinding.FragmentSecurityBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class SecurityFragment extends Fragment {
    private FragmentSecurityBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private static final String CHANNEL_ID = "security_alerts";
    private static final String EMERGENCY_NUMBER = "8088605057";
    private static final int PERMISSION_CODE = 103;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSecurityBinding.inflate(inflater, container, false);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        createNotificationChannel();
        binding.btnPanic.setOnClickListener(v -> checkPermissionsAndTrigger());
    }

    private void checkPermissionsAndTrigger() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_CODE);
        } else {
            triggerPanicAlert();
        }
    }

    private void triggerPanicAlert() {
        binding.tvAlertStatus.setText("Fetching Location...");
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    String locationMsg = "";
                    if (location != null) {
                        locationMsg = "\nLocation: https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
                    }
                    sendEmergencySms(locationMsg);
                    showNotification(locationMsg);
                    updateUIOnAlert();
                });
    }

    private void sendEmergencySms(String locationMsg) {
        try {
            SmsManager smsManager = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? 
                requireContext().getSystemService(SmsManager.class) : SmsManager.getDefault();
            
            String message = "EMERGENCY ALERT from Gandha-Siri! Suspicious activity at farm." + locationMsg;
            smsManager.sendTextMessage(EMERGENCY_NUMBER, null, message, null, null);
            Toast.makeText(getContext(), "Alert sent with location!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "SMS Failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUIOnAlert() {
        binding.tvAlertStatus.setText(R.string.alert_sent);
        binding.tvAlertStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        binding.btnPanic.setEnabled(false);
        binding.btnPanic.setAlpha(0.5f);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (binding != null) {
                binding.tvAlertStatus.setText("");
                binding.btnPanic.setEnabled(true);
                binding.btnPanic.setAlpha(1.0f);
            }
        }, 10000);
    }

    private void showNotification(String locationMsg) {
        NotificationManager nm = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("EMERGENCY ALERT SENT")
                .setContentText("Alert sent with your GPS location to " + EMERGENCY_NUMBER)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        nm.notify(1, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Security Alerts", NotificationManager.IMPORTANCE_HIGH);
            requireContext().getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            triggerPanicAlert();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
