package com.gandhasiri.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.gandhasiri.app.databinding.FragmentCertificateBinding
import java.text.SimpleDateFormat
import java.util.*

class CertificateFragment : Fragment() {
    private var _binding: FragmentCertificateBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCertificateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val treeId = it.getString("treeUid") ?: "N/A"
            val girth = it.getFloat("girth")
            val lat = it.getFloat("lat")
            val lng = it.getFloat("lng")
            val dateMillis = it.getLong("datePlanted")

            binding.tvCertTreeId.text = "Tree ID: $treeId"
            binding.tvCertGirth.text = "Registered Girth: $girth cm"
            binding.tvCertLocation.text = String.format(Locale.getDefault(), "GPS Coordinates: %.4f, %.4f", lat, lng)
            
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            binding.tvCertDate.text = "Registration Date: ${sdf.format(Date(dateMillis))}"
        }

        binding.btnShareCert.setOnClickListener {
            Toast.makeText(context, "Generating Secure PDF...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
