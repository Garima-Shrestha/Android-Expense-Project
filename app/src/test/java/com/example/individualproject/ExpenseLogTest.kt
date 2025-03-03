package com.example.individualproject

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.example.individualproject.model.ExpenseLogModel
import com.example.individualproject.repository.ExpenseLogRepositoryImpl
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.lang.reflect.Field


class ExpenseLogTest {
    @Mock
    private lateinit var mockAuth: FirebaseAuth

    @Mock
    private lateinit var mockUser: FirebaseUser

    @Mock
    private lateinit var mockDatabase: FirebaseDatabase

    @Mock
    private lateinit var mockUsersRef: DatabaseReference

    @Mock
    private lateinit var mockExpensesRef: DatabaseReference

    @Mock
    private lateinit var mockTask: Task<Void>

    @Mock
    private lateinit var mockDataSnapshot: DataSnapshot

    @Captor
    private lateinit var valueEventListenerCaptor: ArgumentCaptor<ValueEventListener>

    @Captor
    private lateinit var taskCaptor: ArgumentCaptor<OnCompleteListener<Void>>

    private lateinit var repository: ExpenseLogRepositoryImpl

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        `when`(mockDatabase.reference).thenReturn(mockUsersRef)
        `when`(mockUsersRef.child("users")).thenReturn(mockUsersRef)

