package com.jianwang.vocab9000

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jianwang.vocab9000.ui.VocabRoot
import com.jianwang.vocab9000.ui.theme.Vocab9000Theme

class MainActivity : ComponentActivity() {
    private var openStudy by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleDeepLink(intent)
        val app = application as Vocab9000App
        setContent {
            Vocab9000Theme {
                VocabRoot(
                    container = app.container,
                    openStudyTab = openStudy,
                    onStudyTabConsumed = { openStudy = false },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == "vocab9000" && (uri.host == "study" || uri.path?.contains("study") == true)) {
            openStudy = true
        }
    }
}
