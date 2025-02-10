package com.example.calculator.viewmodels

import HistoryViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CalculatorViewModelFactory(private val historyViewModel: HistoryViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CalculatorViewModel(historyViewModel) as T
    }
}