        repository = ExpenseLogRepositoryImpl().apply {
            injectMock("database", mockDatabase)
            injectMock("usersRef", mockUsersRef)
            injectMock("auth", mockAuth)
        }
    }

    private fun Any.injectMock(fieldName: String, mock: Any) {
        val field: Field = this.javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(this, mock)
    }

    @Test
    fun `addExpenseLog should return success when user is authenticated`() {
        // Given
        val userId = "user123"
        val expense = ExpenseLogModel().apply { LogId = "expense123" }

        `when`(mockAuth.currentUser).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn(userId)
        `when`(mockUsersRef.child(userId)).thenReturn(mockUsersRef)
        `when`(mockUsersRef.child("expenses")).thenReturn(mockExpensesRef)
        `when`(mockExpensesRef.push()).thenReturn(mockExpensesRef)
        `when`(mockExpensesRef.key).thenReturn("expense123")
        `when`(mockExpensesRef.setValue(expense)).thenReturn(mockTask)

        // When
        var success = false
        var message = ""
        repository.addExpenseLog(expense) { s, m ->
            success = s
            message = m
        }

        // Then
        verify(mockTask).addOnCompleteListener(taskCaptor.capture())
        taskCaptor.value.onComplete(mockTask)
        assertTrue(success)
        assertEquals("Expense Log Added successfully", message)
    }

    @Test
    fun `addExpenseLog should return failure when user is not authenticated`() {
        // Given
        `when`(mockAuth.currentUser).thenReturn(null)

        // When
        var success = true
        var message = ""
        repository.addExpenseLog(ExpenseLogModel()) { s, m ->
            success = s
            message = m
        }

        // Then
        assertFalse(success)
        assertEquals("User  not authenticated", message)
    }

    @Test
    fun `updateExpenseLog should return success on valid update`() {
        // Given
        val userId = "user123"
        val expenseId = "expense123"
        val updates = mutableMapOf<String, Any>("amount" to 100)

        `when`(mockAuth.currentUser).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn(userId)
        `when`(mockUsersRef.child(userId)).thenReturn(mockUsersRef)
        `when`(mockUsersRef.child("expenses")).thenReturn(mockExpensesRef)
        `when`(mockExpensesRef.child(expenseId)).thenReturn(mockExpensesRef)
        `when`(mockExpensesRef.updateChildren(updates)).thenReturn(mockTask)

        // When
        var success = false
        var message = ""
        repository.updateExpenseLog(expenseId, updates) { s, m ->
            success = s
            message = m
        }

        // Then
        verify(mockTask).addOnCompleteListener(taskCaptor.capture())
        taskCaptor.value.onComplete(mockTask)
        assertTrue(success)
        assertEquals("Expense Log Updated successfully", message)
    }

    @Test
    fun `deleteExpenseLog should return success on valid deletion`() {
        // Given
        val userId = "user123"
        val expenseId = "expense123"

        `when`(mockAuth.currentUser).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn(userId)
        `when`(mockUsersRef.child(userId)).thenReturn(mockUsersRef)
        `when`(mockUsersRef.child("expenses")).thenReturn(mockExpensesRef)
        `when`(mockExpensesRef.child(expenseId)).thenReturn(mockExpensesRef)
        `when`(mockExpensesRef.removeValue()).thenReturn(mockTask)

        // When
        var success = false
        var message = ""
        repository.deleteExpenseLog(expenseId) { s, m ->
            success = s
            message = m
        }

        // Then
        verify(mockTask).addOnCompleteListener(taskCaptor.capture())
        taskCaptor.value.onComplete(mockTask)
        assertTrue(success)
        assertEquals("Expense Log Deleted successfully", message)
    }

    @Test
    fun `getExpenseLogById should return expense when exists`() {
        // Given
        val userId = "user123"
        val expenseId = "expense123"
        val mockExpense = ExpenseLogModel().apply { LogId = expenseId }

        `when`(mockAuth.currentUser).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn(userId)
        `when`(mockUsersRef.child(userId)).thenReturn(mockUsersRef)
        `when`(mockUsersRef.child("expenses")).thenReturn(mockExpensesRef)
        `when`(mockExpensesRef.child(expenseId)).thenReturn(mockExpensesRef)
        `when`(mockDataSnapshot.getValue(ExpenseLogModel::class.java)).thenReturn(mockExpense)

        // When
        var result: ExpenseLogModel? = null
        repository.getExpenseLogById(expenseId) { expense, success, message ->
            if (success) result = expense
        }

        // Then
        verify(mockExpensesRef).addListenerForSingleValueEvent(valueEventListenerCaptor.capture())
        valueEventListenerCaptor.value.onDataChange(mockDataSnapshot)
        assertNotNull(result)
        assertEquals(expenseId, result?.LogId)
    }

    @Test
    fun `getExpenseAllLog should return list of expenses`() {
        // Given
        val userId = "user123"
        val mockExpense = ExpenseLogModel().apply { LogId = "expense123" }

        `when`(mockAuth.currentUser).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn(userId)
        `when`(mockUsersRef.child(userId)).thenReturn(mockUsersRef)
        `when`(mockUsersRef.child("expenses")).thenReturn(mockExpensesRef)
        `when`(mockDataSnapshot.children).thenReturn(listOf(mockDataSnapshot))
        `when`(mockDataSnapshot.getValue(ExpenseLogModel::class.java)).thenReturn(mockExpense)

        // When
        var result: List<ExpenseLogModel>? = null
        repository.getExpenseAllLog { expenses, success, message ->
            if (success) result = expenses
        }

        // Then
        verify(mockExpensesRef).addListenerForSingleValueEvent(valueEventListenerCaptor.capture())
        valueEventListenerCaptor.value.onDataChange(mockDataSnapshot)
        assertNotNull(result)
        assertEquals(1, result?.size)
        assertEquals("expense123", result?.first()?.LogId)
    }

    @Test
    fun `getExpenseLogById should handle database error`() {
        // Given
        val userId = "user123"
        val expenseId = "expense123"
        val errorMessage = "Database error"

        `when`(mockAuth.currentUser).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn(userId)
        `when`(mockUsersRef.child(userId)).thenReturn(mockUsersRef)
        `when`(mockUsersRef.child("expenses")).thenReturn(mockExpensesRef)
        `when`(mockExpensesRef.child(expenseId)).thenReturn(mockExpensesRef)

        // When
        var error: String? = null
        repository.getExpenseLogById(expenseId) { _, success, message ->
            if (!success) error = message
        }

        // Then
        verify(mockExpensesRef).addListenerForSingleValueEvent(valueEventListenerCaptor.capture())
        valueEventListenerCaptor.value.onCancelled(DatabaseError.fromException(Exception(errorMessage)))
        assertEquals(errorMessage, error)
    }
}