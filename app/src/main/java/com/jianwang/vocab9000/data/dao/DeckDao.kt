package com.jianwang.vocab9000.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jianwang.vocab9000.data.entity.DeckEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {
    @Query("SELECT * FROM decks ORDER BY isBuiltIn DESC, createdAt ASC")
    fun observeAll(): Flow<List<DeckEntity>>

    @Query("SELECT * FROM decks ORDER BY isBuiltIn DESC, createdAt ASC")
    suspend fun getAll(): List<DeckEntity>

    @Query("SELECT * FROM decks WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): DeckEntity?

    @Query("SELECT * FROM decks WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<DeckEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(deck: DeckEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(decks: List<DeckEntity>)

    @Update
    suspend fun update(deck: DeckEntity)

    @Delete
    suspend fun delete(deck: DeckEntity)

    @Query("SELECT COUNT(*) FROM decks")
    suspend fun count(): Int
}
