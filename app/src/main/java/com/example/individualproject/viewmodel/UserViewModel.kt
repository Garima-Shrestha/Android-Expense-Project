package com.example.individualproject.viewmodel

import android.content.Context
import android.content.SharedPreferences
import com.example.individualproject.model.UserModel
import com.example.individualproject.repository.UserRepository
import com.google.firebase.auth.FirebaseUser

class UserViewModel( val repo: UserRepository) {

    private lateinit var sharedPreferences: SharedPreferences

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

    fun addUserToDatabase(userId: String, userModel: UserModel, callback: (Boolean, String) -> Unit) {
        repo.addUserToDatabase(userId, userModel, callback)
    }

    fun forgetPassword(email: String, callback: (Boolean, String) -> Unit) {
        repo.forgetPassword(email, callback)
    }

    fun getCurrentUser (): FirebaseUser ? {
        return repo.getCurrentUser ()
    }

    // Check if the user is logged in
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

    // Optional: Method to log out the user
    fun logout() {
        sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
        // Additional logout logic if needed (e.g., Firebase sign out)
    }
}