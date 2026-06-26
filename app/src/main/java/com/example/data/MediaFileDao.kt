package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaFileDao {
    @Query("SELECT * FROM media_files ORDER BY timestamp DESC")
    fun getAllMediaFiles(): Flow<List<MediaFile>>

    @Query("SELECT * FROM media_files WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteMediaFiles(): Flow<List<MediaFile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaFile(mediaFile: MediaFile)

    @Update
    suspend fun updateMediaFile(mediaFile: MediaFile)

    @Query("UPDATE media_files SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavorite(id: Int, isFav: Boolean)

    @Query("UPDATE media_files SET fileName = :newName, filePath = :newPath WHERE id = :id")
    suspend fun renameMediaFile(id: Int, newName: String, newPath: String)

    @Query("DELETE FROM media_files WHERE id = :id")
    suspend fun deleteMediaFileById(id: Int)

    @Query("DELETE FROM media_files")
    suspend fun deleteAllMediaFiles()
}
