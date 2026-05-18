package com.gandhasiri.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class TreeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TreeRepository = TreeRepository(application)
    val allTrees: LiveData<List<Tree>> = repository.getAllTrees()

    fun getTreeById(id: Int): LiveData<Tree> = repository.getTreeById(id)

    fun insert(tree: Tree, initialGirth: Double) {
        repository.insertWithLog(tree, initialGirth)
    }

    fun updateTreeGirth(tree: Tree, newGirth: Double) {
        repository.updateTreeWithLog(tree, newGirth)
    }

    fun delete(tree: Tree) {
        repository.delete(tree)
    }

    fun getGrowthLogs(treeId: Int): LiveData<List<GrowthLog>> = repository.getGrowthLogs(treeId)
}
