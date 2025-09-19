package com.example.xptracks

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup // Added import
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.room.Room
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NewTransactionsActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    private lateinit var addButton: Button
    private lateinit var nameEditText: EditText
    private lateinit var priceEditText: EditText
    private lateinit var descEditText: EditText
    private lateinit var backbutton: ImageView
    private lateinit var transactionTypeRadioGroup: RadioGroup
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_transactions)
        addButton = findViewById(R.id.Addbutton)
        nameEditText = findViewById(R.id.name)
        priceEditText = findViewById(R.id.Price)
        descEditText = findViewById(R.id.desc)
        backbutton = findViewById(R.id.back)
        transactionTypeRadioGroup = findViewById(R.id.transactionTypeRadioGroup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        addButton.setOnClickListener {
            val title = nameEditText.text.toString()
            var amount = priceEditText.text.toString().toDoubleOrNull()
            val description = descEditText.text.toString()

            if (title.isEmpty()) {
                nameEditText.error = "Please enter a valid title"
                return@setOnClickListener
            } else if (amount == null) {
                priceEditText.error = "Please enter a valid amount"
                return@setOnClickListener
            } else {
                val selectedTypeId = transactionTypeRadioGroup.checkedRadioButtonId
                if (selectedTypeId == R.id.expenseRadioButton) {
                    if (amount > 0) {
                        amount *= -1
                    }
                } else if (selectedTypeId == R.id.incomeRadioButton) {
                    if (amount < 0) { 
                        amount *= -1 
                    }
                }
                val transaction = Transactions(0, title, amount, description) // amount is now correctly signed
                insert(transaction)
            }
        }
        backbutton.setOnClickListener {
             backbutton.setBackgroundResource(R.drawable.butttonclick)
             backbutton.postDelayed({
                backbutton.setBackgroundResource(R.drawable.clearbackground)
             }, 300)
            finish()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun insert(transaction: Transactions) {
        val db = Room.databaseBuilder(
            this,
            Database::class.java,
            "transactions"
        ).build()
        GlobalScope.launch {
            db.transactionDao().insertAll(transaction)
            finish()
        }
    }
}
