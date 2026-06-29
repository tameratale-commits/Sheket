package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.CustomerEntity
import com.example.data.model.TransactionEntity
import com.example.data.repository.LedgerRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LedgerViewModel(private val repository: LedgerRepository) : ViewModel() {

    // Transactions list
    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Customer search query
    private val _customerSearchQuery = MutableStateFlow("")
    val customerSearchQuery = _customerSearchQuery.asStateFlow()

    // Customers list, filtered by search query
    val customers: StateFlow<List<CustomerEntity>> = _customerSearchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allCustomers
            } else {
                repository.searchCustomers(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Financial summaries
    val totalSales: StateFlow<Double> = repository.totalSales
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpenses: StateFlow<Double> = repository.totalExpenses
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalDebts: StateFlow<Double> = repository.totalDebts
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Selected customer for detail view
    private val _selectedCustomerId = MutableStateFlow<Int?>(null)
    val selectedCustomerId = _selectedCustomerId.asStateFlow()

    val selectedCustomer: StateFlow<CustomerEntity?> = _selectedCustomerId
        .flatMapLatest { id ->
            if (id != null) {
                repository.getCustomerByIdFlow(id)
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedCustomerTransactions: StateFlow<List<TransactionEntity>> = _selectedCustomerId
        .flatMapLatest { id ->
            if (id != null) {
                repository.getTransactionsByCustomerId(id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun searchCustomers(query: String) {
        _customerSearchQuery.value = query
    }

    fun selectCustomer(customerId: Int?) {
        _selectedCustomerId.value = customerId
    }

    fun addCustomer(name: String, phone: String, notes: String = "", initialDebt: Double = 0.0) {
        viewModelScope.launch {
            val customerId = repository.insertCustomer(
                CustomerEntity(
                    name = name,
                    phone = phone,
                    currentDebt = 0.0, // We will record a separate debt transaction if initial debt > 0
                    notes = notes
                )
            )
            if (initialDebt > 0.0) {
                repository.recordDebt(
                    customerId = customerId.toInt(),
                    amount = initialDebt,
                    description = "የመጀመሪያ እዳ (Initial Debt)"
                )
            }
        }
    }

    fun updateCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            if (_selectedCustomerId.value == customer.id) {
                _selectedCustomerId.value = null
            }
        }
    }

    fun recordSale(amount: Double, description: String, customerId: Int? = null) {
        viewModelScope.launch {
            repository.recordSale(amount, description, customerId)
        }
    }

    fun recordExpense(amount: Double, description: String) {
        viewModelScope.launch {
            repository.recordExpense(amount, description)
        }
    }

    fun recordDebt(customerId: Int, amount: Double, description: String) {
        viewModelScope.launch {
            repository.recordDebt(customerId, amount, description)
        }
    }

    fun recordDebtPayment(customerId: Int, amount: Double, description: String) {
        viewModelScope.launch {
            repository.recordDebtPayment(customerId, amount, description)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    // ViewModel Factory helper
    class Factory(private val repository: LedgerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LedgerViewModel::class.java)) {
                return LedgerViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
