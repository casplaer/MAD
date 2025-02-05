package com.example.calculator.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class CalculatorViewModel : ViewModel() {

    private var currentExpression by mutableStateOf("0")
    private var currentResult by mutableStateOf("0")
    private var isCurrentExpressionSaved = false

    fun getStringExpression(): String {
        return currentExpression
    }

    fun getEvaluated(): String {
        if(currentResult.endsWith(".0"))
            currentResult.dropLast(2)
        return when (currentResult) {
            "Infinity" -> "∞"
            "NaN" -> "Error"
            else -> currentResult
        }
    }

    fun updateExpression(symbol: String) {
        when (symbol) {
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
            "sin", "cos", "tan", "cot", "sqrt", "(", ")", "." -> {
                isCurrentExpressionSaved = false
                addDigit(symbol)
            }
            "C" -> {
                clearExpression()
            }
            "⌫" -> clearLastSymbolOfExpression()
            "×", "–", "÷", "+" -> {
                addDigit(symbol)
            }
            "=" -> {
                if (currentResult != "Error") {
                    currentExpression = currentResult
                }
            }
        }

        try {
            evaluateExpression()
        } catch (_: Exception) {
            currentResult = "Error"
        }
    }

    private fun addDigit(symbol: String) {
        if (currentExpression == "0") {
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
}
