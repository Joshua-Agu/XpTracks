package com.example.xptracks

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.room.Room
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailsActivity : AppCompatActivity() {

    private lateinit var updateButton: Button
    private lateinit var rootLayout: ConstraintLayout
    private lateinit var nameEditText: EditText
    private lateinit var priceEditText: EditText
    private lateinit var descEditText: EditText
    private lateinit var backButton: ImageView
    private lateinit var transactionTypeRadioGroup: RadioGroup

    private var currentTransaction: Transactions? = null
    private var viewsAlreadyMadeVisible = false

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_details)

        updateButton = findViewById(R.id.Updatebutton)
        nameEditText = findViewById(R.id.name)
        priceEditText = findViewById(R.id.Price)
        rootLayout = findViewById(R.id.rootlayout)
        descEditText = findViewById(R.id.desc)
        backButton = findViewById(R.id.back)
        transactionTypeRadioGroup = findViewById(R.id.transactionTypeRadioGroup)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        @Suppress("DEPRECATION")
        currentTransaction = intent.getSerializableExtra("transaction") as? Transactions

        if (currentTransaction == null) {
            Log.e("DetailsActivity", "Transaction data is null. Finishing activity.")
            finish()
            return
        }


        currentTransaction?.let {
            nameEditText.setText(it.label)
            priceEditText.setText(it.amount.toString())
            descEditText.setText(it.decription)

            if (it.amount < 0) {
                transactionTypeRadioGroup.check(R.id.expenseRadioButton)
            } else {
                transactionTypeRadioGroup.check(R.id.incomeRadioButton)
            }
        }


        updateButton.visibility = View.GONE
        transactionTypeRadioGroup.visibility = View.GONE


        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                showUpdateElementsIfNeeded()
            }
        }

        nameEditText.addTextChangedListener(textWatcher)
        priceEditText.addTextChangedListener(textWatcher)
        descEditText.addTextChangedListener(textWatcher)
        transactionTypeRadioGroup.setOnCheckedChangeListener { _, _ ->
            showUpdateElementsIfNeeded()
        }
        rootLayout.setOnClickListener {
            this.window.decorView.clearFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(rootLayout.windowToken, 0)

        }

        updateButton.setOnClickListener {
            val title = nameEditText.text.toString()
            var amount = priceEditText.text.toString().toDoubleOrNull()
            val description = descEditText.text.toString()

            if (title.isEmpty()) {
                nameEditText.error = "Please enter a valid title"
                return@setOnClickListener
            }
            if (amount == null) {
                priceEditText.error = "Please enter a valid amount"
                return@setOnClickListener
            }

            currentTransaction?.let {
                val selectedTypeId = transactionTypeRadioGroup.checkedRadioButtonId
                if (selectedTypeId == R.id.expenseRadioButton) {
                    if (amount > 0) amount *= -1
                } else if (selectedTypeId == R.id.incomeRadioButton) {
                    if (amount < 0) amount *= -1
                }

                val updatedTransaction = Transactions(it.id, title, amount, description)
                updateDatabase(updatedTransaction)
            }
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun showUpdateElementsIfNeeded() {
        if (!viewsAlreadyMadeVisible) {
            viewsAlreadyMadeVisible = true

            updateButton.apply {
                alpha = 0f
                visibility = View.VISIBLE
                animate().alpha(1f).setDuration(300).start()
            }
            transactionTypeRadioGroup.apply {
                alpha = 0f
                visibility = View.VISIBLE
                animate().alpha(1f).setDuration(300).start()
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun updateDatabase(transaction: Transactions) {
        val db = Room.databaseBuilder(
            applicationContext, // Use applicationContext for Room
            Database::class.java,
            "transactions"
        ).build()

        GlobalScope.launch(Dispatchers.IO) {
            db.transactionDao().update(transaction)
            withContext(Dispatchers.Main) {
                finish()
            }
        }
    }
}
