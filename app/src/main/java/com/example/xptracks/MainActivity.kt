package com.example.xptracks

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")

    private lateinit var oldTransaction: Transactions
    private lateinit var transactions: MutableList<Transactions>
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var addTransaction: FloatingActionButton
    private lateinit var clearTransactions: FloatingActionButton
    private lateinit var db : Database
    private lateinit var recyclerView: RecyclerView // Added for easier access

    @OptIn(DelicateCoroutinesApi::class)
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
        addTransaction = findViewById(R.id.AddTransaction)
        transactionAdapter = TransactionAdapter(transactions)
        clearTransactions = findViewById(R.id.clear_transaction_record)
        linearLayoutManager = LinearLayoutManager(this)
        db = Room.databaseBuilder(this, Database::class.java, "transactions").build()

        recyclerView = findViewById<RecyclerView>(R.id.recycleview).apply {
            adapter = transactionAdapter
            layoutManager = linearLayoutManager
        }

        // Delay for RecyclerView layout animation
        Handler(Looper.getMainLooper()).postDelayed({
            val context = this@MainActivity
            val controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_slide_in)
            recyclerView.layoutAnimation = controller
            recyclerView.scheduleLayoutAnimation()
        }, 100)


        addTransaction.setOnClickListener {
            val intent = Intent(this, NewTransactionsActivity::class.java)
            startActivity(intent)
        }
        clearTransactions.setOnClickListener {
            if (transactions.isNotEmpty()) {
                GlobalScope.launch(Dispatchers.IO) {
                    db.transactionDao().deleteAll()
                    runOnUiThread {
                        transactions.clear()
                        transactionAdapter.setData(transactions)
                        updateDashboard()
                        Toast.makeText(this@MainActivity, "All transactions cleared!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
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
            }
        }
        val itemTouchHelper =
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) { // Allow both directions if you want
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    @Suppress("DEPRECATION")
                    val position = viewHolder.adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val transactionToDelete = transactions[position]

                        transactions.removeAt(position)
                        transactionAdapter.notifyItemRemoved(position)

                        deleteTransactionFromDatabase(transactionToDelete)
                        showSnackbar()
                    }
                }
            })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
    @OptIn(DelicateCoroutinesApi::class)
    private fun undoDelete() {
        GlobalScope.launch(Dispatchers.IO) {
            db.transactionDao().insertAll(oldTransaction)
            runOnUiThread {
                transactions.add(oldTransaction)
                transactionAdapter.notifyItemInserted(transactions.lastIndex)
                updateDashboard()
            }
        }
    }
    @SuppressLint("ShowToast")
    private fun showSnackbar() {
        val view = findViewById<View>(R.id.cordinator)
        val snackbar = Snackbar.make(view,"Transaction Deleted", Snackbar.LENGTH_LONG)
        snackbar.setAction("Undo"){
            undoDelete()
        }
            .setActionTextColor(ContextCompat.getColor(this, R.color.red))
            .setTextColor(ContextCompat.getColor(this, R.color.deep_space_blue))
            .show()
    }
    @OptIn(DelicateCoroutinesApi::class)
    private fun deleteTransactionFromDatabase(transaction: Transactions) {
        oldTransaction = transaction
        GlobalScope.launch(Dispatchers.IO) {
            db.transactionDao().delete(transaction)
            runOnUiThread {
                updateDashboard()
            }
        }
    }
    @SuppressLint("SetTextI18n")
    private fun updateDashboard() {
        val totalAmount = transactions.sumOf { it.amount }
        val budgetAmount = transactions.filter { it.amount > 0 }.sumOf { it.amount }
        val rawExpenseAmount = transactions.filter { it.amount < 0 }.sumOf { it.amount }


        findViewById<TextView>(R.id.totalprice).text = "₦ %.2f".format(totalAmount)
        findViewById<TextView>(R.id.budgetValue).text = "₦ %.2f".format(budgetAmount)
        findViewById<TextView>(R.id.expenseValue).text = "₦ %.2f".format(kotlin.math.abs(rawExpenseAmount))
    }



    override fun onResume() {
        super.onResume()
        fetchAll()
    }
}