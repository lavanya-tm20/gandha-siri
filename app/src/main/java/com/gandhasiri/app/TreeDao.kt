package com.gandhasiri.app

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TreeDao {
    @Insert
    fun insert(tree: Tree): Long

    @Update
    fun update(tree: Tree)

    @Delete
    fun delete(tree: Tree)

    @Query("SELECT * FROM trees")
    fun getAllTrees(): LiveData<List<Tree>>

    @Query("SELECT * FROM trees WHERE id = :id")
    fun getTreeById(id: Int): LiveData<Tree>

    @Query("SELECT * FROM trees WHERE id = :id")
    fun getTreeByIdSync(id: Int): Tree?

    @Insert
    fun insertGrowthLog(log: GrowthLog)

    @Query("SELECT * FROM growth_logs WHERE treeId = :treeId ORDER BY timestamp DESC")
    fun getGrowthLogsForTree(treeId: Int): LiveData<List<GrowthLog>>
}
