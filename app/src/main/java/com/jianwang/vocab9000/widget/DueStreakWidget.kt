package com.jianwang.vocab9000.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.jianwang.vocab9000.MainActivity
import com.jianwang.vocab9000.R
import com.jianwang.vocab9000.data.repository.WidgetSnapshot
import com.jianwang.vocab9000.util.AppLocale
import com.jianwang.vocab9000.ui.theme.BrandIndigo

class DueStreakWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val localized = AppLocale.wrap(context)
        var snapshot = WidgetSnapshotStore.load(localized)
        if (snapshot.updatedAt == 0L) {
            snapshot = WidgetDataPublisher.publish(localized)
        }
        val studyIntent = Intent(localized, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(STUDY_DEEP_LINK)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        provideContent {
            GlanceTheme(colors = Vocab9000GlanceColors) {
                DueStreakWidgetContent(
                    context = localized,
                    snapshot = snapshot,
                    studyIntent = studyIntent,
                )
            }
        }
    }

    companion object {
        const val STUDY_DEEP_LINK = "vocab9000://study"
    }
}

private val Vocab9000GlanceColors = ColorProviders(
    light = lightColorScheme(
        primary = BrandIndigo,
        secondary = Color(0xFF6366F1),
        background = Color(0xFFEEF2FF),
        surface = Color(0xFFEEF2FF),
        onPrimary = Color.White,
        onBackground = Color(0xFF1E1B4B),
        onSurface = Color(0xFF1E1B4B),
        onSurfaceVariant = Color(0xFF475569),
    ),
    dark = darkColorScheme(
        primary = Color(0xFF818CF8),
        secondary = Color(0xFFA5B4FC),
        background = Color(0xFF1E1B4B),
        surface = Color(0xFF1E1B4B),
        onPrimary = Color(0xFF1E1B4B),
        onBackground = Color(0xFFEEF2FF),
        onSurface = Color(0xFFEEF2FF),
        onSurfaceVariant = Color(0xFFC7D2FE),
    ),
)

@Composable
private fun DueStreakWidgetContent(
    context: Context,
    snapshot: WidgetSnapshot,
    studyIntent: Intent,
) {
    val a11y = context.getString(
        R.string.widget_a11y,
        snapshot.dueToday,
        snapshot.streakDays,
        snapshot.learnedCount,
    )
    val wide = LocalSize.current.width >= 180.dp
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .cornerRadius(16.dp)
            .background(GlanceTheme.colors.background)
            .clickable(onClick = actionStartActivity(studyIntent))
            .semantics { contentDescription = a11y }
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                provider = ImageProvider(R.drawable.ic_notification),
                contentDescription = null,
                modifier = GlanceModifier.size(16.dp),
            )
            Spacer(modifier = GlanceModifier.width(6.dp))
            Text(
                text = context.getString(R.string.app_name),
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
        Spacer(modifier = GlanceModifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = snapshot.dueToday.toString(),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = GlanceModifier.width(4.dp))
            Text(
                text = context.getString(R.string.widget_due_label),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp,
                ),
            )
        }
        Spacer(modifier = GlanceModifier.height(8.dp))
        val streak = context.getString(R.string.widget_streak, snapshot.streakDays)
        val learned = context.getString(R.string.widget_learned, snapshot.learnedCount)
        val metaStyle = TextStyle(
            color = GlanceTheme.colors.onSurfaceVariant,
            fontSize = 11.sp,
        )
        if (wide) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = streak, style = metaStyle)
                Spacer(modifier = GlanceModifier.width(12.dp))
                Text(text = learned, style = metaStyle)
            }
        } else {
            Column {
                Text(text = streak, style = metaStyle)
                Text(text = learned, style = metaStyle)
            }
        }
    }
}

class DueStreakWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DueStreakWidget()
}
