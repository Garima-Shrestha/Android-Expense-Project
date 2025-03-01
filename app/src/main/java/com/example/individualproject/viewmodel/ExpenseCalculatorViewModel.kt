package com.example.individualproject.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.individualproject.model.ExpenseCalculatorModel
import com.example.individualproject.repository.ExpenseCalculatorRepository

class ExpenseCalculatorViewModel (private val repository: ExpenseCalculatorRepository) : ViewModel() {

    private val _expenses = MutableLiveData<List<ExpenseCalculatorModel>>()
    val expenses: LiveData<List<ExpenseCalculatorModel>> = _expenses

    private val _isExpenseAdded = MutableLiveData<Boolean>()
    val isExpenseAdded: LiveData<Boolean> = _isExpenseAdded


    fun addExpenseCalculator(expenseCalculatorModel: ExpenseCalculatorModel) {
        repository.addExpenseCalculator(expenseCalculatorModel) { isSuccess, message ->
            _isExpenseAdded.value = isSuccess
        }
    }

    fun getExpenseAllCalculator() {
        repository.getExpenseAllCalculator { expenseList, isSuccess, message ->
            if (isSuccess) {
                _expenses.value = expenseList
            } else {
                // Handle error case
                _expenses.value = emptyList()  // Optional: Handle empty list case
            }
        }
    }

}