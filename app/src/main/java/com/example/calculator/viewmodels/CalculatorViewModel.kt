package com.example.calculator.viewmodels

import HistoryViewModel
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel

class CalculatorViewModel(private val historyViewModel: HistoryViewModel) : ViewModel() {

    var currentExpression by mutableStateOf("0")
        private set
    var currentResult by mutableStateOf("0")
        private set
    var toastMessage by mutableStateOf<String?>(null)
        private set

    private var isSaved = false

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
                isSaved = false
                addDigit(symbol)
            }
            "C" -> clearExpression()
            "⌫" -> clearLastSymbolOfExpression()
            "×", "–", "÷", "+" -> addDigit(symbol)
            "=" -> {
                if (!isSaved) {
                    historyViewModel.saveHistoryToFirebase(currentExpression)
                    isSaved = true
                }

                if (currentResult != "Error") {
                    currentExpression = currentResult
                }
            }
            else ->{
                currentExpression = symbol
            }
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
        val commandList = listOf("sin", "cos", "tan", "cot", "sqrt")
        val lastCommand = commandList.find { currentExpression.endsWith(it) }

        if (lastCommand != null) {
            currentExpression = currentExpression.dropLast(lastCommand.length)
        } else {
            currentExpression = currentExpression.dropLast(1)
        }

        if (currentExpression.isEmpty()) {
            currentExpression = "0"
        }
    }


    private fun prepareForEvaluation(raw: String): String {
        var expression = raw

        expression = expression.replace(Regex("([0-9)])(\\(|sin|cos|tg|ctg|sqrt)"), "$1*$2")

        return expression
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
