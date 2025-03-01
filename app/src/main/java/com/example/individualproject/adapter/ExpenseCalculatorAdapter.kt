package com.example.individualproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.individualproject.R
import com.example.individualproject.model.ExpenseCalculatorModel


class ExpenseCalculatorAdapter(
    private val expenses: List<ExpenseCalculatorModel>,
    private val onUpdateExpense: (ExpenseCalculatorModel) -> Unit
) : RecyclerView.Adapter<ExpenseCalculatorAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvExpenseAmount: TextView = itemView.findViewById(R.id.tvExpenseAmount)
        val tvExpenseDescription: TextView = itemView.findViewById(R.id.tvExpenseDescription)
        val tvExpenseDate: TextView = itemView.findViewById(R.id.tvExpenseDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return expenses.size
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.tvExpenseAmount.text = "Rs. ${expense.calExpenseAmount}"
        holder.tvExpenseDescription.text = expense.calExpenseDescription
        holder.tvExpenseDate.text = expense.calExpenseDate

        // Handle update action
        holder.itemView.setOnClickListener {
            onUpdateExpense(expense)
        }
    }
}