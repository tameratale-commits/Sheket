package com.example.data.repository

import com.example.data.local.CustomerDao
import com.example.data.local.TransactionDao
import com.example.data.model.CustomerEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

class LedgerRepository(
    private val customerDao: CustomerDao,
    private val transactionDao: TransactionDao
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()

    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByType(type)

    fun getTransactionsByCustomerId(customerId: Int): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByCustomerId(customerId)

    fun searchCustomers(query: String): Flow<List<CustomerEntity>> =
        customerDao.searchCustomers(query)

    fun getCustomerByIdFlow(id: Int): Flow<CustomerEntity?> =
        customerDao.getCustomerByIdFlow(id)

    val totalSales: Flow<Double?> = transactionDao.getTotalSalesFlow()
    val totalExpenses: Flow<Double?> = transactionDao.getTotalExpensesFlow()
    val totalDebts: Flow<Double?> = transactionDao.getTotalDebtsFlow()

    suspend fun insertCustomer(customer: CustomerEntity): Long {
        return customerDao.insertCustomer(customer)
    }

    suspend fun updateCustomer(customer: CustomerEntity) {
        customerDao.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: CustomerEntity) {
        customerDao.deleteCustomer(customer)
    }

    suspend fun recordSale(amount: Double, description: String, customerId: Int? = null) {
        var custName: String? = null
        if (customerId != null) {
            custName = customerDao.getCustomerById(customerId)?.name
        }
        val transaction = TransactionEntity(
            type = "SALE",
            amount = amount,
            description = description,
            customerId = customerId,
            customerName = custName
        )
        transactionDao.insertTransaction(transaction)
    }

    suspend fun recordExpense(amount: Double, description: String) {
        val transaction = TransactionEntity(
            type = "EXPENSE",
            amount = amount,
            description = description
        )
        transactionDao.insertTransaction(transaction)
    }

    suspend fun recordDebt(customerId: Int, amount: Double, description: String) {
        val customer = customerDao.getCustomerById(customerId) ?: return
        val transaction = TransactionEntity(
            type = "DEBT",
            amount = amount,
            description = description,
            customerId = customerId,
            customerName = customer.name
        )
        // Record the transaction
        transactionDao.insertTransaction(transaction)
        // Increase the customer's current outstanding debt
        customerDao.adjustCustomerDebt(customerId, amount)
    }

    suspend fun recordDebtPayment(customerId: Int, amount: Double, description: String) {
        val customer = customerDao.getCustomerById(customerId) ?: return
        // A debt payment is cash coming in, so we record it as a SALE but with a linked customer
        val transaction = TransactionEntity(
            type = "SALE",
            amount = amount,
            description = "የእዳ ክፍያ: $description",
            customerId = customerId,
            customerName = customer.name
        )
        transactionDao.insertTransaction(transaction)
        // Decrease the customer's outstanding debt (adjust with negative amount)
        customerDao.adjustCustomerDebt(customerId, -amount)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        // If the transaction is a DEBT, deleting it should reduce the customer's debt
        if (transaction.type == "DEBT" && transaction.customerId != null) {
            customerDao.adjustCustomerDebt(transaction.customerId, -transaction.amount)
        }
        // If the transaction is a SALE (which could be a debt payment, starting with 'የእዳ ክፍያ:'),
        // and has a customerId, we need to add the debt back!
        if (transaction.type == "SALE" && transaction.customerId != null && transaction.description.startsWith("የእዳ ክፍያ:")) {
            customerDao.adjustCustomerDebt(transaction.customerId, transaction.amount)
        }
        transactionDao.deleteTransaction(transaction)
    }
}
