package com.example.calculator

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.ui.theme.CalculatorTheme
import com.example.calculator.viewmodels.CalculatorTheme
import com.example.calculator.viewmodels.ThemeViewModel
import java.time.format.TextStyle
import java.util.UUID

class ThemeSettingsActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        themeViewModel.loadSelectedTheme(applicationContext)

        setContent {
            CalculatorTheme {
                ThemeSettingsScreen(viewModel = themeViewModel)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        themeViewModel.saveSelectedTheme(applicationContext)
    }
}

@Composable
fun ThemeSettingsScreen(viewModel: ThemeViewModel) {
    val context = LocalContext.current
    var themes by remember { mutableStateOf(listOf<CalculatorTheme>()) }
    var showColorPickerDialog by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(viewModel.newBackgroundColor) }
    var currentColorField by remember { mutableStateOf("backgroundColor") }

    LaunchedEffect(true) {
        viewModel.loadThemes { loadedThemes ->
            themes = if (loadedThemes.isEmpty()) {
                listOf(viewModel.defaultTheme)
            } else {
                loadedThemes
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Настройки темы",
            style = androidx.compose.ui.text.TextStyle(fontSize = 24.sp, color = Color.Black),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        themes.forEach { theme ->
            ThemeItem(
                theme = theme,
                isSelected = theme == viewModel.currentTheme,
                onClick = {
                    viewModel.currentTheme = theme
                    viewModel.saveSelectedTheme(context)
                }
            )
        }

        Text(text = "Создать новую тему", style = androidx.compose.ui.text.TextStyle(fontSize = 20.sp, color = Color.Black), modifier = Modifier.padding(top = 16.dp))

        OutlinedTextField(
            value = viewModel.newThemeName,
            onValueChange = { viewModel.newThemeName = it },
            label = { Text("Имя темы") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black)
        )

        ColorPickerField(label = "Цвет фона", color = viewModel.newBackgroundColor) {
            selectedColor = it
            currentColorField = "backgroundColor"
            showColorPickerDialog = true
        }

        ColorPickerField(label = "Цвет текста кнопок", color = viewModel.newButtonTextColor) {
            selectedColor = it
            currentColorField = "buttonTextColor"
            showColorPickerDialog = true
        }

        ColorPickerField(label = "Цвет фона кнопок", color = viewModel.newButtonBackgroundColor) {
            selectedColor = it
            currentColorField = "buttonBackgroundColor"
            showColorPickerDialog = true
        }

        ColorPickerField(label = "Цвет кнопки '='", color = viewModel.newEqualsButtonBackgroundColor) {
            selectedColor = it
            currentColorField = "equalsButtonBackgroundColor"
            showColorPickerDialog = true
        }

        ColorPickerField(label = "Цвет текста кнопки 'C'", color = viewModel.newClearButtonTextColor) {
            selectedColor = it
            currentColorField = "clearButtonTextColor"
            showColorPickerDialog = true
        }

        ColorPickerField(label = "Цвет дополнительного текста", color = viewModel.newAdditionalTextColor) {
            selectedColor = it
            currentColorField = "additionalTextColor"
            showColorPickerDialog = true
        }

        Button(
            onClick = {
                val newTheme = CalculatorTheme(
                    id = UUID.randomUUID().toString(),
                    name = viewModel.newThemeName,
                    backgroundColor = viewModel.newBackgroundColor.toHex(),
                    buttonTextColor = viewModel.newButtonTextColor.toHex(),
                    buttonBackgroundColor = viewModel.newButtonBackgroundColor.toHex(),
                    equalsButtonBackgroundColor = viewModel.newEqualsButtonBackgroundColor.toHex(),
                    clearButtonTextColor = viewModel.newClearButtonTextColor.toHex(),
                    additionalTextColor = viewModel.newAdditionalTextColor.toHex()
                )
                viewModel.saveTheme(newTheme)
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Сохранить новую тему")
        }
    }

    if (showColorPickerDialog) {
        ColorPickerDialog(
            initialColor = selectedColor,
            onColorSelected = { color ->
                when (currentColorField) {
                    "backgroundColor" -> viewModel.newBackgroundColor = color
                    "buttonTextColor" -> viewModel.newButtonTextColor = color
                    "buttonBackgroundColor" -> viewModel.newButtonBackgroundColor = color
                    "equalsButtonBackgroundColor" -> viewModel.newEqualsButtonBackgroundColor = color
                    "clearButtonTextColor" -> viewModel.newClearButtonTextColor = color
                    "additionalTextColor" -> viewModel.newAdditionalTextColor = color
                }
                selectedColor = color
                showColorPickerDialog = false
            },
            onDismiss = {
                showColorPickerDialog = false
            }
        )
    }
}

@Composable
fun ColorPickerField(label: String, color: Color, onColorChanged: (Color) -> Unit) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = label,
            style = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = Color.Black)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Box(modifier = Modifier.size(40.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { onColorChanged(color) }) {
                Text("Выбрать цвет")
            }
        }
    }
}


@Composable
fun ThemeItem(
    theme: CalculatorTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color.LightGray else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
            .background(backgroundColor), // Применяем фон
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = theme.name,
            style = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, color = Color.Black),
            modifier = Modifier.weight(1f)
        )
        if (theme.id == "default") {
            Text(
                text = "(Стандартная)",
                style = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = Color.Gray)
            )
        }
    }
}

@Composable
fun ColorPickerDialog(initialColor: Color, onColorSelected: (Color) -> Unit, onDismiss: () -> Unit) {
    var red by remember { mutableStateOf(initialColor.red * 255) }
    var green by remember { mutableStateOf(initialColor.green * 255) }
    var blue by remember { mutableStateOf(initialColor.blue * 255) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите цвет") },
        text = {
            Column {
                // Слайдеры для выбора цветов
                Text("Красный")
                Slider(
                    value = red.toFloat(),
                    onValueChange = { red = it.toInt().toFloat() },
                    valueRange = 0f..255f
                )
                Text("Зеленый")
                Slider(
                    value = green.toFloat(),
                    onValueChange = { green = it.toInt().toFloat() },
                    valueRange = 0f..255f
                )
                Text("Синий")
                Slider(
                    value = blue.toFloat(),
                    onValueChange = { blue = it.toInt().toFloat() },
                    valueRange = 0f..255f
                )

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(red / 255f, green / 255f, blue / 255f), CircleShape)
                        .padding(16.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onColorSelected(Color(red / 255f, green / 255f, blue / 255f))
            }) {
                Text("Выбрать")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

fun Color.toHex(): String {
    return "#${Integer.toHexString(this.toArgb()).substring(2).toUpperCase()}"
}
