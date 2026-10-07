package ua.kpi.transformermonitoringsystem.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ua.kpi.transformermonitoringsystem.ui.TransformerViewModel
import ua.kpi.transformermonitoringsystem.ui.components.FormTextField

// Екран додавання нового трансформатора
@Composable
fun TransformerFormScreen(navController: NavHostController, viewModel: TransformerViewModel) {
    // Локальні стани для введених даних
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var voltage by remember { mutableStateOf("") }
    var temp by remember { mutableStateOf("") }
    var load by remember { mutableStateOf("") }

    // Локальні стани для індивідуальних помилок полів
    var nameError by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf(false) }
    var voltageError by remember { mutableStateOf(false) }
    var tempError by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf(false) }

    // Спостереження за станами з ViewModel (StateFlow)
    val isLoading by viewModel.isLoading.collectAsState()
    val isSubmitted by viewModel.isSubmitted.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Повернення назад при успішному збереженні
    LaunchedEffect(isSubmitted) {
        if (isSubmitted) {
            navController.popBackStack()
            viewModel.resetSubmitState()
        }
    }

    // Очищення помилок при виході з екрану
    DisposableEffect(Unit) {
        onDispose { viewModel.clearError() }
    }

    // Адаптивний контейнер для визначення розміру екрану
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth > 600.dp
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Реєстрація Трансформатора", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            // Відображення загальної помилки (наприклад, з БД)
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Адаптивне розташування полів: у 2 колонки або в 1
            if (isWide) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormTextField("Назва", name, nameError, "Мін. 3 символи") { name = it; nameError = false; viewModel.clearError() }
                        FormTextField("Локація", location, locationError, "Наприклад: Київ, вул. Енергетиків") { location = it; locationError = false; viewModel.clearError() }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FormTextField("Напруга (В)", voltage, voltageError, "Більше 0 (наприклад: 220)") { voltage = it; voltageError = false; viewModel.clearError() }
                        FormTextField("Температура (°C)", temp, tempError, "Від -50 до 150") { temp = it; tempError = false; viewModel.clearError() }
                        FormTextField("Навантаження (%)", load, loadError, "Від 0 до 150") { load = it; loadError = false; viewModel.clearError() }
                    }
                }
            } else {
                FormTextField("Назва", name, nameError, "Мін. 3 символи") { name = it; nameError = false; viewModel.clearError() }
                FormTextField("Локація", location, locationError, "Наприклад: Київ, вул. Енергетиків") { location = it; locationError = false; viewModel.clearError() }
                FormTextField("Напруга (В)", voltage, voltageError, "Більше 0 (наприклад: 220)") { voltage = it; voltageError = false; viewModel.clearError() }
                FormTextField("Температура (°C)", temp, tempError, "Від -50 до 150") { temp = it; tempError = false; viewModel.clearError() }
                FormTextField("Навантаження (%)", load, loadError, "Від 0 до 150") { load = it; loadError = false; viewModel.clearError() }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator() // Індикатор завантаження
            } else {
                Button(
                    onClick = {
                        // Локальна валідація для кожного поля
                        nameError = name.length < 3
                        locationError = location.isBlank()
                        voltageError = voltage.toDoubleOrNull()?.let { it <= 0 } ?: true
                        tempError = temp.toDoubleOrNull()?.let { it !in -50.0..150.0 } ?: true
                        loadError = load.toDoubleOrNull()?.let { it !in 0.0..150.0 } ?: true

                        // Якщо всі поля валідні, відправляємо у ViewModel
                        if (!nameError && !locationError && !voltageError && !tempError && !loadError) {
                            viewModel.addTransformer(name, location, voltage, temp, load)
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
