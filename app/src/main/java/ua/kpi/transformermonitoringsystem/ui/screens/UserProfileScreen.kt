package ua.kpi.transformermonitoringsystem.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ua.kpi.transformermonitoringsystem.ui.AccountViewModel

@Composable
fun UserProfileScreen(
    accountViewModel: AccountViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userRole by accountViewModel.userRole.collectAsState()
    val username by accountViewModel.username.collectAsState()
    val userProfile by accountViewModel.userProfile.collectAsState()
    val isLoading by accountViewModel.isLoading.collectAsState()
    val errorMessage by accountViewModel.errorMessage.collectAsState()

    var showAdminKeyDialog by remember { mutableStateOf(false) }
    var adminKeyInput by remember { mutableStateOf("") }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var currentPasswordInput by remember { mutableStateOf("") }
    var newLoginInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }

    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        accountViewModel.loadUserProfile()
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            accountViewModel.clearError()
        }
    }

    val formattedCreatedAt = remember(userProfile?.createdAt) {
        userProfile?.createdAt?.toString()?.replace("T", " ")?.substringBefore(".")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Картка профілю користувача (UserProfile)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = userProfile?.login ?: username ?: "Користувач",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Badge(
                        containerColor = if (userRole == "ADMIN") {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    ) {
                        Text(
                            text = userRole,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                userProfile?.let { _ ->
                    if (formattedCreatedAt != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Дата реєстрації: $formattedCreatedAt",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Налаштування акаунту та ролі
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Керування акаунтом та правами", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        newLoginInput = userProfile?.login ?: username.orEmpty()
                        currentPasswordInput = ""
                        newPasswordInput = ""
                        showEditProfileDialog = true
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Редагувати логін або пароль")
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (userRole != "ADMIN") {
                    OutlinedButton(
                        onClick = {
                            adminKeyInput = ""
                            showAdminKeyDialog = true
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Отримати права ADMIN за ключем")
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            accountViewModel.changeRoleToUser {
                                Toast.makeText(context, "Роль змінено на USER", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Перейти до ролі USER")
                    }
                }
            }
        }

        // Вихід та видалення акаунту
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Сесія", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { accountViewModel.logout() },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Вийти з системи")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showDeleteAccountDialog = true },
                    enabled = !isLoading,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Видалити акаунт")
                }
            }
        }
    }

    // Діалог отримання прав ADMIN
    if (showAdminKeyDialog) {
        AlertDialog(
            onDismissRequest = { showAdminKeyDialog = false },
            title = { Text("Підвищення ролі до ADMIN") },
            text = {
                Column {
                    Text("Введіть одноразовий секретний ключ адміністратора:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = adminKeyInput,
                        onValueChange = { adminKeyInput = it },
                        label = { Text("Секретний ключ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        accountViewModel.changeRoleToAdmin(adminKeyInput) {
                            showAdminKeyDialog = false
                            Toast.makeText(context, "Права ADMIN активовано", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Підтвердити")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminKeyDialog = false }) {
                    Text("Скасувати")
                }
            }
        )
    }

    // Діалог оновлення профілю (PUT /users/profile)
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Оновлення профілю") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = currentPasswordInput,
                        onValueChange = { currentPasswordInput = it },
                        label = { Text("Поточний пароль *") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newLoginInput,
                        onValueChange = { newLoginInput = it },
                        label = { Text("Новий логін (необов'язково)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = { newPasswordInput = it },
                        label = { Text("Новий пароль (мін. 6 символів)") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        accountViewModel.updateProfile(
                            currentPassword = currentPasswordInput,
                            newLogin = newLoginInput,
                            newPassword = newPasswordInput
                        ) {
                            showEditProfileDialog = false
                            Toast.makeText(context, "Профіль оновлено", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Зберегти")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Скасувати")
                }
            }
        )
    }

    // Діалог видалення акаунту (DELETE /users/profile)
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Видалити акаунт?") },
            text = { Text("Акаунт буде видалено безповоротно, а сесію завершено.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        accountViewModel.deleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Видалити")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Скасувати")
                }
            }
        )
    }
}
