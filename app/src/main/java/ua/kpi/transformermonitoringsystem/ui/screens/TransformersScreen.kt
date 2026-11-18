package ua.kpi.transformermonitoringsystem.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ua.kpi.transformermonitoringsystem.ui.AccountViewModel
import ua.kpi.transformermonitoringsystem.ui.TransformerViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransformersScreen(
    viewModel: TransformerViewModel,
    accountViewModel: AccountViewModel,
    onNavigateToForm: () -> Unit,
    onNavigateToDetails: (Int) -> Unit = {}
) {
    val transformers by viewModel.transformers.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isAscending by viewModel.isAscending.collectAsState()

    val userRole by accountViewModel.userRole.collectAsState()
    val username by accountViewModel.username.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTabItem by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.loadData()
        accountViewModel.loadUserProfile()
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val topBarTitle = when (selectedTabItem) {
        1 -> "Логи"
        2 -> "Профіль користувача"
        else -> "Трансфори"
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = topBarTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    actions = {
                        Surface(
                            onClick = { selectedTabItem = 2 },
                            shape = MaterialTheme.shapes.small,
                            color = if (userRole == "ADMIN") {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer
                            },
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text(
                                text = userRole,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (userRole == "ADMIN") {
                                    MaterialTheme.colorScheme.onErrorContainer
                                } else {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        IconButton(onClick = { accountViewModel.logout() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Вийти з системи"
                            )
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
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTabItem == 0,
                    onClick = { selectedTabItem = 0 },
                    icon = { Icon(Icons.Default.Bolt, contentDescription = null) },
                    label = { Text("Трансфори") }
                )
                NavigationBarItem(
                    selected = selectedTabItem == 1,
                    onClick = { selectedTabItem = 1 },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
                    label = { Text("Логи") }
                )
                NavigationBarItem(
                    selected = selectedTabItem == 2,
                    onClick = { selectedTabItem = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text(username ?: "Профіль") }
                )
            }
        },
        floatingActionButton = {
            if (selectedTabItem == 0 && userRole == "ADMIN") {
                FloatingActionButton(onClick = onNavigateToForm) {
                    Icon(Icons.Default.Add, contentDescription = "Додати трансформатор")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (selectedTabItem) {
                1 -> LogsScreen()
                2 -> UserProfileScreen(accountViewModel = accountViewModel)
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .animateContentSize()
                    ) {
                        AnimatedVisibility(
                            visible = stats != null,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            stats?.let {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Статистика", style = MaterialTheme.typography.titleMedium)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Середнє навантаження: ${String.format(Locale.US, "%.2f", it.averageLoad)} %")
                                        Text("Середня температура: ${String.format(Locale.US, "%.2f", it.averageTemperature)} °C")
                                        Text(
                                            text = "Критичних вузлів: ${it.criticalCount}",
                                            color = if (it.criticalCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                label = { Text("Пошук за назвою чи локацією") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            FilledTonalButton(onClick = { viewModel.toggleSortOrder() }) {
                                Text(if (isAscending) "А-Я" else "Я-А")
                            }
                            IconButton(onClick = { viewModel.loadData(forceRefresh = true) }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Оновити")
                            }
                        }

                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        if (transformers.isEmpty() && !isLoading) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Немає даних трансформаторів", style = MaterialTheme.typography.bodyLarge)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 80.dp)
                            ) {
                                items(transformers, key = { it.id }) { transformer ->
                                    val isCritical = transformer.temperature > 85.0 ||
                                        transformer.currentLoad > (transformer.maxLoad * 0.9)
                                    val loadRatio = if (transformer.maxLoad > 0) {
                                        (transformer.currentLoad / transformer.maxLoad).toFloat().coerceIn(0f, 1f)
                                    } else {
                                        0f
                                    }

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 6.dp)
                                            .clickable { onNavigateToDetails(transformer.id) },
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = transformer.name,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                Badge(
                                                    containerColor = if (isCritical) {
                                                        MaterialTheme.colorScheme.errorContainer
                                                    } else {
                                                        MaterialTheme.colorScheme.primaryContainer
                                                    }
                                                ) {
                                                    Text(
                                                        text = if (isCritical) "КРИТИЧНИЙ" else "НОРМА",
                                                        color = if (isCritical) {
                                                            MaterialTheme.colorScheme.onErrorContainer
                                                        } else {
                                                            MaterialTheme.colorScheme.onPrimaryContainer
                                                        },
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Локація: ${transformer.location}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = "Температура: ${transformer.temperature} °C",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = "Навантаження: ${transformer.currentLoad} / ${transformer.maxLoad} %",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            LinearProgressIndicator(
                                                progress = { loadRatio },
                                                modifier = Modifier.fillMaxWidth(),
                                                color = if (isCritical) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
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
    }
}
