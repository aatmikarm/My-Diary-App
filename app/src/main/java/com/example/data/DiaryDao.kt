package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary_entries ORDER BY dateMillis DESC, createdMillis DESC")
    fun getAllEntries(): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries ORDER BY dateMillis DESC, createdMillis DESC LIMIT :limit")
    fun getRecentEntries(limit: Int = 4): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries WHERE id = :id")
    fun getEntryById(id: Long): Flow<DiaryEntry?>

    @Query("SELECT * FROM diary_entries WHERE id = :id")
    suspend fun getEntryByIdSync(id: Long): DiaryEntry?

    @Query("SELECT * FROM diary_entries WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR tagsJson LIKE '%' || :query || '%' OR mood LIKE '%' || :query || '%' ORDER BY dateMillis DESC")
    fun searchEntries(query: String): Flow<List<DiaryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DiaryEntry): Long

    @Update
    suspend fun updateEntry(entry: DiaryEntry)

    @Delete
    suspend fun deleteEntry(entry: DiaryEntry)

    @Query("DELETE FROM diary_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    @Query("DELETE FROM diary_entries")
    suspend fun clearAll()
}
