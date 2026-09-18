package ua.kpi.transformermonitoringsystem

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Warning
import ua.kpi.transformermonitoringsystem.data.Calculator
import java.util.Locale
import kotlin.random.Random
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp()
                }
            }
        }
    }
}

enum class PracticalWork {
    PR1, PR2, PR3
}

@Composable
fun MainApp() {
    var selectedPractical by remember { mutableStateOf(PracticalWork.PR1) }

    Column(modifier = Modifier.fillMaxSize()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .statusBarsPadding()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PracticalWork.entries.forEach { work ->
                Button(
                    onClick = { selectedPractical = work },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedPractical == work) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                ) {
                    val title = when (work) {
                        PracticalWork.PR1 -> "Пр 1 (Основи)"
                        PracticalWork.PR2 -> "Пр 2 (Логіка)"
                        PracticalWork.PR3 -> "Пр 3 (UI)"
                    }
                    Text(title)
                }
            }
        }

        HorizontalDivider()

        when (selectedPractical) {
            PracticalWork.PR1 -> Practical1Screen()
            PracticalWork.PR2 -> Practical2Screen()
            PracticalWork.PR3 -> Practical3Screen()
        }
    }
}

@Composable
fun Practical1Screen() {
    var voltageForecast by remember { mutableIntStateOf(0) }

    val animatedVoltage by animateIntAsState(
        targetValue = voltageForecast,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Система моніторингу трансформаторів",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Прогноз напруги: $animatedVoltage кВ",
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                voltageForecast = Random.nextInt(100, 121) 
            }
        ) {
            Text("Оновити прогноз напруги")
        }
    }
}

@Composable
fun Practical2Screen() {
    var voltageInput by remember { mutableStateOf("") }
    var currentInput by remember { mutableStateOf("") }
    var cosPhiInput by remember { mutableStateOf("") }

    var powerResult by remember { mutableDoubleStateOf(0.0) }

    val calculator = remember { Calculator() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Просунутий рівень: масштабування проєкту",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            value = voltageInput,
            onValueChange = { voltageInput = it },
            label = { Text("Напруга (кВ)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = currentInput,
            onValueChange = { currentInput = it },
            label = { Text("Струм (А)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = cosPhiInput,
            onValueChange = { cosPhiInput = it },
            label = { Text("Коефіцієнт потужності (cos φ, напр. 0.85)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                powerResult = calculator.calculatePowerAdvanced(voltageInput, currentInput, cosPhiInput)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Розрахувати активну потужність")
        }

        Text(
            text = "Розрахована активна потужність: ${String.format(Locale.US, "%.2f", powerResult)} кВт",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

data class ModeOption(
    val title: String,
    val modeName: String,
    val temp: Int,
    val oil: String,
    val warning: String,
    val isDanger: Boolean = false
)

@Composable
fun Practical3Screen() {
    var mode by remember { mutableStateOf("Холостий хід") }
    var temperature by remember { mutableIntStateOf(45) }
    var oilLevel by remember { mutableStateOf("Норма") }
    var warnings by remember { mutableStateOf("Немає") }

    val modeOptions = remember {
        listOf(
            ModeOption("Хол. хід", "Холостий хід", 45, "Норма", "Немає"),
            ModeOption("Номінал", "Номінальне навантаження", 75, "Норма", "Немає"),
            ModeOption("Перевант.", "Перевантаження", 105, "Низький", "УВАГА! Перегрів!", isDanger = true)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Детальний моніторинг",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            modeOptions.forEach { option ->
                Button(
                    onClick = { 
                        mode = option.modeName
                        temperature = option.temp
                        oilLevel = option.oil
                        warnings = option.warning
                    }, 
                    colors = if (option.isDanger) {
                        ButtonDefaults.buttonColors(containerColor = Color.Red)
                    } else {
                        ButtonDefaults.buttonColors()
                    },
                    modifier = Modifier.weight(1f).padding(2.dp)
                ) {
                    Text(option.title, fontSize = 12.sp)
                }
            }
        }

        Icon(
            imageVector = if (temperature > 100) Icons.Filled.Warning else Icons.Filled.ElectricBolt,
            contentDescription = "Статус трансформатора",
            modifier = Modifier
                .padding(vertical = 32.dp)
                .size(100.dp),
            tint = if (temperature > 100) Color.Red else Color(0xFFFBC02D)
        )

        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Режим роботи: $mode", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Температура оливи: $temperature °C", fontSize = 16.sp)
                Text("Рівень оливи: $oilLevel", fontSize = 16.sp)
                
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Попередження: $warnings", 
                    fontSize = 16.sp, 
                    color = if (warnings != "Немає") Color.Red else Color.Black,
                    fontWeight = if (warnings != "Немає") FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
