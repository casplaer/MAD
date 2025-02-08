package com.example.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.calculator.ui.theme.CalculatorTheme
import com.example.calculator.viewmodels.CalculatorViewModel
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext


class MainActivity : ComponentActivity() {

    private val calculatorViewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        enableEdgeToEdge()

        setContent {
            CalculatorTheme {
                Scaffold(modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black))
                { innerPadding ->
                    CalculatorScreen(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = calculatorViewModel,
                        onButtonClick = { button ->
                            vibrate()
                            calculatorViewModel.updateExpression(button)
                        })
                }
            }
        }
    }

    private fun vibrate() {
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }
}

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier, viewModel: CalculatorViewModel, onButtonClick: (String) -> Unit) {
    val input = viewModel.getStringExpression()
    val result = viewModel.getEvaluated()

    val context = LocalContext.current
    var currentToast: Toast? by remember { mutableStateOf(null) }

    val toastMessage = viewModel.toastMessage

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            currentToast?.cancel()
            currentToast = Toast.makeText(context, it, Toast.LENGTH_SHORT).also { toast ->
                toast.show()
            }
            viewModel.clearToast()
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val isPortrait = maxWidth < maxHeight

        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            if (isPortrait) {
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = input,
                style = TextStyle(fontSize = 34.sp, color = Color(0xFFE8E8E8)),
                modifier = Modifier.padding(8.dp)
            )

            if (isPortrait) {
                Text(
                    text = result,
                    style = TextStyle(fontSize = 24.sp, color = Color.DarkGray),
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            IconButton(
                onClick = { onButtonClick("⌫") },
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
                onButtonClick = onButtonClick,
                isPortrait = isPortrait
            )
        }
    }
}



@Composable
fun CalculatorButtons(onButtonClick: (String) -> Unit, isPortrait: Boolean) {
    val buttons = listOf(
        listOf("C", "(", ")", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "–"),
        listOf("1", "2", "3", "+"),
        listOf("π", "0", ".", "=")
    )

    val extraButtons = listOf("sqrt", "sin", "cos", "tan", "cot")

    val buttonWidth = 80.dp
    val buttonHeight = if (isPortrait) 80.dp else 40.dp
    val fontSize = if (isPortrait) 28.sp else 16.sp


    if (isPortrait) {
        Column {
            buttons.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    row.forEach { button ->
                        CalculatorButton(
                            label = button,
                            onClick = { onButtonClick(button) },
                            buttonWidth = buttonWidth,
                            buttonHeight = buttonHeight,
                            fontSize = fontSize
                        )
                    }
                }
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxHeight(),
            horizontalArrangement = Arrangement.Start,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 64.dp),
                verticalArrangement = Arrangement.Center
            ) {
                extraButtons.forEach { button ->
                    CalculatorButton(
                        label = button,
                        onClick = { onButtonClick(button) },
                        buttonWidth = buttonWidth,
                        buttonHeight = buttonHeight,
                        fontSize = fontSize
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                buttons.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        row.forEach { button ->
                            CalculatorButton(
                                label = button,
                                onClick = { onButtonClick(button) },
                                buttonWidth = buttonWidth,
                                buttonHeight = buttonHeight,
                                fontSize = fontSize
                            )
                        }
                    }
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
    buttonWidth: Dp,
    buttonHeight: Dp,
    fontSize: TextUnit
) {
    val (backgroundColor, textColor) = when (label) {
        "⌫" -> Color(0xFF131313) to Color(0xFFE8E8E8)
        "=" -> Color(0xFF676767) to Color(0xFFE8E8E8)
        "C" -> Color(0xFF131313) to Color(0xFFB71C1C)
        "÷", "×", "–", "+" -> Color(0xFF131313) to Color(0xFFE8E8E8)
        else -> Color(0xFF131313) to Color(0xFFE8E8E8)
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .padding(4.dp)
            .width(buttonWidth)
            .height(buttonHeight),
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


