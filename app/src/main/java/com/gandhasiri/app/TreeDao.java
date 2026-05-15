package com.gandhasiri.app;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface TreeDao {
    @Insert
    long insert(Tree tree);

    @Update
    void update(Tree tree);

    @Delete
    void delete(Tree tree);

    @Query("SELECT * FROM trees ORDER BY datePlanted DESC")
    LiveData<List<Tree>> getAllTrees();

    @Query("SELECT * FROM trees WHERE id = :id")
    LiveData<Tree> getTreeById(int id);

    @Query("SELECT * FROM trees WHERE id = :id")
    Tree getTreeByIdSync(int id);

    @Insert
    void insertGrowthLog(GrowthLog log);

    @Query("SELECT * FROM growth_logs WHERE treeId = :treeId ORDER BY timestamp DESC")
    LiveData<List<GrowthLog>> getGrowthLogsForTree(int treeId);
}
