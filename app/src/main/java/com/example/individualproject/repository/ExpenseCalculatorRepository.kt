package com.example.individualproject.repository

import com.example.individualproject.model.ExpenseCalculatorModel


interface ExpenseCalculatorRepository {
    fun addExpenseCalculator(expenseCalculatorModel: ExpenseCalculatorModel,
                      callback:(Boolean, String) -> Unit)

    fun updateExpenseCalculator(ExpenseCalculatorId: String,
                                data: MutableMap<String, Any>,
                                callback: (Boolean, String) -> Unit)

    fun deleteExpenseCalculator(ExpenseCalculatorId: String,
                                callback: (Boolean, String) -> Unit)

    fun getExpenseCalculatorById(ExpenseCalculatorId: String,
                                 callback: (ExpenseCalculatorModel?, Boolean, String) -> Unit)

    fun getExpenseAllCalculator(callback: (List<ExpenseCalculatorModel>?,
                                    Boolean, String) -> Unit)
}
