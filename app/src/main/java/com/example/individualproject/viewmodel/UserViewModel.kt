package com.example.individualproject.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.individualproject.model.UserModel
import com.example.individualproject.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UserViewModel( val repo: UserRepository, context: Context):ViewModel() {

    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)


    // Initialize SharedPreferences
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    }

    fun login(email: String, password: String, callback: (Boolean, String) -> Unit) {
        repo.login(email, password) { success, message ->
            if (success) {
                // Save login state
                sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
            }
            callback(success, message)
        }
    }

    fun signup(email: String, password: String, callback: (Boolean, String, String) -> Unit) {
        repo.signup(email, password, callback)
    }
    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
    fun addUserToDatabase(userId: String, userModel: UserModel, callback: (Boolean, String) -> Unit) {
        databaseReference.child(userId).setValue(userModel)
            .addOnSuccessListener {
                callback(true, "Data saved successfully")
            }
            .addOnFailureListener { exception ->
                callback(false, exception.message ?: "Unknown error")
            }
    }


    fun forgetPassword(email: String, callback: (Boolean, String) -> Unit) {
        repo.forgetPassword(email, callback)
    }

    fun getCurrentUser (): FirebaseUser ? {
        return repo.getCurrentUser ()
    }
    var _userData = MutableLiveData<UserModel?>()
    var userData = MutableLiveData<UserModel?>()
        get() = _userData


    fun getUserFromDatabase(userId:String){
        repo.getUserFromDatabase(userId){
                user,success,message->
            Log.d("UserViewModel", "Database Fetch Success: $success, User: $user")
            if(success){
                _userData.postValue(user)
            }else{
                _userData.value = null
            }
        }
    }

    fun logout(callback: (Boolean, String) -> Unit) {
        repo.logout { success, message ->
            if (success) {
                sharedPreferences.edit().putBoolean("isLoggedIn", false).apply() // Clear login state
            }
            callback(success, message)
        }
    }

    fun editProfile(userId: String,data:MutableMap<String,Any>,
                    callback: (Boolean, String) -> Unit){
        repo.editProfile(userId, data){ success, message ->
            if (success) {
                // Fetch the updated user data and update _userData
                getUserFromDatabase(userId)
            }
            callback(success, message)
        }
    }
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }
}