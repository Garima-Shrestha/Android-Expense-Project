package com.example.individualproject.repository

import com.example.individualproject.model.ExpenseLogModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ExpenseLogRepositoryImpl: ExpenseLogRepository {

    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val ref: DatabaseReference = database.reference.child("expense")

    override fun addExpenseLog(
        ExpenseLogModel: ExpenseLogModel,
        callback: (Boolean, String) -> Unit
    ) {
        var id = ref.push().key.toString()
        ExpenseLogModel.LogId = id

        ref.child(id).setValue(ExpenseLogModel).addOnCompleteListener{
            if(it.isSuccessful){
                callback(true, "Expense Log Added successfully")
            }else{
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun updateExpenseLog(
        ExpenseLogId: String,
        data: MutableMap<String, Any>,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(ExpenseLogId).updateChildren(data).addOnCompleteListener{
            if(it.isSuccessful){
                callback(true, "Expense Log Updated successfully")
            }else{
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun deleteExpenseLog(ExpenseLogId: String, callback: (Boolean, String) -> Unit) {
        ref.child(ExpenseLogId).removeValue().addOnCompleteListener{
            if(it.isSuccessful){
                callback(true, "Expense Log Deleted successfully")
            }else{
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun getExpenseLogById(
        ExpenseLogId: String,
        callback: (ExpenseLogModel?, Boolean, String) -> Unit
    ) {
        ref.child(ExpenseLogId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var model = snapshot.getValue(ExpenseLogModel::class.java)
                callback(model,true, "Expense Log fetched successfully")
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null,false,error.message)
            }
        })
    }

    override fun getExpenseAllLog(callback: (List<ExpenseLogModel>?, Boolean, String) -> Unit) {
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var products= mutableListOf<ExpenseLogModel>()
                if(snapshot.exists()){
                    for(eachProduct in snapshot.children){
                        var data = eachProduct.getValue(ExpenseLogModel:: class.java)
                        if(data != null){
                            products.add(data)
                        }
                    }
                    callback(products,true,"Expense Log added successfully")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null,false,error.message)
            }
        })
    }
}
