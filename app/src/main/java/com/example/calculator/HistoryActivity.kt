package com.example.calculator

import HistoryEntry
import HistoryViewModel
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.ui.theme.CalculatorTheme

class HistoryActivity : ComponentActivity() {

    private val historyViewModel: HistoryViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CalculatorTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(title = { Text("История") })
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    HistoryScreen(modifier = Modifier.padding(innerPadding), viewModel = historyViewModel)
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(modifier: Modifier = Modifier, viewModel: HistoryViewModel) {
    var historyList by remember { mutableStateOf<List<HistoryEntry>>(emptyList()) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadHistoryFromFirebase { history ->
            historyList = history
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        LazyColumn {
            items(historyList) { entry ->
                HistoryItem(entry){
                    val intent = Intent(context, MainActivity::class.java)
                    intent.putExtra("selected_expression", entry.expression)
                    context.startActivity(intent)
                }
                Divider(color = Color.Gray, thickness = 1.dp)
            }
        }
    }
}

@Composable
fun HistoryItem(entry: HistoryEntry, onItemClick: (HistoryEntry) -> Unit) {
    Text(
        text = entry.expression,
        fontSize = 24.sp,
        color = Color.White,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .clickable { onItemClick(entry) },
    )
}

