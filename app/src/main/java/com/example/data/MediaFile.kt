package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_files")
data class MediaFile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val filePath: String,
    val fileName: String,
    val fileType: String, // "VIDEO" or "IMAGE"
    val durationMs: Long = 0,
    val fileSize: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val resolution: String = "1080p",
    val fps: Int = 60,
    val bitrate: Int = 8000
)
