package ua.kpi.transformermonitoringsystem.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ua.kpi.transformermonitoringsystem.ui.AccountViewModel
import ua.kpi.transformermonitoringsystem.ui.TransformerViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransformerDetailsScreen(
    transformerId: Int,
    navController: NavHostController,
    viewModel: TransformerViewModel,
    accountViewModel: AccountViewModel
) {
    val statuses by viewModel.statuses.collectAsState()
    val avgTemp by viewModel.avgTemperature.collectAsState()
    val avgLoad by viewModel.avgLoad.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSubmitted by viewModel.isSubmitted.collectAsState()
    val selectedTransformer by viewModel.selectedTransformer.collectAsState()
    val userRole by accountViewModel.userRole.collectAsState()

    LaunchedEffect(transformerId) {
        viewModel.selectTransformer(transformerId)
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isMenuExpanded by remember { mutableStateOf(false) }

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

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(selectedTransformer?.name ?: "Деталі трансформатора") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                        }
                    },
                    actions = {
                        if (userRole == "ADMIN") {
                            IconButton(onClick = { showAddDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Додати статус")
                            }
                            Box {
                                IconButton(onClick = { isMenuExpanded = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Меню")
                                }
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
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 1.dp
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            selectedTransformer?.let { transformer ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Характеристики вузла", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Локація: ${transformer.location}")
                        Text("Поточна температура: ${transformer.temperature} °C")
                        Text("Навантаження: ${transformer.currentLoad} / ${transformer.maxLoad} %")
                    }
                }
            }

            if (errorMessage != null) {
                Text(text = "Помилка: $errorMessage", color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Агреговані показники телеметрії", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Середня температура: ${String.format(Locale.US, "%.1f", avgTemp)} °C")
                    Text("Середнє навантаження: ${String.format(Locale.US, "%.1f", avgLoad)} %")
                }
            }

            Text("Історія показників телеметрії", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading && statuses.isEmpty()) {
                CircularProgressIndicator()
            } else if (statuses.isEmpty()) {
                Text("Немає збережених показників", style = MaterialTheme.typography.bodyLarge)
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(statuses, key = { it.id }) { status ->
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
                                if (userRole == "ADMIN") {
                                    IconButton(onClick = { viewModel.deleteStatus(status) }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Видалити статус",
                                            tint = MaterialTheme.colorScheme.error
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

    if (showAddDialog && userRole == "ADMIN") {
        var voltage by remember { mutableStateOf("") }
        var temp by remember { mutableStateOf("") }
        var load by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                viewModel.clearError()
            },
            title = { Text("Новий показник телеметрії") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (errorMessage != null) {
                        Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedTextField(value = voltage, onValueChange = { voltage = it }, label = { Text("Напруга (В)") })
                    OutlinedTextField(value = temp, onValueChange = { temp = it }, label = { Text("Температура (°C)") })
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

    if (showEditDialog && selectedTransformer != null && userRole == "ADMIN") {
        val currentTransformer = selectedTransformer!!
        var editName by remember(currentTransformer) { mutableStateOf(currentTransformer.name) }
        var editLocation by remember(currentTransformer) { mutableStateOf(currentTransformer.location) }
        var editMaxLoad by remember(currentTransformer) { mutableStateOf(currentTransformer.maxLoad.toString()) }

        AlertDialog(
            onDismissRequest = {
                showEditDialog = false
                viewModel.clearError()
            },
            title = { Text("Редагувати трансформатор") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (errorMessage != null) {
                        Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Назва") })
                    OutlinedTextField(value = editLocation, onValueChange = { editLocation = it }, label = { Text("Локація") })
                    OutlinedTextField(value = editMaxLoad, onValueChange = { editMaxLoad = it }, label = { Text("Макс. навантаження (%)") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateSelectedTransformer(editName, editLocation, editMaxLoad)
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

    if (showDeleteDialog && userRole == "ADMIN") {
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
