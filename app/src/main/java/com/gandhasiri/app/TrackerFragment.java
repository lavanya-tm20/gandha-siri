package com.gandhasiri.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.gandhasiri.app.databinding.FragmentTrackerBinding;

public class TrackerFragment extends Fragment {
    private FragmentTrackerBinding binding;
    private TreeViewModel treeViewModel;
    private TreeAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTrackerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        adapter = new TreeAdapter();
        binding.rvTrees.setAdapter(adapter);

        adapter.setOnTreeClickListener(tree -> {
            Bundle bundle = new Bundle();
            bundle.putInt("treeId", tree.getId());
            bundle.putString("treeUid", tree.getTreeId());
            bundle.putString("photoPath", tree.getPhotoPath());
            bundle.putFloat("girth", (float) tree.getGirth());
            bundle.putFloat("lat", (float) tree.getLatitude());
            bundle.putFloat("lng", (float) tree.getLongitude());
            
            Navigation.findNavController(view).navigate(R.id.navigation_tree_detail, bundle);
        });

        treeViewModel = new ViewModelProvider(this).get(TreeViewModel.class);
        treeViewModel.getAllTrees().observe(getViewLifecycleOwner(), trees -> {
            if (trees != null) {
                adapter.setTrees(trees);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
