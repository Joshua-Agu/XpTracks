package com.example.xptracks

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    private lateinit var transactions: MutableList<Transactions>
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var AddTransaction: FloatingActionButton
    private lateinit var ClearTransactions: FloatingActionButton
    private lateinit var db : Database

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.front)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.starter_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        transactions = arrayListOf()
        AddTransaction = findViewById(R.id.AddTransaction)
        transactionAdapter = TransactionAdapter(transactions)
        ClearTransactions = findViewById(R.id.clear_transaction_record)
        linearLayoutManager = LinearLayoutManager(this)
        db = Room.databaseBuilder(this, Database::class.java, "transactions").build()
        findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycleview).apply {
            adapter = transactionAdapter
            layoutManager = linearLayoutManager
        }
        AddTransaction.setOnClickListener {
            val intent = Intent(this, NewTransactionsActivity::class.java)
            startActivity(intent)
        }
        ClearTransactions.setOnClickListener {
            if (transactions.isNotEmpty()){
                transactions.clear()
                transactionAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Cleared!!", Toast.LENGTH_SHORT).show()
                updateDashboard()
            } else{
                return@setOnClickListener
            }
        }

    }
    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("NotifyDataSetChanged")
    private fun fetchAll() {
        GlobalScope.launch(Dispatchers.IO) {
            transactions = db.transactionDao().getAll() as MutableList<Transactions>
            runOnUiThread {
                updateDashboard()
                transactionAdapter.setData(transactions)
                transactionAdapter.notifyDataSetChanged()
            }
        }
    }
    private fun updateDashboard() {
        val totalAmount = transactions.sumOf { it.amount }
        val budgetAmount = transactions.filter { it.amount > 0 }.sumOf { it.amount }
        val expenseAmount = totalAmount - budgetAmount

        findViewById<TextView>(R.id.totalprice).text = "₦ %.2f".format(totalAmount)
        findViewById<TextView>(R.id.budgetValue).text = "₦ %.2f".format(budgetAmount)
        findViewById<TextView>(R.id.expenseValue).text = "₦ %.2f".format(expenseAmount)
    }

    override fun onResume() {
        super.onResume()
        fetchAll()
    }
}