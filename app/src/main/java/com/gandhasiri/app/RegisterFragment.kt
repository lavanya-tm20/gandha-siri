package com.gandhasiri.app

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.gandhasiri.app.databinding.FragmentRegisterBinding
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.text.SimpleDateFormat
import java.util.*

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var treeViewModel: TreeViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLat = 0.0
    private var currentLng = 0.0
    private var photoUri: Uri? = null
    private var selectedDateMillis: Long = System.currentTimeMillis()
    private val calendar = Calendar.getInstance()

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            binding.ivTreePhoto.setImageURI(photoUri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        treeViewModel = ViewModelProvider(this).get(TreeViewModel::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        
        selectedDateMillis = System.currentTimeMillis()
        updateDateLabel()

        binding.btnGetLocation.setOnClickListener { captureLocation() }
        binding.btnSaveTree.setOnClickListener { saveTree() }
        binding.btnTakePhoto.setOnClickListener { checkCameraPermission() }
        binding.btnPickDate.setOnClickListener { showDatePicker() }
    }

    private fun showDatePicker() {
        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            selectedDateMillis = calendar.timeInMillis
            updateDateLabel()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateDateLabel() {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        binding.tvSelectedDate.text = "Planting Date: ${sdf.format(calendar.time)}"
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 101)
        } else {
            launchCamera()
        }
    }

    private fun launchCamera() {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "New Tree Photo")
            put(MediaStore.Images.Media.DESCRIPTION, "From Gandha-Siri App")
        }
        photoUri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        }
        cameraLauncher.launch(intent)
    }

    private fun captureLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        binding.tvLocation.text = "Locating..."
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        fusedLocationClient.getCurrentLocation(request, null).addOnSuccessListener(requireActivity()) { location ->
            if (location != null) {
                currentLat = location.latitude
                currentLng = location.longitude
                binding.tvLocation.text = String.format(Locale.getDefault(), "Location: %.4f, %.4f", currentLat, currentLng)
            } else {
                binding.tvLocation.text = "Location: Failed (Check GPS)"
            }
        }
    }

    private fun saveTree() {
        val girthStr = binding.etGirth.text.toString()
        if (girthStr.isEmpty()) {
            Toast.makeText(context, "Please enter girth", Toast.LENGTH_SHORT).show()
            return
        }

        val girth = girthStr.toDouble()
        val treeId = "SAN-" + UUID.randomUUID().toString().substring(0, 8).uppercase(Locale.getDefault())
        
        val newTree = Tree(
            treeId = treeId,
            photoPath = photoUri?.toString() ?: "",
            latitude = currentLat,
            longitude = currentLng,
            girth = girth,
            datePlanted = selectedDateMillis,
            healthStatus = "Healthy"
        )

        treeViewModel.insert(newTree, girth)
        Toast.makeText(context, "Successfully Registered: $treeId", Toast.LENGTH_SHORT).show()
        
        findNavController().navigate(R.id.navigation_tracker)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            captureLocation()
        } else if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
