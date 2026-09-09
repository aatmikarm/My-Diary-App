package com.aatmik.mydiary.data

import kotlinx.coroutines.flow.Flow

class DiaryRepository(private val diaryDao: DiaryDao) {
    val allEntries: Flow<List<DiaryEntry>> = diaryDao.getAllEntries()
    val recentEntries: Flow<List<DiaryEntry>> = diaryDao.getRecentEntries(4)

    fun getEntryById(id: Long): Flow<DiaryEntry?> = diaryDao.getEntryById(id)

    suspend fun getEntryByIdSync(id: Long): DiaryEntry? = diaryDao.getEntryByIdSync(id)

    fun search(query: String): Flow<List<DiaryEntry>> = diaryDao.searchEntries(query)

    suspend fun insert(entry: DiaryEntry): Long = diaryDao.insertEntry(entry)

    suspend fun update(entry: DiaryEntry) = diaryDao.updateEntry(entry)

    suspend fun delete(entry: DiaryEntry) = diaryDao.deleteEntry(entry)

    suspend fun deleteById(id: Long) = diaryDao.deleteEntryById(id)

    suspend fun clearAll() = diaryDao.clearAll()
}
