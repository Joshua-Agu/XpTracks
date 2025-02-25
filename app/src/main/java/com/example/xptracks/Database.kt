package com.example.xptracks
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Transactions::class], version = 1)
abstract class Database : RoomDatabase() {
    abstract fun transactionDao(): TransactionsDao

}