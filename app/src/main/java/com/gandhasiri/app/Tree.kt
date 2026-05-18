package com.gandhasiri.app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trees")
data class Tree(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var treeId: String, // Unique Tree ID
    var photoPath: String,
    var latitude: Double,
    var longitude: Double,
    var girth: Double, // in cm
    var datePlanted: Long, // timestamp
    var healthStatus: String
)
