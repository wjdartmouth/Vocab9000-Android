package com.jianwang.vocab9000.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jianwang.vocab9000.data.dao.DeckDao
import com.jianwang.vocab9000.data.dao.DefinitionCacheDao
import com.jianwang.vocab9000.data.dao.FlashCardDao
import com.jianwang.vocab9000.data.dao.ProgressDao
import com.jianwang.vocab9000.data.entity.DeckEntity
import com.jianwang.vocab9000.data.entity.DefinitionCacheEntity
import com.jianwang.vocab9000.data.entity.FlashCardEntity
import com.jianwang.vocab9000.data.entity.ProgressEntity

@Database(
    entities = [
        DeckEntity::class,
        FlashCardEntity::class,
        ProgressEntity::class,
        DefinitionCacheEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deckDao(): DeckDao
    abstract fun flashCardDao(): FlashCardDao
    abstract fun progressDao(): ProgressDao
    abstract fun definitionCacheDao(): DefinitionCacheDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vocab9000.db",
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
