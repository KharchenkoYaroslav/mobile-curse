package ua.kpi.transformermonitoringsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import ua.kpi.transformermonitoringsystem.ui.AuthUiState
import ua.kpi.transformermonitoringsystem.ui.AccountViewModel
import ua.kpi.transformermonitoringsystem.ui.TransformerViewModel
import ua.kpi.transformermonitoringsystem.ui.navigation.NavScreen
import ua.kpi.transformermonitoringsystem.ui.screens.AuthScreen
import ua.kpi.transformermonitoringsystem.ui.screens.TransformerDetailsScreen
import ua.kpi.transformermonitoringsystem.ui.screens.TransformerFormScreen
import ua.kpi.transformermonitoringsystem.ui.screens.TransformersScreen
import ua.kpi.transformermonitoringsystem.ui.theme.TransformerMonitoringSystemTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: TransformerViewModel by viewModels()
    private val accountViewModel: AccountViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TransformerMonitoringSystemTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authState by accountViewModel.authState.collectAsState()

                    when (authState) {
                        AuthUiState.Initializing -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        AuthUiState.Unauthenticated -> {
                            AuthScreen(accountViewModel = accountViewModel)
                        }

                        AuthUiState.Authenticated -> {
                            val navController = rememberNavController()
                            NavHost(
                                navController = navController,
                                startDestination = NavScreen.List.route
                            ) {
                                composable(NavScreen.List.route) {
                                    TransformersScreen(
                                        viewModel = viewModel,
                                        accountViewModel = accountViewModel,
                                        onNavigateToForm = { navController.navigate(NavScreen.Form.route) },
                                        onNavigateToDetails = { id ->
                                            navController.navigate(NavScreen.Details.createRoute(id))
                                        }
                                    )
                                }
                                composable(NavScreen.Form.route) {
                                    TransformerFormScreen(
                                        navController = navController,
                                        viewModel = viewModel
                                    )
                                }
                                composable(
                                    route = NavScreen.Details.route,
                                    arguments = listOf(navArgument("transformerId") { type = NavType.IntType })
                                ) { backStackEntry ->
                                    val id = backStackEntry.arguments?.getInt("transformerId") ?: 0
                                    TransformerDetailsScreen(
                                        transformerId = id,
                                        navController = navController,
                                        viewModel = viewModel,
                                        accountViewModel = accountViewModel
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
