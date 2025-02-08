package com.example.calculator.viewmodels

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel

class CalculatorViewModel : ViewModel() {

    var currentExpression by mutableStateOf("0")
        private set
    var currentResult by mutableStateOf("0")
        private set
    var toastMessage by mutableStateOf<String?>(null)
        private set

    private var isCurrentExpressionSaved = false

    fun getStringExpression(): String {
        return currentExpression
    }

    fun getEvaluated(): String {
        if (currentResult.endsWith(".0"))
            currentResult = currentResult.dropLast(2)

        return when (currentResult) {
            "Infinity" -> "∞"
            "NaN" -> "Error"
            else -> currentResult
        }
    }

    fun updateExpression(symbol: String) {
        when (symbol) {
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
            "sin", "cos", "tan", "cot", "sqrt", "(", ")", ".", "π" -> {
                isCurrentExpressionSaved = false
                addDigit(symbol)
            }
            "C" -> clearExpression()
            "⌫" -> clearLastSymbolOfExpression()
            "×", "–", "÷", "+" -> addDigit(symbol)
            "=" -> if (currentResult != "Error") currentExpression = currentResult
        }

        try {
            evaluateExpression()
        } catch (_: Exception) {
            currentResult = "Error"
        }
    }

    private fun addDigit(symbol: String) {
        val lastNumber = currentExpression.split(Regex("[+\\-×÷()]")).lastOrNull() ?: ""

        if (symbol in "0123456789") {
            if (lastNumber.length >= 15) {
                showToast("Невозможно ввести более 15 цифр в одном числе")
                return
            }
        }

        if (symbol == ".") {
            val parts = lastNumber.split(".")
            if (parts.size > 1 && parts[1].length >= 10) {
                showToast("Максимум 10 цифр после запятой")
                return
            }
        }

        if (currentExpression == "0" && symbol != ".") {
            currentExpression = symbol
        } else {
            currentExpression += symbol
        }
    }


    private fun clearExpression() {
        currentExpression = "0"
    }

    private fun clearLastSymbolOfExpression() {
        currentExpression = currentExpression.dropLast(1)
        if (currentExpression.isEmpty()) {
            currentExpression = "0"
        }
    }

    private fun prepareForEvaluation(raw: String): String {
        return raw
            .replace("sin", "Math.sin")
            .replace("cos", "Math.cos")
            .replace("tan", "Math.tan")
            .replace("cot", "cot")
            .replace("sqrt", "Math.sqrt")
            .replace("π", "Math.PI")
            .replace("–", "-")
            .replace("×", "*")
            .replace("÷", "/")
            .replace("+", "+")
    }

    private fun evaluateExpression() {
        val context = org.mozilla.javascript.Context.enter()
        context.optimizationLevel = -1
        val scope = context.initStandardObjects()

        val prepared = prepareForEvaluation(currentExpression)

        val cotFunc =
            "function cot(x) {" +
                    "   return 1/Math.tan(x);" +
                    "}"

        currentResult = try {
            val tmp = context.evaluateString(scope, cotFunc + prepared, "JavaScript", 1, null)
            if (tmp is Double) {
                tmp.toString()
            } else {
                "Error"
            }
        } catch (_: Exception) {
            "Error"
        } finally {
            org.mozilla.javascript.Context.exit()
        }
    }

    private fun showToast(message: String) {
        toastMessage = message
    }

    fun clearToast() {
        toastMessage = null
    }
}
