package ua.kpi.transformermonitoringsystem.forms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginForm(
    onLogin: (String, String) -> String,
    onSwitchToRegister: () -> Unit
) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Text(
        text = "Авторизація у системі моніторингу",
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    OutlinedTextField(
        value = login,
        onValueChange = { login = it },
        label = { Text("Логін / Email") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Пароль") },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = { message = onLogin(login, password) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Увійти")
    }

    Spacer(modifier = Modifier.height(8.dp))

    TextButton(onClick = onSwitchToRegister) {
        Text("Ще немає акаунту? Зареєструватися")
    }

    if (message.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            color = if (message.contains("успішна")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
