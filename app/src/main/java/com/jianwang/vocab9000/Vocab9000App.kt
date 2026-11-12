package com.jianwang.vocab9000

import android.app.Application
import android.content.Context
import com.jianwang.vocab9000.util.AppLocale
import com.jianwang.vocab9000.util.NotificationHelper

class Vocab9000App : Application() {
    lateinit var container: AppContainer
        private set

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(AppLocale.wrap(base))
    }

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.ensureChannel(this)
    }
}
