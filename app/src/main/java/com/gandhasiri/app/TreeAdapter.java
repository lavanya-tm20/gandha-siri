package com.gandhasiri.app;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.gandhasiri.app.databinding.ItemTreeBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TreeAdapter extends RecyclerView.Adapter<TreeAdapter.TreeViewHolder> {
    private List<Tree> trees = new ArrayList<>();
    private List<Float> distances = new ArrayList<>();
    private OnTreeClickListener listener;

    public interface OnTreeClickListener {
        void onTreeClick(Tree tree);
    }

    public void setOnTreeClickListener(OnTreeClickListener listener) {
        this.listener = listener;
    }

    public void setTrees(List<Tree> trees) {
        this.trees = trees;
        this.distances.clear();
        notifyDataSetChanged();
    }
    
    public void setTreesWithDistances(List<Tree> trees, List<Float> distances) {
        this.trees = trees;
        this.distances = distances;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TreeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTreeBinding binding = ItemTreeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TreeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TreeViewHolder holder, int position) {
        Tree tree = trees.get(position);
        holder.binding.tvTreeId.setText("ID: " + tree.getTreeId());
        holder.binding.tvTreeGirth.setText(String.format(Locale.getDefault(), "Girth: %.1f cm", tree.getGirth()));
        
        // Maturity logic
        double maturityPercent = MaturityCalculator.calculateMaturityPercentage(tree.getGirth(), tree.getDatePlanted());
        int yearsLeft = MaturityCalculator.estimateYearsToHarvest(tree.getGirth(), tree.getDatePlanted());
        
        String estimateText = String.format(Locale.getDefault(), "Maturity: %.0f%% (%d years left)", maturityPercent, yearsLeft);

        // Distance logic (for Map view)
        if (distances != null && position < distances.size()) {
            estimateText = String.format(Locale.getDefault(), "Dist: %.1f m | %s", distances.get(position), estimateText);
        }
        
        holder.binding.tvMaturityEstimate.setText(estimateText);
        holder.binding.pbMaturity.setProgress((int) maturityPercent);

        if (tree.getPhotoPath() != null && !tree.getPhotoPath().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(Uri.parse(tree.getPhotoPath()))
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.binding.ivTreeThumbnail);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTreeClick(tree);
        });
    }

    @Override
    public int getItemCount() {
        return trees.size();
    }

    static class TreeViewHolder extends RecyclerView.ViewHolder {
        ItemTreeBinding binding;
        TreeViewHolder(ItemTreeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
