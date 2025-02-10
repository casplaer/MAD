package com.example.calculator.viewmodels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

data class CalculatorTheme(
    val id: String = "",
    val name: String = "Стандартная тема",
    val backgroundColor: String = "#000000",
    val buttonTextColor: String = "#E8E8E8",
    val buttonBackgroundColor: String = "#131313",
    val equalsButtonBackgroundColor: String = "#676767",
    val clearButtonTextColor: String = "#B71C1C",
    val additionalTextColor: String = "#E8E8E8"
){
    constructor() : this("", "Стандартная тема", "#000000", "#E8E8E8", "#131313", "#676767", "#B71C1C", "#E8E8E8")
}


class ThemeViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val themesRef: DatabaseReference = database.reference.child("themes")

    val defaultTheme = CalculatorTheme(
        id = "default",
        name = "Стандартная тема",
        backgroundColor = "#000000",
        buttonTextColor = "#E8E8E8",
        buttonBackgroundColor = "#131313",
        equalsButtonBackgroundColor = "#676767",
        clearButtonTextColor = "#B71C1C",
        additionalTextColor = "#3C3C3C"
    )

    var currentTheme by mutableStateOf(defaultTheme)

    var newThemeName by mutableStateOf("Новая тема")
    var newBackgroundColor by mutableStateOf(Color(0xFF5733))
    var newButtonTextColor by mutableStateOf(Color(0xFF000000))
    var newButtonBackgroundColor by mutableStateOf(Color(0xFFCCCCCC))
    var newEqualsButtonBackgroundColor by mutableStateOf(Color(0xFF0000FF))
    var newClearButtonTextColor by mutableStateOf(Color(0xFFFF0000))
    var newAdditionalTextColor by mutableStateOf(Color(0xFF00FF00))

    fun saveSelectedTheme(context: Context) {
        val sharedPreferences = context.getSharedPreferences("theme_preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("selected_theme_id", currentTheme.id)
        editor.apply()
    }

    fun loadSelectedTheme(context: Context) {
        val sharedPreferences = context.getSharedPreferences("theme_preferences", Context.MODE_PRIVATE)
        val selectedThemeId = sharedPreferences.getString("selected_theme_id", "default")
        loadThemes { loadedThemes ->
            currentTheme = loadedThemes.firstOrNull { it.id == selectedThemeId } ?: defaultTheme
        }
    }

    fun saveTheme(theme: CalculatorTheme) {
        val themeId = if (theme.id.isEmpty()) UUID.randomUUID().toString() else theme.id
        themeId?.let {
            themesRef.child(it).setValue(theme).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    println("Тема успешно сохранена!")
                } else {
                    println("Ошибка при сохранении темы: ${task.exception}")
                }
            }
        }
    }

    fun loadThemes(onThemesLoaded: (List<CalculatorTheme>) -> Unit) {
        themesRef.get().addOnSuccessListener { snapshot ->
            val themesList = mutableListOf<CalculatorTheme>()
            themesList.add(defaultTheme)
            snapshot.children.forEach { dataSnapshot ->
                val theme = dataSnapshot.getValue(CalculatorTheme::class.java)
                if (theme != null && theme.id != "default") {
                    themesList.add(theme)
                }
            }
            onThemesLoaded(themesList)
        }.addOnFailureListener { exception ->
            Log.e("ThemeVM","Ошибка при загрузке тем: ${exception.message}")
        }
    }
}
