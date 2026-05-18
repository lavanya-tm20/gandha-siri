package com.gandhasiri.app

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gandhasiri.app.databinding.ItemTreeBinding
import java.util.*

class TreeAdapter : RecyclerView.Adapter<TreeAdapter.TreeViewHolder>() {
    private var trees: List<Tree> = ArrayList()
    private var distances: List<Float>? = null
    private var listener: OnTreeClickListener? = null

    fun interface OnTreeClickListener {
        fun onTreeClick(tree: Tree)
    }

    fun setOnTreeClickListener(listener: OnTreeClickListener) {
        this.listener = listener
    }

    fun setTrees(trees: List<Tree>) {
        this.trees = trees
        this.distances = null
        notifyDataSetChanged()
    }
    
    fun setTreesWithDistances(trees: List<Tree>, distances: List<Float>) {
        this.trees = trees
        this.distances = distances
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TreeViewHolder {
        val binding = ItemTreeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TreeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TreeViewHolder, position: Int) {
        val tree = trees[position]
        holder.binding.tvTreeId.text = "ID: ${tree.treeId}"
        holder.binding.tvTreeGirth.text = String.format(Locale.getDefault(), "Girth: %.1f cm", tree.girth)
        
        // Maturity logic
        val maturityPercent = MaturityCalculator.calculateMaturityPercentage(tree.girth, tree.datePlanted)
        val yearsLeft = MaturityCalculator.estimateYearsToHarvest(tree.girth, tree.datePlanted)
        
        var estimateText = String.format(Locale.getDefault(), "Maturity: %.0f%% (%d years left)", maturityPercent, yearsLeft)

        // Distance logic (for Map view)
        distances?.let {
            if (position < it.size) {
                estimateText = String.format(Locale.getDefault(), "Dist: %.1f m | %s", it[position], estimateText)
            }
        }
        
        holder.binding.tvMaturityEstimate.text = estimateText
        holder.binding.pbMaturity.progress = maturityPercent.toInt()

        if (tree.photoPath.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(Uri.parse(tree.photoPath))
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.binding.ivTreeThumbnail)
        }

        holder.itemView.setOnClickListener {
            listener?.onTreeClick(tree)
        }
    }

    override fun getItemCount(): Int = trees.size

    class TreeViewHolder(val binding: ItemTreeBinding) : RecyclerView.ViewHolder(binding.root)
}
