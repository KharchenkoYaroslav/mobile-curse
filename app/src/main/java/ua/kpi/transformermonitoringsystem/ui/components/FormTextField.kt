package ua.kpi.transformermonitoringsystem.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Перевикористовуваний компонент поля вводу з підтримкою валідації та підказок
@Composable
fun FormTextField(
    label: String,
    value: String,
    isError: Boolean,
    helperText: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = isError,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        supportingText = {
            if (isError) {
                Text(
                    text = "Некоректний ввід",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Text(
                    text = helperText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    )
}
