package com.gandhasiri.app;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "growth_logs",
        foreignKeys = @ForeignKey(entity = Tree.class,
                parentColumns = "id",
                childColumns = "treeId",
                onDelete = ForeignKey.CASCADE))
public class GrowthLog {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int treeId;
    public double girth;
    public long timestamp;
    public String healthStatus;

    public GrowthLog(int treeId, double girth, long timestamp, String healthStatus) {
        this.treeId = treeId;
        this.girth = girth;
        this.timestamp = timestamp;
        this.healthStatus = healthStatus;
    }
}
