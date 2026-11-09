package com.jianwang.vocab9000.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jianwang.vocab9000.AppContainer
import com.jianwang.vocab9000.R
import com.jianwang.vocab9000.ui.catalog.CatalogScreen
import com.jianwang.vocab9000.ui.catalog.WordDetailScreen
import com.jianwang.vocab9000.ui.decks.CardEditorScreen
import com.jianwang.vocab9000.ui.decks.DeckDetailScreen
import com.jianwang.vocab9000.ui.decks.DeckListScreen
import com.jianwang.vocab9000.ui.settings.SettingsScreen
import com.jianwang.vocab9000.ui.stats.StatsScreen
import com.jianwang.vocab9000.ui.study.StudyHubScreen
import com.jianwang.vocab9000.ui.study.StudySessionScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private sealed class Tab(val route: String, val labelRes: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    data object Catalog : Tab("tab_catalog", R.string.tab_catalog, Icons.Filled.MenuBook)
    data object Decks : Tab("tab_decks", R.string.tab_decks, Icons.Filled.Layers)
    data object Study : Tab("tab_study", R.string.tab_study, Icons.Filled.School)
    data object Stats : Tab("tab_stats", R.string.tab_stats, Icons.Filled.BarChart)
    data object Settings : Tab("tab_settings", R.string.tab_settings, Icons.Filled.Settings)
}

@Composable
fun VocabRoot(
    container: AppContainer,
    openStudyTab: Boolean,
    onStudyTabConsumed: () -> Unit,
) {
    val tabs = listOf(Tab.Catalog, Tab.Decks, Tab.Study, Tab.Stats, Tab.Settings)
    var selected by remember { mutableIntStateOf(0) }
    val navControllers = listOf(
        rememberNavController(),
        rememberNavController(),
        rememberNavController(),
        rememberNavController(),
        rememberNavController(),
    )

    LaunchedEffect(Unit) {
        container.catalog.loadIfNeeded()
        container.seed.seedIfNeeded()
        container.seed.migrateSRSIfNeeded()
    }

    LaunchedEffect(openStudyTab) {
        if (openStudyTab) {
            selected = 2
            onStudyTabConsumed()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selected == index,
                        onClick = { selected = index },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(stringResource(tab.labelRes)) },
                    )
                }
            }
        }
    ) { padding ->
        when (selected) {
            0 -> {
                val nav = navControllers[0]
                NavHost(
                    navController = nav,
                    startDestination = "catalog",
                    modifier = Modifier.padding(padding),
                ) {
                    composable("catalog") {
                        CatalogScreen(
                            container = container,
                            onOpenWord = { key ->
                                val enc = URLEncoder.encode(key, StandardCharsets.UTF_8.toString())
                                nav.navigate("word/$enc")
                            },
                        )
                    }
                    composable(
                        "word/{key}",
                        arguments = listOf(navArgument("key") { type = NavType.StringType }),
                    ) { entry ->
                        val raw = entry.arguments?.getString("key").orEmpty()
                        val key = URLDecoder.decode(raw, StandardCharsets.UTF_8.toString())
                        WordDetailScreen(
                            container = container,
                            catalogKey = key,
                            onBack = { nav.popBackStack() },
                        )
                    }
                }
            }
            1 -> {
                val nav = navControllers[1]
                NavHost(
                    navController = nav,
                    startDestination = "decks",
                    modifier = Modifier.padding(padding),
                ) {
                    composable("decks") {
                        DeckListScreen(
                            container = container,
                            onOpenDeck = { id -> nav.navigate("deck/$id") },
                        )
                    }
                    composable(
                        "deck/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.StringType }),
                    ) { entry ->
                        val id = entry.arguments?.getString("id").orEmpty()
                        DeckDetailScreen(
                            container = container,
                            deckId = id,
                            onBack = { nav.popBackStack() },
                            onStudy = { nav.navigate("study/$id") },
                            onEditCard = { cardId ->
                                nav.navigate("card/$id/${cardId ?: "new"}")
                            },
                        )
                    }
                    composable(
                        "study/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.StringType }),
                    ) { entry ->
                        val id = entry.arguments?.getString("id").orEmpty()
                        StudySessionScreen(
                            container = container,
                            deckId = id,
                            onDone = { nav.popBackStack() },
                        )
                    }
                    composable(
                        "card/{deckId}/{cardId}",
                        arguments = listOf(
                            navArgument("deckId") { type = NavType.StringType },
                            navArgument("cardId") { type = NavType.StringType },
                        ),
                    ) { entry ->
                        val deckId = entry.arguments?.getString("deckId").orEmpty()
                        val cardId = entry.arguments?.getString("cardId")
                        CardEditorScreen(
                            container = container,
                            deckId = deckId,
                            cardId = cardId?.takeIf { it != "new" },
                            onDone = { nav.popBackStack() },
                        )
                    }
                }
            }
            2 -> {
                val nav = navControllers[2]
                NavHost(
                    navController = nav,
                    startDestination = "study_hub",
                    modifier = Modifier.padding(padding),
                ) {
                    composable("study_hub") {
                        StudyHubScreen(
                            container = container,
                            onStart = { id -> nav.navigate("session/$id") },
                        )
                    }
                    composable(
                        "session/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.StringType }),
                    ) { entry ->
                        val id = entry.arguments?.getString("id").orEmpty()
                        StudySessionScreen(
                            container = container,
                            deckId = id,
                            onDone = { nav.popBackStack() },
                        )
                    }
                }
            }
            3 -> StatsScreen(container = container, modifier = Modifier.padding(padding))
            4 -> SettingsScreen(container = container, modifier = Modifier.padding(padding))
        }
    }
}
