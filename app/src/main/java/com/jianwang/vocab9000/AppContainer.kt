package com.jianwang.vocab9000

import android.content.Context
import com.jianwang.vocab9000.data.db.AppDatabase
import com.jianwang.vocab9000.data.repository.CatalogStore
import com.jianwang.vocab9000.data.repository.DefinitionService
import com.jianwang.vocab9000.data.repository.SeedService
import com.jianwang.vocab9000.data.repository.SpeechService
import com.jianwang.vocab9000.data.repository.StatsService
import com.jianwang.vocab9000.util.AppPreferences

class AppContainer(context: Context) {
    val appContext = context.applicationContext
    val db = AppDatabase.get(appContext)
    val catalog = CatalogStore.get(appContext)
    val prefs = AppPreferences(appContext)
    val seed = SeedService(appContext, db, catalog)
    val definitions = DefinitionService(db.definitionCacheDao())
    val speech = SpeechService(appContext).also { it.setLanguage(prefs.ttsLanguage) }
    val stats = StatsService(db, catalog, seed)
}
