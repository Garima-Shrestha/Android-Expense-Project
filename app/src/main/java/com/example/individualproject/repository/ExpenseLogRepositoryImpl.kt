package com.example.individualproject.repository

import com.example.individualproject.model.ExpenseLogModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ExpenseLogRepositoryImpl : ExpenseLogRepository {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val usersRef: DatabaseReference = database.reference.child("users")

    override fun addExpenseLog(
        expenseLogModel: ExpenseLogModel,
        callback: (Boolean, String) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser ?.uid
        if (userId != null) {
            val id = usersRef.child(userId).child("expenses").push().key.toString()
            expenseLogModel.LogId = id

            usersRef.child(userId).child("expenses").child(id).setValue(expenseLogModel).addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Expense Log Added successfully")
                } else {
                    callback(false, "${it.exception?.message}")
                }
            }
        } else {
            callback(false, "User  not authenticated")
        }
    }

    override fun updateExpenseLog(
        expenseLogId: String,
        data: MutableMap<String, Any>,
        callback: (Boolean, String) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser ?.uid
        if (userId != null) {
            usersRef.child(userId).child("expenses").child(expenseLogId).updateChildren(data).addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Expense Log Updated successfully")
                } else {
                    callback(false, "${it.exception?.message}")
                }
            }
        } else {
            callback(false, "User  not authenticated")
        }
    }

    override fun deleteExpenseLog(expenseLogId: String, callback: (Boolean, String) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser ?.uid
        if (userId != null) {
            usersRef.child(userId).child("expenses").child(expenseLogId).removeValue().addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Expense Log Deleted successfully")
                } else {
                    callback(false, "${it.exception?.message}")
                }
            }
        } else {
            callback(false, "User  not authenticated")
        }
    }

    override fun getExpenseLogById(
        expenseLogId: String,
        callback: (ExpenseLogModel?, Boolean, String) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser ?.uid
        if (userId != null) {
            usersRef.child(userId).child("expenses").child(expenseLogId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val model = snapshot.getValue(ExpenseLogModel::class.java)
                    callback(model, true, "Expense Log fetched successfully")
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null, false, error.message)
                }
            })
        } else {
            callback(null, false, "User  not authenticated")
        }
    }

    override fun getExpenseAllLog(callback: (List<ExpenseLogModel>?, Boolean, String) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser ?.uid
        if (userId != null) {
            usersRef.child(userId).child("expenses").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val expenses = mutableListOf<ExpenseLogModel>()
                    if (snapshot.exists()) {
                        for (eachExpense in snapshot.children) {
                            val data = eachExpense.getValue(ExpenseLogModel::class.java)
                            if (data != null) {
                                expenses.add(data)
                            }
                        }
                        callback(expenses, true, "Expense Logs fetched successfully")
                    } else {
                        callback(emptyList(), true, "No Expense Logs found")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null, false, error.message)
                }
            })
        } else {
            callback(null, false, "User  not authenticated")
        }
    }
}