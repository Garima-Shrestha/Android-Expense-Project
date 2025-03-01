package com.example.individualproject.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.individualproject.R
import com.example.individualproject.model.ExpenseLogModel
import com.example.individualproject.ui.activity.UpdateLogActivity

class ExpenseLogAdapter(
    var context: Context,
    var data: ArrayList<ExpenseLogModel>
) : RecyclerView.Adapter<ExpenseLogAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eAmount: TextView = itemView.findViewById(R.id.displayAmount)
        val eCategory: TextView = itemView.findViewById(R.id.displayCategory)
        val eDate: TextView = itemView.findViewById(R.id.displayDate)
        val ePurpose: TextView = itemView.findViewById(R.id.displayPurpose)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val itemView: View = LayoutInflater.from(context)
            .inflate(R.layout.sample_expense, parent, false)
        return ExpenseViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = data[position]
        holder.eAmount.text = expense.ExpenseAmount.toString()
        holder.eCategory.text = expense.ExpenseCategory
        holder.eDate.text = expense.ExpenseDate
        holder.ePurpose.text = expense.ExpensePurpose


        holder.progressBar.visibility = View.GONE

        holder.itemView.setOnClickListener {
            val intent = Intent(context, UpdateLogActivity::class.java)
            intent.putExtra("logId", expense.LogId)
            context.startActivity(intent)
        }
    }

    fun updateData(expense: List<ExpenseLogModel>) {
        data.clear()
        data.addAll(expense)
        notifyDataSetChanged()
    }

    fun getExpenseLogId(position: Int): String {
        return data[position].LogId
    }
}