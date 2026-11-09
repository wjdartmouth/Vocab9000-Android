package com.jianwang.vocab9000.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jianwang.vocab9000.data.entity.FlashCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashCardDao {
    @Query("SELECT * FROM flash_cards WHERE deckId = :deckId ORDER BY front COLLATE NOCASE ASC")
    fun observeForDeck(deckId: String): Flow<List<FlashCardEntity>>

    @Query("SELECT * FROM flash_cards WHERE deckId = :deckId ORDER BY front COLLATE NOCASE ASC")
    suspend fun getForDeck(deckId: String): List<FlashCardEntity>

    @Query("SELECT * FROM flash_cards WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): FlashCardEntity?

    @Query("SELECT * FROM flash_cards")
    suspend fun getAll(): List<FlashCardEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(card: FlashCardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(cards: List<FlashCardEntity>)

    @Update
    suspend fun update(card: FlashCardEntity)

    @Delete
    suspend fun delete(card: FlashCardEntity)

    @Query("DELETE FROM flash_cards WHERE id = :id")
    suspend fun deleteById(id: String)
}
