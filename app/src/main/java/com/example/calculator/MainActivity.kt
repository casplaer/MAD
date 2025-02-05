package com.example.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.ui.theme.CalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorTheme {
                Scaffold(modifier = Modifier.fillMaxSize().background(Color.Black)) { innerPadding ->
                    CalculatorScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    LaunchedEffect(input) {
        if (isCompleteExpression(input)) {
            result = evaluateExpression(input)
        } else {
            result = ""
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.End
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = input,
            style = TextStyle(fontSize = 34.sp, color = Color(0xFFE8E8E8)),
            modifier = Modifier.padding(8.dp)
        )

        Text(
            text = result,
            style = TextStyle(fontSize = 24.sp, color = Color.DarkGray),
            modifier = Modifier.padding(8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        CalculatorButtons(
            onButtonClick = { button ->
                if (button == "=") {
                    if (isCompleteExpression(input)) {
                        input = result
                        result = ""
                    }
                } else if (button == "C") {
                    input = ""
                    result = ""
                } else {
                    input += button
                }
            }
        )
    }
}

@Composable
fun CalculatorButtons(onButtonClick: (String) -> Unit) {
    val buttons = listOf(
        listOf("C", "( )", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "—"),
        listOf("1", "2", "3", "+"),
        listOf("+/-", "0", ",", "=")
    )

    Column {
        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { button ->
                    CalculatorButton(
                        label = button,
                        onClick = { onButtonClick(button) },
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val (fontSize, backgroundColor, textColor) = when (label) {
        "+/-" -> Triple(23.sp, Color(0xFF131313), Color(0xFFE8E8E8))
        "=" -> Triple(34.sp, Color(0xFF4E4E4E), Color(0xFFE8E8E8))
        "C" -> Triple(28.sp, Color(0xFF131313), Color(0xFFB71C1C))
        "×" -> Triple(28.sp, Color(0xFF131313), Color(0xFFE8E8E8))
        "÷" -> Triple(28.sp, Color(0xFF131313), Color(0xFFE8E8E8))
        "( )" -> Triple(22.sp, Color(0xFF131313), Color(0xFFE8E8E8))
        "—" -> Triple(34.sp, Color(0xFF131313), Color(0xFFE8E8E8))
        else -> Triple(28.sp, Color(0xFF131313), Color(0xFFE8E8E8))
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .padding(4.dp)
            .size(80.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        shape = CircleShape
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = label,
                style = TextStyle(fontSize = fontSize),
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}



fun evaluateExpression(expression: String): String {
    return try {
        val result = when {
            expression.contains("÷") -> {
                val parts = expression.split("÷")
                (parts[0].toDouble() / parts[1].toDouble()).toString()
            }
            expression.contains("×") -> {
                val parts = expression.split("×")
                (parts[0].toDouble() * parts[1].toDouble()).toString()
            }
            expression.contains("—") -> {
                val parts = expression.split("—")
                (parts[0].toDouble() - parts[1].toDouble()).toString()
            }
            expression.contains("+") -> {
                val parts = expression.split("+")
                (parts[0].toDouble() + parts[1].toDouble()).toString()
            }
            else -> expression
        }

        if (result.endsWith(".0")) {
            result.dropLast(2)
        } else {
            result
        }

    } catch (e: Exception) {
        "Error"
    }
}

fun isCompleteExpression(expression: String): Boolean {
    val operators = listOf("+", "—", "×", "÷")
    return operators.any { operator ->
        val parts = expression.split(operator)
        parts.size == 2 && parts[0].isNotEmpty() && parts[1].isNotEmpty()
    }
}

@Preview(showBackground = true)
@Composable
fun CalculatorPreview() {
    CalculatorTheme {
        CalculatorScreen(modifier = Modifier.background(Color.Black))
    }
}
