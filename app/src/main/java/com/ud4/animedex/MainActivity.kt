package com.ud4.animedex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ud4.animedex.data.database.AnimeDatabase
import com.ud4.animedex.data.repository.SettingsRepository
import com.ud4.animedex.ui.screens.CatalogScreen
import com.ud4.animedex.ui.screens.DetailScreen
import com.ud4.animedex.ui.screens.FavoritesScreen
import com.ud4.animedex.ui.screens.SettingsScreen
import com.ud4.animedex.ui.theme.AnimeDexTheme
import com.ud4.animedex.viewmodel.CatalogViewModel
import com.ud4.animedex.viewmodel.DetailViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnimeDexApp()
        }
    }
}

@Composable
fun AnimeDexApp() {
    val app = LocalContext.current.applicationContext

    val catalogViewModel: CatalogViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val db = AnimeDatabase.get(app)
                val settingsRepo = SettingsRepository(app)
                CatalogViewModel(db.animeDao(), settingsRepo)
            }
        }
    )

    val settings by catalogViewModel.settings.collectAsStateWithLifecycle()

    AnimeDexTheme(darkTheme = settings.darkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "catalog"
            ) {
                composable("catalog") {
                    CatalogScreen(
                        viewModel = catalogViewModel,
                        onAnimeClick = { animeId ->
                            navController.navigate("detail/$animeId")
                        },
                        onNavigateToSettings = {
                            navController.navigate("settings")
                        },
                        onNavigateToFavorites = {
                            navController.navigate("favorites")
                        }
                    )
                }

                composable("favorites") {
                    FavoritesScreen(
                        viewModel = catalogViewModel,
                        onAnimeClick = { animeId ->
                            navController.navigate("detail/$animeId")
                        },
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable("settings") {
                    SettingsScreen(
                        viewModel = catalogViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = "detail/{id}",
                    arguments = listOf(
                        navArgument("id") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val animeId = backStackEntry.arguments?.getInt("id") ?: 0

                    val detailViewModel: DetailViewModel = viewModel(
                        key = "detail_$animeId",
                        factory = viewModelFactory {
                            initializer {
                                val db = AnimeDatabase.get(app)
                                val settingsRepo = SettingsRepository(app)
                                DetailViewModel(animeId, db.animeDao(), settingsRepo)
                            }
                        }
                    )

                    DetailScreen(
                        viewModel = detailViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
