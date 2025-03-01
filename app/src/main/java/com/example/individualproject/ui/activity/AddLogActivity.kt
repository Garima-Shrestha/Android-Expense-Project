package com.example.individualproject.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.individualproject.R
import com.example.individualproject.databinding.ActivityAddLogBinding
import com.example.individualproject.model.ExpenseLogModel
import com.example.individualproject.repository.ExpenseLogRepositoryImpl
import com.example.individualproject.viewmodel.ExpenseLogViewModel

class AddLogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddLogBinding
    private lateinit var viewModel: ExpenseLogViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize View Binding
        binding = ActivityAddLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the repository and ViewModel
        var repo = ExpenseLogRepositoryImpl()
        viewModel = ExpenseLogViewModel(repo)

        // Set up the Floating Action Button
        binding.floatingActionButton2.setOnClickListener {
            addExpenseLog()
        }


        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

        private fun addExpenseLog() {
            val ExpenseAmount = binding.ExpenseAmountEditText.text.toString().toIntOrNull() ?: 0
            val ExpenseCategory = binding.ExpenseCategoryEditText.text.toString()
            val ExpenseDate = binding.ExpenseDateEditText.text.toString()
            val ExpensePurpose = binding.ExpensePurposeEditText.text.toString()

            val model = ExpenseLogModel(
                "",
                ExpenseAmount = ExpenseAmount,
                ExpenseCategory = ExpenseCategory,
                ExpenseDate = ExpenseDate,
                ExpensePurpose = ExpensePurpose
            )

            viewModel.addExpenseLog(model) { success, message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                if (success) {
                    finish() // Close the activity after adding the log
                }
            }
        }
    }









