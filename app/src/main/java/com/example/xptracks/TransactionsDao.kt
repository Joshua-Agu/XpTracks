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

    @Insert
    fun insertAll(vararg transaction: Transactions)

    @Delete
    fun delete(transaction: Transactions)

    @Update
    fun update(vararg transaction: Transactions)

}