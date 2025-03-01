package com.example.individualproject.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.individualproject.R
import com.example.individualproject.adapter.ExpenseCalculatorAdapter
import com.example.individualproject.model.ExpenseCalculatorModel
import com.example.individualproject.repository.ExpenseCalculatorRepositoryImpl
import com.google.android.material.snackbar.Snackbar


class ExpenseCalculatorFragment : Fragment() {

    private lateinit var editAmount: EditText
    private lateinit var editDescription: EditText
    private lateinit var btnCalculate: Button
    private lateinit var textTotalExpense: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var expenseAdapter: ExpenseCalculatorAdapter
    private val repository = ExpenseCalculatorRepositoryImpl()
    private var expenseList: MutableList<ExpenseCalculatorModel> = mutableListOf()
    private var isUpdateMode = false // Flag to check if in update mode
    private lateinit var currentExpense: ExpenseCalculatorModel // To hold the current expense being updated

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_expense_calculator, container, false)

        // Initialize Views
        editAmount = view.findViewById(R.id.editAmount)
        editDescription = view.findViewById(R.id.editDescription)
        btnCalculate = view.findViewById(R.id.btnCalculate)
        textTotalExpense = view.findViewById(R.id.textTotalExpense)
        recyclerView = view.findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(context)
        expenseAdapter = ExpenseCalculatorAdapter(expenseList, ::onUpdateExpense)
        recyclerView.adapter = expenseAdapter

        // Fetch and display all expenses
        fetchExpenses()

        // Setup swipe to delete
        setupSwipeToDelete()

        // Button Click Listener to add a new expense
        btnCalculate.setOnClickListener {
            if (isUpdateMode) {
                updateExpense()
            } else {
                addExpense()
            }
        }

        return view
    }

    private fun addExpense() {
        val amount = editAmount.text.toString().toDoubleOrNull()
        val description = editDescription.text.toString()
        val date = getCurrentDate()

        // Check if amount and description are valid
        if (amount != null && amount > 0 && description.isNotBlank()) {
            val expense = ExpenseCalculatorModel(
                id = "", // ID will be generated in Firebase
                calExpenseAmount = amount,
                calExpenseDescription = description,
                calExpenseDate = date
            )

            repository.addExpenseCalculator(expense) { success, message ->
                if (success) {
                    fetchExpenses() // Fetch the updated list of expenses
                    clearInputFields() // Clear input fields after adding
                    Snackbar.make(requireView(), "Expense Added Successfully!", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(requireView(), "Error: $message", Snackbar.LENGTH_SHORT).show()
                }
            }
        } else {
            Snackbar.make(requireView(), "Please enter a valid amount and description", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun updateExpense() {
        val updatedAmount = editAmount.text.toString().toDoubleOrNull()
        val updatedDescription = editDescription.text.toString()

        if (updatedAmount != null && updatedDescription.isNotBlank()) {
            val updatedData = mutableMapOf<String, Any>(
                "calExpenseAmount" to updatedAmount, // Ensure this matches Firebase keys
                "calExpenseDescription" to updatedDescription, // Ensure this matches Firebase keys
                "calExpenseDate" to currentExpense.calExpenseDate // Keep the original date or update it if needed
            )

            repository.updateExpenseCalculator(currentExpense.id, updatedData) { success, message ->
                if (success) {
                    fetchExpenses()
                    clearInputFields() // Clear input fields after updating
                    btnCalculate.text = "Add Expense" // Reset button text
                    isUpdateMode = false // Reset update mode
                    Snackbar.make(requireView(), "Expense Updated Successfully!", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(requireView(), "Error: $message", Snackbar.LENGTH_SHORT).show()
                }
            }
        } else {
            Snackbar.make(requireView(), "Please enter valid amount and description", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun clearInputFields() {
        editAmount.text.clear()
        editDescription.text.clear()
    }

    private fun fetchExpenses() {
        repository.getExpenseAllCalculator { expenses, success, message ->
            if (success) {
                expenses?.let {
                    expenseList.clear()
                    expenseList.addAll(it)
                    expenseAdapter.notifyDataSetChanged()
                    calculateTotalExpense() // Calculate total expense after fetching
                }
            } else {
                Snackbar.make(requireView(), "Error: $message", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun calculateTotalExpense() {
        val total = expenseList.sumOf { it.calExpenseAmount }
        textTotalExpense.text = "Total Expense: Rs. $total"
    }

    private fun onUpdateExpense(expense: ExpenseCalculatorModel) {
        // Populate the EditTexts with the selected expense details
        editAmount.setText(expense.calExpenseAmount.toString())
        editDescription.setText(expense.calExpenseDescription)

        // Change the button text to "Update Expense"
        btnCalculate.text = "Update Expense"

        // Set the update mode flag and store the current expense
        isUpdateMode = true
        currentExpense = expense
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelper = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val expenseToDelete = expenseList[position]
                onDeleteExpense(expenseToDelete)
            }
        }
        ItemTouchHelper(itemTouchHelper).attachToRecyclerView(recyclerView)
    }

    private fun onDeleteExpense(expense: ExpenseCalculatorModel) {
        repository.deleteExpenseCalculator(expense.id) { success, message ->
            if (success) {
                fetchExpenses()
                Snackbar.make(requireView(), "Expense Deleted Successfully!", Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(requireView(), "Error: $message", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date())
    }
}