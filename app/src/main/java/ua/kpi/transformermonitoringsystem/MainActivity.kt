package ua.kpi.transformermonitoringsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import ua.kpi.transformermonitoringsystem.data.local.AppDatabase
import ua.kpi.transformermonitoringsystem.ui.navigation.NavScreen
import ua.kpi.transformermonitoringsystem.data.TransformerRepository
import ua.kpi.transformermonitoringsystem.ui.screens.MainMenuScreen
import ua.kpi.transformermonitoringsystem.ui.screens.TransformerDetailsScreen
import ua.kpi.transformermonitoringsystem.ui.screens.TransformerFormScreen
import ua.kpi.transformermonitoringsystem.ui.screens.TransformerListScreen
import ua.kpi.transformermonitoringsystem.ui.theme.TransformerMonitoringSystemTheme
import ua.kpi.transformermonitoringsystem.ui.TransformerViewModel
import ua.kpi.transformermonitoringsystem.ui.TransformerViewModelFactory

class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "transformer-db"
        ).build()
    }

    private val repository by lazy {
        TransformerRepository(db.transformerDao())
    }

    private val viewModel: TransformerViewModel by viewModels {
        TransformerViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TransformerMonitoringSystemTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = NavScreen.MainMenu.route) {
                        composable(NavScreen.MainMenu.route) {
                            MainMenuScreen(navController)
                        }
                        composable(NavScreen.List.route) {
                            TransformerListScreen(navController, viewModel)
                        }
                        composable(NavScreen.Form.route) {
                            TransformerFormScreen(navController, viewModel)
                        }
                        composable(
                            route = NavScreen.Details.route,
                            arguments = listOf(navArgument("transformerId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getInt("transformerId") ?: 0
                            TransformerDetailsScreen(id, navController, viewModel)
                        }
                    }
                }
            }
        }
    }
}
