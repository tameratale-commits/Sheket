package com.example.data.local

import androidx.room.*
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY timestamp DESC")
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getTransactionsByCustomerId(customerId: Int): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'SALE'")
    fun getTotalSalesFlow(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE'")
    fun getTotalExpensesFlow(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'DEBT'")
    fun getTotalDebtsFlow(): Flow<Double?>
}
