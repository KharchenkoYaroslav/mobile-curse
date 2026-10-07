package ua.kpi.transformermonitoringsystem.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ua.kpi.transformermonitoringsystem.ui.TransformerViewModel
import java.util.Locale

// Екран відображення детальної інформації про трансформатор та його історію статусів
@Composable
fun TransformerDetailsScreen(transformerId: Int, navController: NavHostController, viewModel: TransformerViewModel) {
    // Зчитування реактивних станів із ViewModel
    val statuses by viewModel.statuses.collectAsState()
    val avgTemp by viewModel.avgTemperature.collectAsState()
    val avgLoad by viewModel.avgLoad.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSubmitted by viewModel.isSubmitted.collectAsState()
    val selectedTransformer by viewModel.selectedTransformer.collectAsState()

    // Ініціалізація даних при відкритті екрану
    LaunchedEffect(transformerId) {
        viewModel.selectTransformer(transformerId)
    }

    // Локальні стани для керування діалогами
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isMenuExpanded by remember { mutableStateOf(false) }

    // Обробка успішного завершення операцій
    LaunchedEffect(isSubmitted) {
        if (isSubmitted) {
            if (showDeleteDialog) {
                showDeleteDialog = false
                viewModel.resetSubmitState()
                navController.popBackStack()
            } else if (showEditDialog) {
                showEditDialog = false
                viewModel.resetSubmitState()
            } else if (showAddDialog) {
                showAddDialog = false
                viewModel.resetSubmitState()
            }
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .systemBarsPadding()
        .padding(16.dp)) {
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(selectedTransformer?.name ?: "Деталі", style = MaterialTheme.typography.titleLarge)
            Row {
                Button(onClick = { showAddDialog = true }) {
                    Text("+ Статус")
                }
                Box {
                    IconButton(onClick = { isMenuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Меню")
                    }
                    // Випадаюче меню для редагування та видалення
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Редагувати") },
                            onClick = {
                                isMenuExpanded = false
                                showEditDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Видалити", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                isMenuExpanded = false
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Відображення локації
        if (selectedTransformer != null) {
            Text("Локація: ${selectedTransformer!!.location}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Відображення помилок
        if (errorMessage != null) {
            Text(text = "Помилка: $errorMessage", color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Карточка з агрегованими показниками (середня температура та навантаження)
        Card(modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Агреговані показники", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Середня температура: ${String.format(Locale.US, "%.1f", avgTemp)} °C")
                Text("Середнє навантаження: ${String.format(Locale.US, "%.1f", avgLoad)} %")
            }
        }
        
        Text("Історія статусів:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // Історія статусів з індикатором завантаження
        if (isLoading && statuses.isEmpty()) {
            CircularProgressIndicator()
        } else if (statuses.isEmpty()) {
            Text("Немає статусів", style = MaterialTheme.typography.bodyLarge)
        } else {
            LazyColumn {
                items(statuses) { status ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Напруга: ${status.voltage} В")
                                Text("Температура: ${status.temperature} °C")
                                Text("Навантаження: ${status.load} %")
                            }
                            IconButton(onClick = { viewModel.deleteStatus(status) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Видалити статус", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    // Діалог додавання нового статусу
    if (showAddDialog) {
        var voltage by remember { mutableStateOf("") }
        var temp by remember { mutableStateOf("") }
        var load by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { 
                showAddDialog = false
                viewModel.clearError() 
            },
            title = { Text("Новий статус") },
            text = {
                Column {
                    if (errorMessage != null) {
                        Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    OutlinedTextField(value = voltage, onValueChange = { voltage = it }, label = { Text("Напруга (В)") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = temp, onValueChange = { temp = it }, label = { Text("Температура (°C)") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = load, onValueChange = { load = it }, label = { Text("Навантаження (%)") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.resetSubmitState()
                    viewModel.addStatusForSelectedTransformer(voltage, temp, load)
                }) {
                    Text("Додати")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddDialog = false
                    viewModel.clearError() 
                }) {
                    Text("Скасувати")
                }
            }
        )
    }

    // Діалог редагування інформації
    if (showEditDialog && selectedTransformer != null) {
        var editName by remember { mutableStateOf(selectedTransformer!!.name) }
        var editLocation by remember { mutableStateOf(selectedTransformer!!.location) }

        AlertDialog(
            onDismissRequest = { 
                showEditDialog = false
                viewModel.clearError()
            },
            title = { Text("Редагувати трансформатор") },
            text = {
                Column {
                    if (errorMessage != null) {
                        Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Назва") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = editLocation, onValueChange = { editLocation = it }, label = { Text("Локація") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateSelectedTransformer(editName, editLocation)
                }) {
                    Text("Зберегти")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showEditDialog = false
                    viewModel.clearError()
                }) {
                    Text("Скасувати")
                }
            }
        )
    }

    // Діалог підтвердження видалення
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Видалити трансформатор?") },
            text = { Text("Цю дію неможливо скасувати. Всі статуси трансформатора також будуть видалені.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteSelectedTransformer() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Видалити")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Скасувати")
                }
            }
        )
    }
}
