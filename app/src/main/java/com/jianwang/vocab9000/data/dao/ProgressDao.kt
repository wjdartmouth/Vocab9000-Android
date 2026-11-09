package com.jianwang.vocab9000.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jianwang.vocab9000.data.entity.ProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Query("SELECT * FROM progress")
    fun observeAll(): Flow<List<ProgressEntity>>

    @Query("SELECT * FROM progress")
    suspend fun getAll(): List<ProgressEntity>

    @Query("SELECT * FROM progress WHERE catalogKey = :key LIMIT 1")
    suspend fun getByKey(key: String): ProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: ProgressEntity)

    @Update
    suspend fun update(row: ProgressEntity)

    @Query("SELECT COUNT(*) FROM progress WHERE isLearned = 1")
    suspend fun learnedCount(): Int
}
