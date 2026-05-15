package com.gandhasiri.app;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.gandhasiri.app.databinding.FragmentTreeDetailBinding;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TreeDetailFragment extends Fragment {
    private FragmentTreeDetailBinding binding;
    private TreeViewModel treeViewModel;
    private Tree currentTree;
    private int treeId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTreeDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        treeViewModel = new ViewModelProvider(this).get(TreeViewModel.class);

        if (getArguments() != null) {
            treeId = getArguments().getInt("treeId");
            
            treeViewModel.getTreeById(treeId).observe(getViewLifecycleOwner(), tree -> {
                if (tree != null) {
                    currentTree = tree;
                    updateUI(tree);
                }
            });

            treeViewModel.getGrowthLogs(treeId).observe(getViewLifecycleOwner(), this::updateGrowthChart);

            binding.btnViewMap.setOnClickListener(v -> openInMaps());
            binding.btnAskAi.setOnClickListener(v -> runAiVisionScan());
            binding.btnDeleteTree.setOnClickListener(v -> confirmDelete());
        }

        binding.btnAddLog.setOnClickListener(v -> showAddLogDialog());
    }

    private void updateUI(Tree tree) {
        binding.tvDetailId.setText(getString(R.string.label_tree_id, tree.getTreeId()));
        if (tree.getPhotoPath() != null && !tree.getPhotoPath().isEmpty()) {
            Glide.with(this).load(Uri.parse(tree.getPhotoPath()))
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(binding.ivDetailPhoto);
        }

        double maturity = MaturityCalculator.calculateMaturityPercentage(tree.getGirth(), tree.getDatePlanted());
        binding.pbMaturityGauge.setProgress((int) maturity);
        binding.tvMaturityPercent.setText(String.format(Locale.getDefault(), "%.0f%%", maturity));
        
        if (maturity < 30) {
            binding.tvMaturityStatusLabel.setText(getString(R.string.status_sapling_label));
            binding.tvMaturityStatusLabel.setTextColor(Color.parseColor("#388E3C"));
        } else if (maturity < 80) {
            binding.tvMaturityStatusLabel.setText(getString(R.string.status_growing_label));
            binding.tvMaturityStatusLabel.setTextColor(Color.parseColor("#FBC02D"));
        } else {
            binding.tvMaturityStatusLabel.setText(getString(R.string.status_ready_label));
            binding.tvMaturityStatusLabel.setTextColor(Color.parseColor("#D32F2F"));
        }
    }

    private void updateGrowthChart(List<GrowthLog> logs) {
        if (logs == null || logs.isEmpty()) return;

        List<Entry> entries = new ArrayList<>();
        final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());

        for (int i = 0; i < logs.size(); i++) {
            entries.add(new Entry(i, (float) logs.get(i).girth));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Girth (cm)");
        dataSet.setColor(Color.parseColor("#5D4037"));
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleColor(Color.parseColor("#388E3C"));
        dataSet.setCircleRadius(5f);
        dataSet.setDrawFilled(true);
        dataSet.setFillAlpha(50);
        dataSet.setFillColor(Color.parseColor("#F5DEB3"));

        LineData lineData = new LineData(dataSet);
        binding.chartGrowthHistory.setData(lineData);
        binding.chartGrowthHistory.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int idx = (int) value;
                if (idx >= 0 && idx < logs.size()) {
                    return sdf.format(new Date(logs.get(idx).timestamp));
                }
                return "";
            }
        });
        binding.chartGrowthHistory.getDescription().setEnabled(false);
        binding.chartGrowthHistory.animateY(800);
        binding.chartGrowthHistory.invalidate();
    }

    private void runAiVisionScan() {
        if (currentTree == null) return;
        binding.vScanningBar.setVisibility(View.VISIBLE);
        binding.btnAskAi.setEnabled(false);
        binding.tvAiAdvice.setText(R.string.ai_init_scan);

        ObjectAnimator animator = ObjectAnimator.ofFloat(binding.vScanningBar, "translationY", 0f, 600f);
        animator.setDuration(1500);
        animator.setRepeatCount(2);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();

        binding.vScanningBar.postDelayed(() -> {
            if (binding == null) return;
            binding.vScanningBar.setVisibility(View.GONE);
            String advice = currentTree.getGirth() < 25 ? getString(R.string.ai_advice_sapling) : getString(R.string.ai_advice_mature);
            binding.tvAiAdvice.setText(advice);
            binding.btnAskAi.setEnabled(true);
            binding.btnAskAi.setText(R.string.btn_run_new_scan);
        }, 4500);
    }

    private void openInMaps() {
        if (currentTree == null || (currentTree.getLatitude() == 0 && currentTree.getLongitude() == 0)) return;
        Uri gmmIntentUri = Uri.parse("geo:" + currentTree.getLatitude() + "," + currentTree.getLongitude() + "?q=" + currentTree.getLatitude() + "," + currentTree.getLongitude() + "(" + currentTree.getTreeId() + ")");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    private void showAddLogDialog() {
        EditText input = new EditText(requireContext());
        input.setHint(R.string.hint_enter_girth);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_update_growth)
                .setView(input)
                .setPositiveButton(R.string.btn_save, (dialog, which) -> {
                    String value = input.getText().toString();
                    if (!value.isEmpty()) {
                        treeViewModel.updateTreeGirth(currentTree, Double.parseDouble(value));
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void confirmDelete() {
        if (currentTree == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Tree")
                .setMessage("Are you sure you want to remove this tree record? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    treeViewModel.delete(currentTree);
                    Toast.makeText(getContext(), "Tree record deleted", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
