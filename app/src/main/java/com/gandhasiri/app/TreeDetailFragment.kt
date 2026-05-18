package com.gandhasiri.app

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.gandhasiri.app.databinding.FragmentTreeDetailBinding
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class TreeDetailFragment : Fragment() {
    private var _binding: FragmentTreeDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var treeViewModel: TreeViewModel
    private var currentTree: Tree? = null
    private var treeId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTreeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        treeViewModel = ViewModelProvider(this).get(TreeViewModel::class.java)

        arguments?.let {
            treeId = it.getInt("treeId")
            
            treeViewModel.getTreeById(treeId).observe(viewLifecycleOwner) { tree ->
                tree?.let { t ->
                    currentTree = t
                    updateUI(t)
                }
            }

            treeViewModel.getGrowthLogs(treeId).observe(viewLifecycleOwner) { logs ->
                updateGrowthChart(logs)
            }

            binding.btnViewMap.setOnClickListener { openInMaps() }
            binding.btnAskAi.setOnClickListener { runAiVisionScan() }
            binding.btnDeleteTree.setOnClickListener { confirmDelete() }
            binding.btnGenerateCert.setOnClickListener { navigateToCertificate() }
        }

        binding.btnAddLog.setOnClickListener { showAddLogDialog() }
    }

    private fun updateUI(tree: Tree) {
        binding.tvDetailId.text = getString(R.string.label_tree_id, tree.treeId)
        if (tree.photoPath.isNotEmpty()) {
            Glide.with(this).load(Uri.parse(tree.photoPath))
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.ivDetailPhoto)
        }

        val maturity = MaturityCalculator.calculateMaturityPercentage(tree.girth, tree.datePlanted)
        binding.pbMaturityGauge.progress = maturity.toInt()
        binding.tvMaturityPercent.text = String.format(Locale.getDefault(), "%.0f%%", maturity)
        
        when {
            maturity < 30 -> {
                binding.tvMaturityStatusLabel.text = getString(R.string.status_sapling_label)
                binding.tvMaturityStatusLabel.setTextColor(Color.parseColor("#388E3C"))
            }
            maturity < 80 -> {
                binding.tvMaturityStatusLabel.text = getString(R.string.status_growing_label)
                binding.tvMaturityStatusLabel.setTextColor(Color.parseColor("#FBC02D"))
            }
            else -> {
                binding.tvMaturityStatusLabel.text = getString(R.string.status_ready_label)
                binding.tvMaturityStatusLabel.setTextColor(Color.parseColor("#D32F2F"))
            }
        }

        val yearsLeft = MaturityCalculator.estimateYearsToHarvest(tree.girth, tree.datePlanted)
        binding.tvHarvestCountdown.text = getString(R.string.harvest_countdown_format, yearsLeft)
        
        val heartwood = MaturityCalculator.estimateHeartwoodWeight(tree.girth)
        binding.tvHeartwoodEstimate.text = getString(R.string.heartwood_estimate_format, heartwood)
    }

    private fun updateGrowthChart(logs: List<GrowthLog>?) {
        if (logs.isNullOrEmpty()) return

        val entries = ArrayList<Entry>()
        val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())

        for (i in logs.indices) {
            entries.add(Entry(i.toFloat(), logs[i].girth.toFloat()))
        }

        val dataSet = LineDataSet(entries, "Girth (cm)").apply {
            color = Color.parseColor("#5D4037")
            lineWidth = 2.5f
            setCircleColor(Color.parseColor("#388E3C"))
            circleRadius = 5f
            setDrawFilled(true)
            fillAlpha = 50
            fillColor = Color.parseColor("#F5DEB3")
        }

        val lineData = LineData(dataSet)
        binding.chartGrowthHistory.data = lineData
        binding.chartGrowthHistory.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val idx = value.toInt()
                return if (idx in logs.indices) {
                    sdf.format(Date(logs[idx].timestamp))
                } else ""
            }
        }
        binding.chartGrowthHistory.description.isEnabled = false
        binding.chartGrowthHistory.animateY(800)
        binding.chartGrowthHistory.invalidate()
    }

    private fun runAiVisionScan() {
        val tree = currentTree ?: return
        binding.vScanningBar.visibility = View.VISIBLE
        binding.btnAskAi.isEnabled = false
        binding.tvAiAdvice.setText(R.string.ai_init_scan)

        ObjectAnimator.ofFloat(binding.vScanningBar, "translationY", 0f, 600f).apply {
            duration = 1500
            repeatCount = 2
            repeatMode = ValueAnimator.REVERSE
            interpolator = LinearInterpolator()
            start()
        }

        binding.vScanningBar.postDelayed({
            _binding?.let {
                it.vScanningBar.visibility = View.GONE
                val advice = if (tree.girth < 25) getString(R.string.ai_advice_sapling) else getString(R.string.ai_advice_mature)
                it.tvAiAdvice.text = advice
                it.btnAskAi.isEnabled = true
                it.btnAskAi.setText(R.string.btn_run_new_scan)
            }
        }, 4500)
    }

    private fun openInMaps() {
        val tree = currentTree ?: return
        if (tree.latitude == 0.0 && tree.longitude == 0.0) return
        val gmmIntentUri = Uri.parse("geo:${tree.latitude},${tree.longitude}?q=${tree.latitude},${tree.longitude}(${tree.treeId})")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        startActivity(mapIntent)
    }

    private fun showAddLogDialog() {
        val input = EditText(requireContext()).apply {
            hint = getString(R.string.hint_enter_girth)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_update_growth)
            .setView(input)
            .setPositiveButton(R.string.btn_save) { _, _ ->
                val value = input.text.toString()
                if (value.isNotEmpty()) {
                    currentTree?.let {
                        treeViewModel.updateTreeGirth(it, value.toDouble())
                    }
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun navigateToCertificate() {
        val tree = currentTree ?: return
        val bundle = Bundle().apply {
            putString("treeUid", tree.treeId)
            putFloat("girth", tree.girth.toFloat())
            putFloat("lat", tree.latitude.toFloat())
            putFloat("lng", tree.longitude.toFloat())
            putLong("datePlanted", tree.datePlanted)
        }
        findNavController().navigate(R.id.navigation_certificate, bundle)
    }

    private fun confirmDelete() {
        val tree = currentTree ?: return
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Tree")
            .setMessage("Are you sure you want to remove this tree record? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                treeViewModel.delete(tree)
                Toast.makeText(context, "Tree record deleted", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
