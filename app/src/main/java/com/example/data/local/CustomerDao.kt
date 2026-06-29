package com.example.data.local

import androidx.room.*
import com.example.data.model.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Int): CustomerEntity?

    @Query("SELECT * FROM customers WHERE id = :id")
    fun getCustomerByIdFlow(id: Int): Flow<CustomerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET currentDebt = currentDebt + :debtDelta WHERE id = :id")
    suspend fun adjustCustomerDebt(id: Int, debtDelta: Double)
}
