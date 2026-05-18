package com.gandhasiri.app

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "growth_logs",
    foreignKeys = [
        ForeignKey(
            entity = Tree::class,
            parentColumns = ["id"],
            childColumns = ["treeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GrowthLog(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var treeId: Int,
    var girth: Double,
    var timestamp: Long,
    var healthStatus: String
)
