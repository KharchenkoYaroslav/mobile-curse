package ua.kpi.transformermonitoringsystem.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ua.kpi.transformermonitoringsystem.ui.navigation.NavScreen

// Головний екран застосунку, що відповідає за стартову навігацію
@Composable
fun MainMenuScreen(navController: NavHostController) {
    // Контейнер для адаптивного дизайну залежно від ширини екрану
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth > 600.dp // Прапорець широкого екрану

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Система моніторингу трансформаторів", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(32.dp))

            // Адаптивне розташування елементів (Row для планшетів, Column для смартфонів)
            if (isWide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { navController.navigate(NavScreen.List.route) },
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        Text("Список трансформаторів")
                    }
                    Button(
                        onClick = { navController.navigate(NavScreen.Form.route) },
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    ) {
                        Text("Додати трансформатор")
                    }
                }
            } else {
                Button(
                    onClick = { navController.navigate(NavScreen.List.route) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Список трансформаторів")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate(NavScreen.Form.route) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Додати трансформатор")
                }
            }
        }
    }
}
