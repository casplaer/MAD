package com.example.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.ui.theme.CalculatorTheme
import com.example.calculator.viewmodels.CalculatorViewModel

class MainActivity : ComponentActivity() {

    private val calculatorViewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorTheme {
                Scaffold(modifier = Modifier.fillMaxSize().background(Color.Black)) { innerPadding ->
                    CalculatorScreen(modifier = Modifier.padding(innerPadding), viewModel = calculatorViewModel)
                }
            }
        }
    }
}

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier, viewModel: CalculatorViewModel) {
    val input = viewModel.getStringExpression()
    val result = viewModel.getEvaluated()

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

        IconButton(
            onClick = { viewModel.updateExpression("⌫") },
            modifier = Modifier
                .padding(end = 16.dp)
                .size(32.dp)
        ) {
            Text(
                text = "⌫",
                style = TextStyle(fontSize = 20.sp, color = Color.DarkGray)
            )
        }

        HorizontalDivider(
            color = Color.DarkGray,
            thickness = 1.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .align(Alignment.CenterHorizontally)
        )

        CalculatorButtons(
            onButtonClick = { button ->
                viewModel.updateExpression(button)
            }
        )
    }
}

@Composable
fun CalculatorButtons(onButtonClick: (String) -> Unit) {
    val buttons = listOf(
        listOf("C", "(", ")", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "–"),
        listOf("1", "2", "3", "+"),
        listOf("%", "0", ".", "=")
    )

    Column {
        buttons.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.97f),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                row.forEach { button ->
                    CalculatorButton(
                        label = button,
                        onClick = { onButtonClick(button) }
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (fontSize, backgroundColor, textColor) = when (label) {
        "⌫" -> Triple(28.sp, Color(0xFF131313), Color(0xFFE8E8E8))
        "=" -> Triple(40.sp, Color(0xFF676767), Color(0xFFE8E8E8))
        "C" -> Triple(28.sp, Color(0xFF131313), Color(0xFFB71C1C))
        "÷" -> Triple(34.sp, Color(0xFF131313), Color(0xFFE8E8E8))
        "×" -> Triple(34.sp, Color(0xFF131313), Color(0xFFE8E8E8))
        "–" -> Triple(34.sp, Color(0xFF131313), Color(0xFFE8E8E8))
        else -> Triple(28.sp, Color(0xFF131313), Color(0xFFE8E8E8))
    }

    val buttonWidth = 80.dp

    Button(
        onClick = onClick,
        modifier = modifier
            .padding(4.dp)
            .width(buttonWidth)
            .height(80.dp),
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
                maxLines = 1
            )
        }
    }
}
