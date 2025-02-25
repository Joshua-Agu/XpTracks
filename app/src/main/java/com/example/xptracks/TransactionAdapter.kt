package com.example.xptracks

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(private var transactions: List<Transactions>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_layout, parent, false)
        return TransactionHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: TransactionHolder,
        position: Int
    ) {
        val transaction = transactions[position]
        val context = holder.amount.context

        if(transaction.amount >= 0){
            holder.amount.text = "+${transaction.amount}"
            holder.amount.setTextColor(context.getColor(R.color.green))
        }else {
            holder.amount.text = "-%.2f".format(Math.abs(transaction.amount))
            holder.amount.setTextColor(context.getColor(R.color.red))
        }

        holder.label.text = transaction.label
    }

    override fun getItemCount(): Int {
        return transactions.size
    }
    @SuppressLint("NotifyDataSetChanged")
    fun setData(transactions: List<Transactions>){
        this.transactions = transactions
        notifyDataSetChanged()
    }

    class TransactionHolder(view: View) : RecyclerView.ViewHolder(view) {
        val label: TextView? = view.findViewById<TextView>(R.id.label)
        val amount: TextView? = view.findViewById<TextView>(R.id.cost)
    }
}























