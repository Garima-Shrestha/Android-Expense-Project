package com.example.individualproject.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log
import com.cloudinary.Cloudinary
import com.example.individualproject.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.InputStream
import java.util.concurrent.Executors
import com.cloudinary.utils.ObjectUtils


class UserRepositoryImpl(var auth: FirebaseAuth):UserRepository{
    var database : FirebaseDatabase = FirebaseDatabase.getInstance()
    var ref : DatabaseReference = database.reference.child("users")
    override fun login(email: String, password: String, callback: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener{
            if(it.isSuccessful){
                callback(true,"Login Successful")
            }else{
                callback(false,it.exception?.message.toString())
            }
        }
    }

    override fun signup(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{
            if(it.isSuccessful){
                callback(true,"Registration success", auth.currentUser?.uid.toString())
            }else{
                callback(false,it.exception?.message.toString(),"")
            }
        }
    }

    override fun addUserToDatabase(
        userId: String,
        userModel: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).setValue(UserModel).addOnCompleteListener{
            if(it.isSuccessful){
                callback(true,"Registration success")
            }else{
                callback(false,it.exception?.message.toString())
            }
        }
    }

    override fun forgetPassword(email: String, callback: (Boolean, String) -> Unit) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener(){
            if (it.isSuccessful) {
                callback(true, "Reset email sent to $email")
            } else {
                callback(false, it.exception?.message.toString())
            }
        }
    }

    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    override fun logout(callback: (Boolean, String) -> Unit) {
        try {
            auth.signOut()
            callback(true, "Signout successfull")
        } catch (e: Exception) {
            callback(false, e.message.toString())
        }
    }

    override fun getUserFromDatabase(
        userId: String,
        callback: (UserModel?, Boolean, String) -> Unit
    ) {
        ref.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.getValue(UserModel::class.java)?.let { model ->
                        callback(model, true, "Details fetched successfully")
                    } ?: callback(null, false, "User data is empty or invalid")
                } else {
                    callback(null, false, "User not found")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null, false, error.message)
            }
        })
    }

    override fun editProfile(
        userId: String,
        data: MutableMap<String, Any>,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).updateChildren(data)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Fetch the updated user data from the database
                    getUserFromDatabase(userId) { user, success, message ->
                        if (success && user != null) {
                            callback(true, "Profile edited successfully")
                        } else {
                            callback(false, "Profile updated but failed to fetch updated data")
                        }
                    }
                } else {
                    callback(false, "Unable to edit profile")
                }
            }
    }

    override fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                var fileName = getFileNameFromUri(context, imageUri)

                fileName = fileName?.substringBeforeLast(".") ?: "uploaded_image"

                Log.d("User RepositoryImpl", "Uploading image with file name: $fileName")
                val response = cloudinary.uploader().upload(
                    inputStream, ObjectUtils.asMap(
                        "public_id", fileName,
                        "resource_type", "image"
                    )
                )

                var imageUrl = response["url"] as String?

                imageUrl = imageUrl?.replace("http://", "https://")

                Log.d("User RepositoryImpl", "Image upload response: $response")
                Handler(Looper.getMainLooper()).post {
                    callback(imageUrl)
                }

            } catch (e: Exception) {
                Log.e("User RepositoryImpl", "Image upload failed", e)
                Handler(Looper.getMainLooper()).post {
                    callback(null)
                }
            }
        }
    }

    override fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var fileName: String? = null
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName
    }
}

private val cloudinary = Cloudinary(
    mapOf(
        "cloud_name" to "dramerjyi",
        "api_key" to "628618411914333",
        "api_secret" to "tfLD9rtSruIqEiur-8jvjNx6DBk"
    )
)