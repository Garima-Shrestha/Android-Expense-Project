package com.example.individualproject.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.individualproject.model.ExpenseLogModel
import com.example.individualproject.repository.ExpenseLogRepository

class ExpenseLogViewModel(private val repository: ExpenseLogRepository) {

    private val _expense = MutableLiveData<ExpenseLogModel?>()
    val expense: LiveData<ExpenseLogModel?> get() = _expense

    private val _allExpense = MutableLiveData<List<ExpenseLogModel>?>()
    val allExpense: LiveData<List<ExpenseLogModel>?> get() = _allExpense

    private val _loadingState = MutableLiveData<Boolean>()
    val loadingState: LiveData<Boolean> get() = _loadingState


    fun addExpenseLog(expenseLogModel: ExpenseLogModel, callback: (Boolean, String) -> Unit) {
        repository.addExpenseLog(expenseLogModel) { success, message ->
            // After successfully adding, refresh the list of expenses
            if (success) {
                getExpenseAllLog() // Fetch all logs to update the list
            }
            callback(success, message)
        }
    }


    fun updateExpenseLog(logId: String, data: MutableMap<String, Any>, callback: (Boolean, String) -> Unit) {
        repository.updateExpenseLog(logId, data){ success, message ->
            if (success) {
                getExpenseLogById(logId) // Fetch updated data after updating
            }
            callback(success, message)
        }
    }

    fun deleteLog(logId: String, callback: (Boolean, String) -> Unit) {
        repository.deleteExpenseLog(logId, callback)
    }

    fun getExpenseLogById(logId: String) {
        _loadingState.value = true
        repository.getExpenseLogById(logId) { expense, success, _ ->
            if (success) {
                _expense.value = null
                _expense.value = expense
            }
            _loadingState.value = false
        }
    }

    fun getExpenseAllLog() {
        _loadingState.value = true
        repository.getExpenseAllLog() { expense, success, _ ->
            if (success) {
                _allExpense.value = expense
            } else {
                _allExpense.value = emptyList()
            }
            _loadingState.value = false
        }
    }
}

