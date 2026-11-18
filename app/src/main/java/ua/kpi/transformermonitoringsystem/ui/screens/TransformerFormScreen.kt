package ua.kpi.transformermonitoringsystem.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ua.kpi.transformermonitoringsystem.ui.TransformerViewModel
import ua.kpi.transformermonitoringsystem.ui.components.FormTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransformerFormScreen(navController: NavHostController, viewModel: TransformerViewModel) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var voltage by remember { mutableStateOf("") }
    var temp by remember { mutableStateOf("") }
    var load by remember { mutableStateOf("") }
    var maxLoad by remember { mutableStateOf("100.0") }

    var nameError by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf(false) }
    var voltageError by remember { mutableStateOf(false) }
    var tempError by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf(false) }
    var maxLoadError by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val isSubmitted by viewModel.isSubmitted.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(isSubmitted) {
        if (isSubmitted) {
            navController.popBackStack()
            viewModel.resetSubmitState()
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearError() }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Новий трансформатор") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
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
        BoxWithConstraints(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            val isWide = maxWidth > 600.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (isWide) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormTextField("Назва", name, nameError, "Мін. 3 символи") {
                                name = it; nameError = false; viewModel.clearError()
                            }
                            FormTextField("Локація", location, locationError, "Наприклад: Київ, вул. Енергетиків") {
                                location = it; locationError = false; viewModel.clearError()
                            }
                            FormTextField("Макс. навантаження (%)", maxLoad, maxLoadError, "Більше 0 (за замовчуванням 100)") {
                                maxLoad = it; maxLoadError = false; viewModel.clearError()
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormTextField("Початкова напруга (В)", voltage, voltageError, "Більше 0 (наприклад: 220)") {
                                voltage = it; voltageError = false; viewModel.clearError()
                            }
                            FormTextField("Температура (°C)", temp, tempError, "Від -50 до 150") {
                                temp = it; tempError = false; viewModel.clearError()
                            }
                            FormTextField("Поточне навантаження (%)", load, loadError, "Від 0 і вище") {
                                load = it; loadError = false; viewModel.clearError()
                            }
                        }
                    }
                } else {
                    FormTextField("Назва", name, nameError, "Мін. 3 символи") {
                        name = it; nameError = false; viewModel.clearError()
                    }
                    FormTextField("Локація", location, locationError, "Наприклад: Київ, вул. Енергетиків") {
                        location = it; locationError = false; viewModel.clearError()
                    }
                    FormTextField("Макс. навантаження (%)", maxLoad, maxLoadError, "Більше 0 (за замовчуванням 100)") {
                        maxLoad = it; maxLoadError = false; viewModel.clearError()
                    }
                    FormTextField("Початкова напруга (В)", voltage, voltageError, "Більше 0 (наприклад: 220)") {
                        voltage = it; voltageError = false; viewModel.clearError()
                    }
                    FormTextField("Температура (°C)", temp, tempError, "Від -50 до 150") {
                        temp = it; tempError = false; viewModel.clearError()
                    }
                    FormTextField("Поточне навантаження (%)", load, loadError, "Від 0 і вище") {
                        load = it; loadError = false; viewModel.clearError()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            nameError = name.trim().length < 3
                            locationError = location.isBlank()
                            maxLoadError = maxLoad.toDoubleOrNull()?.let { it <= 0.0 } ?: true
                            voltageError = voltage.toDoubleOrNull()?.let { it <= 0.0 } ?: true
                            tempError = temp.toDoubleOrNull()?.let { it !in -50.0..150.0 } ?: true
                            loadError = load.toDoubleOrNull()?.let { it < 0.0 } ?: true

                            if (!nameError && !locationError && !maxLoadError && !voltageError && !tempError && !loadError) {
                                viewModel.addTransformer(name, location, voltage, temp, load, maxLoad)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Зберегти")
                    }
                }
            }
        }
    }
}
