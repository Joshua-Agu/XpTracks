package com.example.xptracks

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TransactionsDao {
    @Query("SELECT * from transactions")
    fun getAll(): List<Transactions>

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Insert
    suspend fun insertAll(vararg transaction: Transactions)

    @Delete
    suspend fun delete(transaction: Transactions)

    @Update
    suspend fun update(vararg transaction: Transactions)
}