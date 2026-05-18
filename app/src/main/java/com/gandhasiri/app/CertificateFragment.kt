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

            binding.tvCertTreeId.text = getString(R.string.cert_id_format, treeId)
            binding.tvCertGirth.text = getString(R.string.cert_girth_format, girth)
            binding.tvCertLocation.text = getString(R.string.cert_location_format, lat, lng)
            
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            binding.tvCertDate.text = getString(R.string.cert_date_format, sdf.format(Date(dateMillis)))
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
