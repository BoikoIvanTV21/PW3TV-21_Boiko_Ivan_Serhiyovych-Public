package com.example.pr2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                EmissionUI()
            }
        }
    }
}

@Composable
fun EmissionUI() {
    var coal by remember { mutableStateOf(FuelInput("Вугілля", amountText = "595061.91", qrText = "20.47", kText = "150", etaFilterText = "0.985")) }
    var fuelOil by remember { mutableStateOf(FuelInput("Мазут", amountText = "125029.33", qrText = "40.40", kText = "0.57", etaFilterText = "0.985")) }
    var gas by remember { mutableStateOf(FuelInput("Природний газ", isGas = true, amountText = "142828.90", qrText = "33.08", kText = "0", densityText = "0.723", etaFilterText = "1.0")) }

    var results by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    var total by remember { mutableStateOf(0.0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Калькулятор викидів твердих частинок",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Text(
            "Оберіть тип палива та перевірте розрахунок емісій.",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))
        FuelCard(input = coal, onChange = { coal = it })
        FuelCard(input = fuelOil, onChange = { fuelOil = it })
        FuelCard(input = gas, onChange = { gas = it })

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val (map, sum) = computeAll(listOf(coal, fuelOil, gas))
                results = map
                total = sum
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Розрахувати", fontSize = 18.sp)
        }

        if (results.isNotEmpty()) {
            Spacer(modifier = Modifier.height(18.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Результати:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    results.forEach { (name, value) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(name)
                            Text(String.format("%.4f т", value), fontWeight = FontWeight.Medium)
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Сумарний викид", fontWeight = FontWeight.Bold)
                        Text(String.format("%.4f т", total), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Примітка:", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "- Формула: E = k * B * Qr / 1e6\n" +
                            "- Для газу: м³ → т через щільність (кг/м³)/1000\n" +
                            "- Якщо k=0 → результат 0.",
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun FuelCard(input: FuelInput, onChange: (FuelInput) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(input.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = input.amountText,
                onValueChange = { onChange(input.copy(amountText = it)) },
                label = { Text(if (input.isGas) "Обсяг (м³)" else "Маса (т)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = input.qrText,
                onValueChange = { onChange(input.copy(qrText = it)) },
                label = { Text("Qr (МДж/кг)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (input.isGas) {
                OutlinedTextField(
                    value = input.densityText,
                    onValueChange = { onChange(input.copy(densityText = it)) },
                    label = { Text("Щільність (кг/м³)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            } else {
                OutlinedTextField(
                    value = input.etaFilterText,
                    onValueChange = { onChange(input.copy(etaFilterText = it)) },
                    label = { Text("Ефективність фільтра (0..1)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = input.kText,
                onValueChange = { onChange(input.copy(kText = it)) },
                label = { Text("k (г/ГДж)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

// Математична логіка (без змін)
data class FuelInput(
    val name: String,
    val isGas: Boolean = false,
    val amountText: String = "",
    val qrText: String = "",
    val kText: String = "",
    val densityText: String = "0.723",
    val etaFilterText: String = "0.985"
)

fun parseDoubleOrZero(s: String): Double {
    return try {
        s.replace(',', '.').trim().toDouble()
    } catch (e: Exception) {
        0.0
    }
}

fun computeForFuel(f: FuelInput): Double {
    val k = parseDoubleOrZero(f.kText)
    val qr = parseDoubleOrZero(f.qrText)
    val eta = parseDoubleOrZero(f.etaFilterText).coerceIn(0.0, 1.0)

    val massTons = if (f.isGas) {
        val vol = parseDoubleOrZero(f.amountText)
        val dens = parseDoubleOrZero(f.densityText).let { if (it <= 0) 0.723 else it }
        (vol * dens) / 1000.0
    } else parseDoubleOrZero(f.amountText)

    if (k <= 0 || qr <= 0 || massTons <= 0) return 0.0

    val e = k * massTons * qr / 1_000_000.0
    return e * (1 - eta)
}

fun computeAll(list: List<FuelInput>): Pair<Map<String, Double>, Double> {
    val map = mutableMapOf<String, Double>()
    var sum = 0.0
    for (f in list) {
        val res = computeForFuel(f)
        map[f.name] = res
        sum += res
    }
    return map to sum
}

@Preview(showBackground = true)
@Composable
fun PreviewEmissionUI() {
    EmissionUI()
}
