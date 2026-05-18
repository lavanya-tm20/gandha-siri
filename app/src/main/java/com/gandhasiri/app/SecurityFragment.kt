package com.gandhasiri.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.gandhasiri.app.databinding.FragmentSecurityBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class SecurityFragment : Fragment() {
    private var _binding: FragmentSecurityBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val CHANNEL_ID = "security_alerts"
        private const val EMERGENCY_NUMBER = "8088605057"
        private const val PERMISSION_CODE = 103
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecurityBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createNotificationChannel()
        binding.btnPanic.setOnClickListener { checkPermissionsAndTrigger() }
    }

    private fun checkPermissionsAndTrigger() {
        val smsPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
        val locationPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        
        if (smsPermission != PackageManager.PERMISSION_GRANTED || locationPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_CODE)
        } else {
            triggerPanicAlert()
        }
    }

    private fun triggerPanicAlert() {
        binding.tvAlertStatus.text = "Fetching Location..."
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    var locationMsg = ""
                    location?.let {
                        locationMsg = "\nLocation: https://maps.google.com/?q=${it.latitude},${it.longitude}"
                    }
                    sendEmergencySms(locationMsg)
                    showNotification(locationMsg)
                    updateUIOnAlert()
                }
        }
    }

    private fun sendEmergencySms(locationMsg: String) {
        try {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requireContext().getSystemService(SmsManager::class.java)
            } else {
                SmsManager.getDefault()
            }
            
            val message = "EMERGENCY ALERT from Gandha-Siri! Suspicious activity at farm.$locationMsg"
            smsManager.sendTextMessage(EMERGENCY_NUMBER, null, message, null, null)
            Toast.makeText(context, "Alert sent with location!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "SMS Failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUIOnAlert() {
        binding.tvAlertStatus.setText(R.string.alert_sent)
        binding.tvAlertStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
        binding.btnPanic.isEnabled = false
        binding.btnPanic.alpha = 0.5f
        Handler(Looper.getMainLooper()).postDelayed({
            _binding?.let {
                it.tvAlertStatus.text = ""
                it.btnPanic.isEnabled = true
                it.btnPanic.alpha = 1.0f
            }
        }, 10000)
    }

    private fun showNotification(locationMsg: String) {
        val nm = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("EMERGENCY ALERT SENT")
            .setContentText("Alert sent with your GPS location to $EMERGENCY_NUMBER")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
        nm.notify(1, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Security Alerts", NotificationManager.IMPORTANCE_HIGH)
            val nm = requireContext().getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            triggerPanicAlert()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
