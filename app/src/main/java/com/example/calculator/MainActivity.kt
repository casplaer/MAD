package com.example.calculator

import HistoryViewModel
import android.Manifest
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
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.calculator.services.NotificationService
import com.example.calculator.viewmodels.CalculatorTheme
import com.example.calculator.viewmodels.CalculatorViewModelFactory
import com.example.calculator.viewmodels.ThemeViewModel
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {

    private val historyViewModel: HistoryViewModel by viewModels()
    private val calculatorViewModel: CalculatorViewModel by viewModels {
        CalculatorViewModelFactory(historyViewModel)
    }
    private val themeViewModel: ThemeViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val selectedExpression = intent.getStringExtra("selected_expression") ?: ""

        val notificationService = NotificationService()

        themeViewModel.loadSelectedTheme(this)

        if (selectedExpression.isNotEmpty()) {
            calculatorViewModel.updateExpression(selectedExpression)
        }

        installSplashScreen()

        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.SCHEDULE_EXACT_ALARM,
                Manifest.permission.USE_EXACT_ALARM),
                1)
        }

        setContent {
            val currentTheme = themeViewModel.currentTheme

            changeStatusBarColor(stringToColor(currentTheme.backgroundColor))

            CalculatorTheme {
                notificationService.scheduleNotificationSet(applicationContext)
                Scaffold(
                    modifier = Modifier
                                    .fillMaxSize()
                )
                { innerPadding ->
                    CalculatorScreen(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = calculatorViewModel,
                        onButtonClick = { button ->
                            vibrate()
                            calculatorViewModel.updateExpression(button)
                        },
                        currentTheme = currentTheme)
                }
            }
        }
    }

    private fun changeStatusBarColor(color: Color) {
        val window = window
        val controller = WindowInsetsControllerCompat(window, window.decorView)

        controller.isAppearanceLightStatusBars = false
        window.statusBarColor = color.toArgb()
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
fun CalculatorScreen(modifier: Modifier = Modifier, viewModel: CalculatorViewModel, onButtonClick: (String) -> Unit, currentTheme: CalculatorTheme) {
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
            .background(stringToColor(currentTheme.backgroundColor))
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
                style = TextStyle(fontSize = 34.sp, color = stringToColor(currentTheme.buttonTextColor)),
                modifier = Modifier.padding(8.dp)
            )

            if (isPortrait) {
                Text(
                    text = result,
                    style = TextStyle(fontSize = 24.sp, color = stringToColor(currentTheme.additionalTextColor)),
                    modifier = Modifier.padding(8.dp)
                )
            }

            var blankSpace = 16.dp
            if(isPortrait) blankSpace = 32.dp

            Spacer(modifier = Modifier.height(blankSpace))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val intent = Intent(context, HistoryActivity::class.java)
                    context.startActivity(intent)
                }) {
                    Image(
                        painter = painterResource(id = R.drawable.history),
                        contentDescription = "History",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(stringToColor(currentTheme.additionalTextColor))
                    )
                }

                IconButton(onClick = {
                    val intent = Intent(context, ThemeSettingsActivity::class.java)
                    context.startActivity(intent)
                }) {
                    Image(
                        painter = painterResource(id = R.drawable.settings),
                        contentDescription = "History",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(stringToColor(currentTheme.additionalTextColor))
                    )
                }

                IconButton(
                    onClick = { onButtonClick("⌫") },
                    modifier = Modifier
                        .size(32.dp)
                ) {
                    Text(
                        text = "⌫",
                        style = TextStyle(fontSize = 20.sp, color = stringToColor(currentTheme.additionalTextColor))
                    )
                }
            }

            HorizontalDivider(
                color = stringToColor(currentTheme.additionalTextColor),
                thickness = 1.dp,
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .align(Alignment.CenterHorizontally)
            )

            CalculatorButtons(
                onButtonClick = onButtonClick,
                isPortrait = isPortrait,
                currentTheme = currentTheme
            )
        }
    }
}



@Composable
fun CalculatorButtons(onButtonClick: (String) -> Unit, isPortrait: Boolean, currentTheme: CalculatorTheme) {
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
                            fontSize = fontSize,
                            currentTheme = currentTheme
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
                        fontSize = fontSize,
                        currentTheme = currentTheme
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
                                fontSize = fontSize,
                                currentTheme = currentTheme
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
    fontSize: TextUnit,
    currentTheme: CalculatorTheme
) {
    val (backgroundColor, textColor) = when (label) {
        "⌫" -> stringToColor(currentTheme.buttonBackgroundColor) to stringToColor(currentTheme.buttonTextColor)
        "=" -> stringToColor(currentTheme.equalsButtonBackgroundColor) to stringToColor(currentTheme.buttonTextColor)
        "C" -> stringToColor(currentTheme.buttonBackgroundColor) to stringToColor(currentTheme.clearButtonTextColor)
        "÷", "×", "–", "+" -> stringToColor(currentTheme.buttonBackgroundColor) to stringToColor(currentTheme.buttonTextColor)
        else -> stringToColor(currentTheme.buttonBackgroundColor) to stringToColor(currentTheme.buttonTextColor)
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

fun stringToColor(colorString: String): Color {
    return if (colorString.startsWith("#")) {
        val colorHex = colorString.removePrefix("#")
        if (colorHex.length == 6) {
            Color(
                red = Integer.valueOf(colorHex.substring(0, 2), 16) / 255f,
                green = Integer.valueOf(colorHex.substring(2, 4), 16) / 255f,
                blue = Integer.valueOf(colorHex.substring(4, 6), 16) / 255f
            )
        } else if (colorHex.length == 8) {
            Color(
                alpha = Integer.valueOf(colorHex.substring(0, 2), 16) / 255f,
                red = Integer.valueOf(colorHex.substring(2, 4), 16) / 255f,
                green = Integer.valueOf(colorHex.substring(4, 6), 16) / 255f,
                blue = Integer.valueOf(colorHex.substring(6, 8), 16) / 255f
            )
        } else {
            Color.Black
        }
    } else {
        Color.Black
    }
}
