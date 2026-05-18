package com.gandhasiri.app

import android.app.Application
import androidx.lifecycle.LiveData
import java.util.concurrent.Executors

class TreeRepository(application: Application) {
    private val treeDao: TreeDao
    private val allTrees: LiveData<List<Tree>>
    private val executorService = Executors.newFixedThreadPool(4)

    init {
        val db = AppDatabase.getDatabase(application)
        treeDao = db.treeDao()
        allTrees = treeDao.getAllTrees()
    }

    fun getAllTrees(): LiveData<List<Tree>> = allTrees

    fun getTreeById(id: Int): LiveData<Tree> = treeDao.getTreeById(id)

    fun insertWithLog(tree: Tree, initialGirth: Double) {
        executorService.execute {
            val id = treeDao.insert(tree)
            treeDao.insertGrowthLog(GrowthLog(treeId = id.toInt(), girth = initialGirth, timestamp = System.currentTimeMillis(), healthStatus = "Healthy"))
        }
    }

    fun updateTreeWithLog(tree: Tree, newGirth: Double) {
        executorService.execute {
            tree.girth = newGirth
            treeDao.update(tree)
            treeDao.insertGrowthLog(GrowthLog(treeId = tree.id, girth = newGirth, timestamp = System.currentTimeMillis(), healthStatus = "Healthy"))
        }
    }

    fun delete(tree: Tree) {
        executorService.execute { treeDao.delete(tree) }
    }

    fun getGrowthLogs(treeId: Int): LiveData<List<GrowthLog>> = treeDao.getGrowthLogsForTree(treeId)
}
