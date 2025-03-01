
package com.example.individualproject.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.individualproject.R
import com.example.individualproject.databinding.ActivityUpdateLogBinding
import com.example.individualproject.repository.ExpenseLogRepositoryImpl
import com.example.individualproject.viewmodel.ExpenseLogViewModel

class UpdateLogActivity : AppCompatActivity() {

    lateinit var binding: ActivityUpdateLogBinding
    lateinit var viewModel: ExpenseLogViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUpdateLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var repo = ExpenseLogRepositoryImpl()
        viewModel = ExpenseLogViewModel(repo)

        var logId: String? = intent.getStringExtra("logId")

        viewModel.getExpenseLogById(logId.toString())

        viewModel.expense.observe(this) {
            binding.etExpenseAmount.setText(it?.ExpenseAmount.toString())
            binding.etExpenseCategory.setText(it?.ExpenseCategory.toString())
            binding.etExpenseDate.setText(it?.ExpenseDate.toString())
            binding.etExpensePurpose.setText(it?.ExpensePurpose.toString())
        }

        binding.btnUpdate.setOnClickListener {
            val amount = binding.etExpenseAmount.text.toString().toIntOrNull() ?: 0
            val category = binding.etExpenseCategory.text.toString()
            val date = binding.etExpenseDate.text.toString().trim()
            val purpose = binding.etExpensePurpose.text.toString().trim()

            var updatedMap = mutableMapOf<String, Any>()
            updatedMap["expenseAmount"] = amount
            updatedMap["expenseCategory"] = category
            updatedMap["expenseDate"] = date
            updatedMap["expensePurpose"] = purpose

            viewModel.updateExpenseLog(
                logId.toString(),
                updatedMap
            ) { success, message ->
                Toast.makeText(this@UpdateLogActivity, message, Toast.LENGTH_LONG).show()

                if(success) {
                    viewModel.getExpenseLogById(logId.toString())
                    finish()
                }

            }
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}