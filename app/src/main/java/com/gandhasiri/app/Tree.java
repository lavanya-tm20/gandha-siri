package com.gandhasiri.app;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "trees")
public class Tree {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String treeId; // Unique Tree ID
    private String photoPath;
    private double latitude;
    private double longitude;
    private double girth; // in cm
    private long datePlanted; // timestamp
    private String healthStatus;

    public Tree(String treeId, String photoPath, double latitude, double longitude, double girth, long datePlanted, String healthStatus) {
        this.treeId = treeId;
        this.photoPath = photoPath;
        this.latitude = latitude;
        this.longitude = longitude;
        this.girth = girth;
        this.datePlanted = datePlanted;
        this.healthStatus = healthStatus;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTreeId() { return treeId; }
    public void setTreeId(String treeId) { this.treeId = treeId; }
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public double getGirth() { return girth; }
    public void setGirth(double girth) { this.girth = girth; }
    public long getDatePlanted() { return datePlanted; }
    public void setDatePlanted(long datePlanted) { this.datePlanted = datePlanted; }
    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }
}
