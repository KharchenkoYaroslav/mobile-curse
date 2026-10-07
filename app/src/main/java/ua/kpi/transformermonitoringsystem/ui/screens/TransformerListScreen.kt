package ua.kpi.transformermonitoringsystem.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ua.kpi.transformermonitoringsystem.data.local.Transformer
import ua.kpi.transformermonitoringsystem.ui.navigation.NavScreen
import ua.kpi.transformermonitoringsystem.ui.TransformerViewModel

// Екран відображення списку трансформаторів
@Composable
fun TransformerListScreen(navController: NavHostController, viewModel: TransformerViewModel) {
    // Підписка на StateFlow з ViewModel для реактивного оновлення UI
    val transformers by viewModel.transformers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isAscending by viewModel.isAscending.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Завантажуємо дані при першому відкритті екрану
    LaunchedEffect(Unit) {
        viewModel.loadTransformers()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .systemBarsPadding()
        .padding(16.dp)) {
        Text("Список трансформаторів", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // Поле для пошуку (фільтрації)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            label = { Text("Пошук за назвою або локацією") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Кнопка для зміни напрямку сортування
        Button(onClick = { viewModel.toggleSortOrder() }, modifier = Modifier.fillMaxWidth()) {
            Text(if (isAscending) "Сортування: А-Я" else "Сортування: Я-А")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Обробка помилок та порожнього списку
        if (errorMessage != null) {
            Text(text = "Помилка: $errorMessage", color = MaterialTheme.colorScheme.error)
        } else if (transformers.isEmpty()) {
            Text("Немає даних, що відповідають критеріям", style = MaterialTheme.typography.bodyLarge)
        } else {
            // Використання LazyColumn для оптимізованого відображення великих списків
            LazyColumn {
                items(transformers) { transformer ->
                    TransformerItem(transformer) {
                        // Перехід до екрану деталей конкретного трансформатора
                        navController.navigate(NavScreen.Details.createRoute(transformer.id))
                    }
                }
            }
        }
    }
}

// Окремий Composable-компонент для елемента списку
@Composable
fun TransformerItem(transformer: Transformer, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(transformer.name, style = MaterialTheme.typography.titleMedium)
            Text(transformer.location, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
