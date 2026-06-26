package com.example.data

import kotlinx.coroutines.flow.Flow

class MediaRepository(private val mediaFileDao: MediaFileDao) {
    val allMediaFiles: Flow<List<MediaFile>> = mediaFileDao.getAllMediaFiles()
    val favoriteMediaFiles: Flow<List<MediaFile>> = mediaFileDao.getFavoriteMediaFiles()

    suspend fun insert(mediaFile: MediaFile) {
        mediaFileDao.insertMediaFile(mediaFile)
    }

    suspend fun update(mediaFile: MediaFile) {
        mediaFileDao.updateMediaFile(mediaFile)
    }

    suspend fun updateFavorite(id: Int, isFavorite: Boolean) {
        mediaFileDao.updateFavorite(id, isFavorite)
    }

    suspend fun rename(id: Int, newName: String, newPath: String) {
        mediaFileDao.renameMediaFile(id, newName, newPath)
    }

    suspend fun deleteById(id: Int) {
        mediaFileDao.deleteMediaFileById(id)
    }

    suspend fun deleteAll() {
        mediaFileDao.deleteAllMediaFiles()
    }
}
