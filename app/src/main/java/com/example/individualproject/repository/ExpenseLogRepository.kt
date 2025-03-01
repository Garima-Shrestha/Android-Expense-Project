package com.example.individualproject.repository

import com.example.individualproject.model.ExpenseLogModel

interface ExpenseLogRepository {
    fun addExpenseLog(ExpenseLogModel: ExpenseLogModel,
                   callback:(Boolean, String) -> Unit)

    fun updateExpenseLog(ExpenseLogId: String,
                      data: MutableMap<String, Any>,
                      callback: (Boolean, String) -> Unit)

    fun deleteExpenseLog(ExpenseLogId: String,
                      callback: (Boolean, String) -> Unit)

    fun getExpenseLogById(ExpenseLogId: String,
                       callback: (ExpenseLogModel?, Boolean, String) -> Unit)

    fun getExpenseAllLog(callback: (List<ExpenseLogModel>?,
                                 Boolean, String) -> Unit)

}


