package com.example.individualproject.repository

import com.example.individualproject.model.ExpenseCalculatorModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class ExpenseCalculatorTest {

    @Mock private lateinit var mockAuth: FirebaseAuth
    @Mock private lateinit var mockDatabase: FirebaseDatabase
    @Mock private lateinit var mockUsersRef: DatabaseReference
    @Mock private lateinit var mockExpenseRef: DatabaseReference
    @Mock private lateinit var mockTask: Task<Void>
    @Mock private lateinit var mockDataSnapshot: DataSnapshot

    @Captor private lateinit var taskCaptor: ArgumentCaptor<OnCompleteListener<Void>>
    @Captor private lateinit var valueEventCaptor: ArgumentCaptor<ValueEventListener>

    private lateinit var repository: ExpenseCalculatorRepositoryImpl
    private val userId = "test_user_id"
    private val expenseId = "test_expense_id"
    private lateinit var mockedFirebaseAuth: MockedStatic<FirebaseAuth>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        mockedFirebaseAuth = mockStatic(FirebaseAuth::class.java)
        mockedFirebaseAuth.`when`<FirebaseAuth>(FirebaseAuth::getInstance).thenReturn(mockAuth)

        `when`(mockDatabase.reference).thenReturn(mockUsersRef)
        `when`(mockUsersRef.child(anyString())).thenReturn(mockUsersRef)
        `when`(mockUsersRef.child("expense_calculator")).thenReturn(mockExpenseRef)
        `when`(mockExpenseRef.push()).thenReturn(mockExpenseRef)
        `when`(mockExpenseRef.key).thenReturn(expenseId)

        repository = ExpenseCalculatorRepositoryImpl().apply {
            val databaseField = this::class.java.getDeclaredField("database")
            databaseField.isAccessible = true
            databaseField.set(this, mockDatabase)
        }
    }

    @Test
    fun addExpenseCalculator_Success() {
        val expense = ExpenseCalculatorModel(
            id = expenseId,
            calExpenseAmount = 150.0,
            calExpenseDescription = "Office Supplies",
            calExpenseDate = "2023-10-01"
        )
        var resultMessage = ""
        val callback: (Boolean, String) -> Unit = { _, message ->
            resultMessage = message
        }

        `when`(mockAuth.currentUser?.uid).thenReturn(userId)
        `when`(mockExpenseRef.setValue(any())).thenReturn(mockTask)
        `when`(mockTask.isSuccessful).thenReturn(true)

        repository.addExpenseCalculator(expense, callback)

        verify(mockTask).addOnCompleteListener(taskCaptor.capture())
        taskCaptor.value.onComplete(mockTask)
        assertEquals("Expense added successfully", resultMessage)
    }

    @Test
    fun updateExpenseCalculator_Success() {
        val updates = mutableMapOf<String, Any>(
            "calExpenseAmount" to 200.0,
            "calExpenseDescription" to "Updated Office Supplies"
        )
        var resultMessage = ""
        val callback: (Boolean, String) -> Unit = { _, message ->
            resultMessage = message
        }

        `when`(mockAuth.currentUser?.uid).thenReturn(userId)
        `when`(mockExpenseRef.updateChildren(any())).thenReturn(mockTask)
        `when`(mockTask.isSuccessful).thenReturn(true)

        repository.updateExpenseCalculator(expenseId, updates, callback)

        verify(mockTask).addOnCompleteListener(taskCaptor.capture())
        taskCaptor.value.onComplete(mockTask)
        assertEquals("Expense updated successfully", resultMessage)
    }

    @Test
    fun deleteExpenseCalculator_Success() {
        var resultMessage = ""
        val callback: (Boolean, String) -> Unit = { _, message ->
            resultMessage = message
        }

        `when`(mockAuth.currentUser?.uid).thenReturn(userId)
        `when`(mockExpenseRef.removeValue()).thenReturn(mockTask)
        `when`(mockTask.isSuccessful).thenReturn(true)

        repository.deleteExpenseCalculator(expenseId, callback)

        verify(mockTask).addOnCompleteListener(taskCaptor.capture())
        taskCaptor.value.onComplete(mockTask)
        assertEquals("Expense deleted successfully", resultMessage)
    }

    @Test
    fun getExpenseCalculatorById_Success() {
        val testExpense = ExpenseCalculatorModel(
            id = expenseId,
            calExpenseAmount = 300.0,
            calExpenseDescription = "Team Lunch",
            calExpenseDate = "2023-10-05"
        )
        var retrievedExpense: ExpenseCalculatorModel? = null
        val callback: (ExpenseCalculatorModel?, Boolean, String) -> Unit = { expense, _, _ ->
            retrievedExpense = expense
        }

        `when`(mockAuth.currentUser?.uid).thenReturn(userId)
        `when`(mockDataSnapshot.getValue(ExpenseCalculatorModel::class.java)).thenReturn(testExpense)

        repository.getExpenseCalculatorById(expenseId, callback)

        verify(mockExpenseRef).addListenerForSingleValueEvent(valueEventCaptor.capture())
        valueEventCaptor.value.onDataChange(mockDataSnapshot)

        assertNotNull(retrievedExpense)
        assertEquals(testExpense.id, retrievedExpense!!.id)
        assertEquals("Team Lunch", retrievedExpense!!.calExpenseDescription)
        assertEquals(300.0, retrievedExpense!!.calExpenseAmount, 0.01)
    }

    @Test
    fun getExpenseAllCalculator_Success() {
        val testExpenses = listOf(
            ExpenseCalculatorModel(
                id = "1",
                calExpenseAmount = 150.0,
                calExpenseDescription = "Printer Ink",
                calExpenseDate = "2023-10-01"
            ),
            ExpenseCalculatorModel(
                id = "2",
                calExpenseAmount = 75.0,
                calExpenseDescription = "Stationery",
                calExpenseDate = "2023-10-02"
            )
        )
        var retrievedExpenses: List<ExpenseCalculatorModel>? = null
        val callback: (List<ExpenseCalculatorModel>?, Boolean, String) -> Unit = { expenses, _, _ ->
            retrievedExpenses = expenses
        }

        `when`(mockAuth.currentUser?.uid).thenReturn(userId)
        `when`(mockDataSnapshot.children).thenReturn(testExpenses.map {
            val snap = mock(DataSnapshot::class.java)
            `when`(snap.getValue(ExpenseCalculatorModel::class.java)).thenReturn(it)
            snap
        }.toMutableList())

        repository.getExpenseAllCalculator(callback)

        verify(mockUsersRef).addListenerForSingleValueEvent(valueEventCaptor.capture())
        valueEventCaptor.value.onDataChange(mockDataSnapshot)

        assertNotNull(retrievedExpenses)
        assertEquals(2, retrievedExpenses!!.size)
        assertEquals("Printer Ink", retrievedExpenses!![0].calExpenseDescription)
        assertEquals(150.0, retrievedExpenses!![0].calExpenseAmount, 0.01)
        assertEquals(75.0, retrievedExpenses!![1].calExpenseAmount, 0.01)
    }

    @Test
    fun allOperations_UserNotAuthenticated() {
        `when`(mockAuth.currentUser?.uid).thenReturn(null)
        var errorMessage = ""

        repository.addExpenseCalculator(ExpenseCalculatorModel()) { _, message ->
            errorMessage = message
        }
        assertEquals("User  not authenticated", errorMessage)

        repository.updateExpenseCalculator("123", mutableMapOf()) { _, message ->
            errorMessage = message
        }
        assertEquals("User  not authenticated", errorMessage)

        repository.deleteExpenseCalculator("123") { _, message ->
            errorMessage = message
        }
        assertEquals("User  not authenticated", errorMessage)

        repository.getExpenseCalculatorById("123") { _, _, message ->
            errorMessage = message
        }
        assertEquals("User  not authenticated", errorMessage)

        repository.getExpenseAllCalculator { _, _, message ->
            errorMessage = message
        }
        assertEquals("User  not authenticated", errorMessage)
    }
}