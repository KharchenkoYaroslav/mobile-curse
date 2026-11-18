package ua.kpi.transformermonitoringsystem.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Екран перегляду та очищення журналу логів помилок.
 */
@Composable
fun LogsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var errorLogsText by remember { mutableStateOf("") }

    fun loadErrorLogs() {
        scope.launch(Dispatchers.IO) {
            val file = File(context.filesDir, "error_log.txt")
            val content = if (file.exists()) {
                val lines = file.readLines().takeLast(50).reversed()
                if (lines.isEmpty()) "Журнал помилок порожній" else lines.joinToString("\n")
            } else {
                "Файл error_log.txt ще не створено"
            }
            withContext(Dispatchers.Main) {
                errorLogsText = content
            }
        }
    }

    LaunchedEffect(Unit) {
        loadErrorLogs()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BugReport, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Журнал помилок (error_log.txt)", style = MaterialTheme.typography.titleMedium)
                    }
                    IconButton(onClick = { loadErrorLogs() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Оновити лог")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(
                        text = errorLogsText,
                        modifier = Modifier
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState()),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            File(context.filesDir, "error_log.txt").delete()
                            loadErrorLogs()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Лог очищено", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Очистити лог")
                }
            }
        }
    }
}
