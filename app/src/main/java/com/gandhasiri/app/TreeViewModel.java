package com.gandhasiri.app;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class TreeViewModel extends AndroidViewModel {
    private TreeRepository repository;
    private LiveData<List<Tree>> allTrees;

    public TreeViewModel(@NonNull Application application) {
        super(application);
        repository = new TreeRepository(application);
        allTrees = repository.getAllTrees();
    }

    public LiveData<List<Tree>> getAllTrees() {
        return allTrees;
    }

    public LiveData<Tree> getTreeById(int id) {
        return repository.getTreeById(id);
    }

    public void insert(Tree tree, double initialGirth) {
        repository.insertWithLog(tree, initialGirth);
    }

    public void updateTreeGirth(Tree tree, double newGirth) {
        repository.updateTreeWithLog(tree, newGirth);
    }

    public void delete(Tree tree) {
        repository.delete(tree);
    }

    public LiveData<List<GrowthLog>> getGrowthLogs(int treeId) {
        return repository.getGrowthLogs(treeId);
    }
}
