package com.jianwang.vocab9000.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.jianwang.vocab9000.AppContainer
import com.jianwang.vocab9000.Vocab9000App
import com.jianwang.vocab9000.data.repository.WidgetSnapshot
import com.jianwang.vocab9000.util.AppLocale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Publishes due / streak / learned counts for the home-screen widget.
 * Uses [com.jianwang.vocab9000.data.repository.StatsService] Progress aggregation
 * (the All deck is never materialized as 9000 FlashCards).
 */
object WidgetDataPublisher {
    const val PERIODIC_WORK = "vocab9000.widget.refresh"
    private const val ONE_SHOT_WORK = "vocab9000.widget.refresh.now"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    suspend fun publish(context: Context): WidgetSnapshot {
        val appContext = context.applicationContext
        val snapshot = try {
            val stats = (appContext as? Vocab9000App)?.container?.stats
                ?: AppContainer(appContext).stats
            stats.widgetSnapshot()
        } catch (_: Exception) {
            WidgetSnapshotStore.load(appContext)
        }
        WidgetSnapshotStore.save(appContext, snapshot)
        DueStreakWidget().updateAll(AppLocale.wrap(appContext))
        return snapshot
    }

    /** Fire-and-forget refresh from UI or after an SRS write. */
    fun requestPublish(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            try {
                publish(appContext)
            } catch (_: Exception) {
                enqueueOneShot(appContext)
            }
        }
    }

    fun ensurePeriodic(context: Context) {
        val appContext = context.applicationContext
        val periodic = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(30, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().build())
            .build()
        WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
            PERIODIC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            periodic,
        )
    }

    private fun enqueueOneShot(context: Context) {
        val request = OneTimeWorkRequestBuilder<WidgetRefreshWorker>().build()
        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            ONE_SHOT_WORK,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }
}

class WidgetRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            WidgetDataPublisher.publish(applicationContext)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
