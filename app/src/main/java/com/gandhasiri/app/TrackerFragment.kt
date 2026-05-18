package com.gandhasiri.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.gandhasiri.app.databinding.FragmentTrackerBinding

class TrackerFragment : Fragment() {
    private var _binding: FragmentTrackerBinding? = null
    private val binding get() = _binding!!
    private lateinit var treeViewModel: TreeViewModel
    private lateinit var adapter: TreeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrackerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        adapter = TreeAdapter()
        binding.rvTrees.adapter = adapter

        adapter.setOnTreeClickListener { tree ->
            val bundle = Bundle().apply {
                putInt("treeId", tree.id)
                putString("treeUid", tree.treeId)
                putString("photoPath", tree.photoPath)
                putFloat("girth", tree.girth.toFloat())
                putFloat("lat", tree.latitude.toFloat())
                putFloat("lng", tree.longitude.toFloat())
            }
            findNavController().navigate(R.id.navigation_tree_detail, bundle)
        }

        treeViewModel = ViewModelProvider(this).get(TreeViewModel::class.java)
        treeViewModel.allTrees.observe(viewLifecycleOwner) { trees ->
            trees?.let {
                adapter.setTrees(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
