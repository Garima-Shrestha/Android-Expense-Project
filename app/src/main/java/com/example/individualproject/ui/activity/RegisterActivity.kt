package com.example.individualproject.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.individualproject.R
import com.example.individualproject.databinding.ActivityRegisterBinding
import com.example.individualproject.model.UserModel
import com.example.individualproject.repository.UserRepositoryImpl
import com.example.individualproject.utils.LoadingUtils
import com.example.individualproject.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    lateinit var binding: ActivityRegisterBinding

    lateinit var userViewModel: UserViewModel

    lateinit var loadingUtils: LoadingUtils
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingUtils=LoadingUtils(this)

        var repo= UserRepositoryImpl(FirebaseAuth.getInstance())
        userViewModel= UserViewModel(repo, this)
        binding.signUp.setOnClickListener {
            loadingUtils.show()
            var email = binding.registerEmail.text.toString()
            var password = binding.registerPassword.text.toString()
            var firstName = binding.registerFname.text.toString()
            var contact = binding.registerContact.text.toString()

            userViewModel.signup(email, password) { success, message, userId ->
                if (success) {
                    var userModel = UserModel(
                        userId.toString(),
                        firstName,
                        contact,
                        email
                    )

                    userViewModel.addUserToDatabase(userId, userModel) { success, message ->
                        if (success) {
                            loadingUtils.dismiss()
                            Toast.makeText(
                                this@RegisterActivity,
                                message,
                                Toast.LENGTH_LONG
                            ).show()
                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                } else {
                    loadingUtils.dismiss()
                    Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_LONG).show()
                }
            }
        }
        binding.btnLoginNavigate.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        // System Bar Insets Handling
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
