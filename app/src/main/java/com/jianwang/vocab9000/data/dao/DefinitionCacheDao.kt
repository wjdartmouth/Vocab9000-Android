package com.jianwang.vocab9000.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jianwang.vocab9000.data.entity.DefinitionCacheEntity

@Dao
interface DefinitionCacheDao {
    @Query("SELECT * FROM definition_cache WHERE word = :word LIMIT 1")
    suspend fun get(word: String): DefinitionCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: DefinitionCacheEntity)
}
