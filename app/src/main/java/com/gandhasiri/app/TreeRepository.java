package com.gandhasiri.app;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TreeRepository {
    private TreeDao treeDao;
    private LiveData<List<Tree>> allTrees;
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public TreeRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        treeDao = db.treeDao();
        allTrees = treeDao.getAllTrees();
    }

    public LiveData<List<Tree>> getAllTrees() {
        return allTrees;
    }

    public LiveData<Tree> getTreeById(int id) {
        return treeDao.getTreeById(id);
    }

    public void insertWithLog(Tree tree, double initialGirth) {
        executorService.execute(() -> {
            long id = treeDao.insert(tree);
            treeDao.insertGrowthLog(new GrowthLog((int)id, initialGirth, System.currentTimeMillis(), "Healthy"));
        });
    }

    public void updateTreeWithLog(Tree tree, double newGirth) {
        executorService.execute(() -> {
            tree.setGirth(newGirth);
            treeDao.update(tree);
            treeDao.insertGrowthLog(new GrowthLog(tree.getId(), newGirth, System.currentTimeMillis(), "Healthy"));
        });
    }

    public void delete(Tree tree) {
        executorService.execute(() -> treeDao.delete(tree));
    }

    public LiveData<List<GrowthLog>> getGrowthLogs(int treeId) {
        return treeDao.getGrowthLogsForTree(treeId);
    }
}
