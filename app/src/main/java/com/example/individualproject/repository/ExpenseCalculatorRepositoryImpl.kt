package com.example.individualproject.repository

import com.example.individualproject.model.ExpenseCalculatorModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ExpenseCalculatorRepositoryImpl : ExpenseCalculatorRepository {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.reference.child("Expense Calculator")

    override fun addExpenseCalculator(
        expenseCalculatorModel: ExpenseCalculatorModel,
        callback: (Boolean, String) -> Unit
    ) {
        val expenseId = ref.push().key ?: return callback(false, "Failed to generate ID")
        val newExpense = expenseCalculatorModel.copy(id = expenseId)

        ref.child(expenseId).setValue(newExpense).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, "Expense added successfully")
            } else {
                callback(false, task.exception?.message ?: "Error adding expense")
            }
        }
    }

    override fun updateExpenseCalculator(
        ExpenseCalculatorId: String,
        data: MutableMap<String, Any>,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(ExpenseCalculatorId).updateChildren(data).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, "Expense updated successfully")
            } else {
                callback(false, task.exception?.message ?: "Error updating expense")
            }
        }
    }

    override fun deleteExpenseCalculator(
        ExpenseCalculatorId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(ExpenseCalculatorId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, "Expense deleted successfully")
            } else {
                callback(false, task.exception?.message ?: "Error deleting expense")
            }
        }
    }

    override fun getExpenseCalculatorById(
        ExpenseCalculatorId: String,
        callback: (ExpenseCalculatorModel?, Boolean, String) -> Unit
    ) {
        ref.child(ExpenseCalculatorId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val expense = snapshot.getValue(ExpenseCalculatorModel::class.java)
                if (expense != null) {
                    callback(expense, true, "Expense retrieved successfully")
                } else {
                    callback(null, false, "Expense not found")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null, false, "Error retrieving expense: ${error.message}")
            }
        })
    }

    override fun getExpenseAllCalculator(
        callback: (List<ExpenseCalculatorModel>?, Boolean, String) -> Unit
    ) {
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val expenseList = mutableListOf<ExpenseCalculatorModel>()
                for (expenseSnapshot in snapshot.children) {
                    val expense = expenseSnapshot.getValue(ExpenseCalculatorModel::class.java)
                    expense?.let { expenseList.add(it) }
                }
                if (expenseList.isNotEmpty()) {
                    callback(expenseList, true, "Expenses retrieved successfully")
                } else {
                    callback(null, false, "No expenses found")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null, false, "Error retrieving expenses: ${error.message}")
            }
        })
    }
}