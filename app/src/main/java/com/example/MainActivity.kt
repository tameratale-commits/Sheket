package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.repository.LedgerRepository
import com.example.ui.LedgerScreen
import com.example.ui.LedgerViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Initialize Database & Repository
    val database = AppDatabase.getDatabase(this)
    val repository = LedgerRepository(database.customerDao(), database.transactionDao())

    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          // Initialize ViewModel in Compose with Factory
          val ledgerViewModel: LedgerViewModel = viewModel(
            factory = LedgerViewModel.Factory(repository)
          )
          
          LedgerScreen(viewModel = ledgerViewModel)
        }
      }
    }
  }
}
