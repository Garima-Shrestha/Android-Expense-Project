package com.example.individualproject.repository

import com.example.individualproject.model.ExpenseCalculatorModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ExpenseCalculatorRepositoryImpl : ExpenseCalculatorRepository {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val usersRef: DatabaseReference = database.reference.child("users")

    override fun addExpenseCalculator(
        expenseCalculatorModel: ExpenseCalculatorModel,
        callback: (Boolean, String) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser ?.uid
        if (userId != null) {
            val expenseId = usersRef.child(userId).child("expense_calculator").push().key ?: return callback(false, "Failed to generate ID")
            val newExpense = expenseCalculatorModel.copy(id = expenseId)

            usersRef.child(userId).child("expense_calculator").child(expenseId).setValue(newExpense).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Expense added successfully")
                } else {
                    callback(false, task.exception?.message ?: "Error adding expense")
                }
            }
        } else {
            callback(false, "User  not authenticated")
        }
    }

    override fun updateExpenseCalculator(
        expenseCalculatorId: String,
        data: MutableMap<String, Any>,
        callback: (Boolean, String) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser ?.uid
        if (userId != null) {
            usersRef.child(userId).child("expense_calculator").child(expenseCalculatorId).updateChildren(data).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Expense updated successfully")
                } else {
                    callback(false, task.exception?.message ?: "Error updating expense")
                }
            }
        } else {
            callback(false, "User  not authenticated")
        }
    }

    override fun deleteExpenseCalculator(
        expenseCalculatorId: String,
        callback: (Boolean, String) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser ?.uid
        if (userId != null) {
            usersRef.child(userId).child("expense_calculator").child(expenseCalculatorId).removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Expense deleted successfully")
                } else {
                    callback(false, task.exception?.message ?: "Error deleting expense")
                }
            }
        } else {
            callback(false, "User  not authenticated")
        }
    }

    override fun getExpenseCalculatorById(
        expenseCalculatorId: String,
        callback: (ExpenseCalculatorModel?, Boolean, String) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser ?.uid
        if (userId != null) {
            usersRef.child(userId).child("expense_calculator").child(expenseCalculatorId).addListenerForSingleValueEvent(object : ValueEventListener {
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
        } else {
            callback(null, false, "User  not authenticated")
        }
    }

    override fun getExpenseAllCalculator(
        callback: (List<ExpenseCalculatorModel>?, Boolean, String) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser ?.uid
        if (userId != null) {
            usersRef.child(userId).child("expense_calculator").addListenerForSingleValueEvent(object : ValueEventListener {
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
        } else {
            callback(null, false, "User  not authenticated")
        }
    }
}