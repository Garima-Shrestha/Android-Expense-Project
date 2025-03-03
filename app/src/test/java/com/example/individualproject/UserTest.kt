package com.example.individualproject

import android.net.Uri
import com.cloudinary.Cloudinary
import com.cloudinary.Uploader
import com.example.individualproject.model.UserModel
import com.example.individualproject.repository.UserRepositoryImpl
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.ArgumentMatchers.any
import android.content.Context
import android.database.MatrixCursor
import android.provider.OpenableColumns
import java.io.ByteArrayInputStream
import com.google.firebase.auth.AuthResult


class UserTest {
    @Mock
    private lateinit var mockAuth: FirebaseAuth

    @Mock
    private lateinit var mockDatabase: FirebaseDatabase

    @Mock
    private lateinit var mockDbRef: DatabaseReference

    @Mock
    private lateinit var mockUser: FirebaseUser

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockCloudinary: Cloudinary

    @Mock
    private lateinit var mockUploader: Uploader

    @Captor
    private lateinit var valueEventListenerCaptor: ArgumentCaptor<ValueEventListener>

    private lateinit var repository: UserRepositoryImpl

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        `when`(mockDatabase.reference).thenReturn(mockDbRef)
        `when`(mockDbRef.child(anyString())).thenReturn(mockDbRef)
        repository = UserRepositoryImpl(mockAuth)

        // Use reflection to inject mock Cloudinary
        val field = UserRepositoryImpl::class.java.getDeclaredField("cloudinary")
        field.isAccessible = true
        field.set(null, mockCloudinary)
    }

    @Test
    fun `login should invoke success callback when authentication succeeds`() {
        val task = mock(Task::class.java) as Task<AuthResult>
        `when`(task.isSuccessful).thenReturn(true)
        `when`(mockAuth.signInWithEmailAndPassword("test@example.com", "password")).thenReturn(task)

        repository.login("test@example.com", "password") { success, message ->
            assert(success)
        }
    }

    @Test
    fun `login should invoke failure callback when authentication fails`() {
        val task = mock(Task::class.java) as Task<AuthResult>
        `when`(task.isSuccessful).thenReturn(false)
        `when`(task.exception).thenReturn(RuntimeException("Authentication failed"))
        `when`(mockAuth.signInWithEmailAndPassword("test@example.com", "wrong")).thenReturn(task)

        repository.login("test@example.com", "wrong") { success, message ->
            assert(!success)
        }
    }

    @Test
    fun `signup should return success on valid credentials`() {
        val task = mock(Task::class.java) as Task<AuthResult>
        `when`(task.isSuccessful).thenReturn(true)
        `when`(mockAuth.createUserWithEmailAndPassword("new@example.com", "password")).thenReturn(task)
        `when`(mockAuth.currentUser).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn("123")

        repository.signup("new@example.com", "password") { success, message, userId ->
            assert(success)
            assert(userId == "123")
        }
    }

    @Test
    fun `addUserToDatabase should invoke success callback`() {
        val userModel = UserModel()
        val task = mock(Task::class.java) as Task<Void>
        `when`(mockDbRef.setValue(userModel)).thenReturn(task)
        `when`(task.isSuccessful).thenReturn(true)

        repository.addUserToDatabase("123", userModel) { success, message ->
            assert(success)
        }
    }

    @Test
    fun `getCurrentUser should return current authenticated user`() {
        `when`(mockAuth.currentUser).thenReturn(mockUser)
        assert(repository.getCurrentUser() == mockUser)
    }

    @Test
    fun `logout should invoke success callback`() {
        repository.logout { success, message ->
            assert(success)
        }
        verify(mockAuth).signOut()
    }

    @Test
    fun `getUserFromDatabase should return user data when exists`() {
        val mockSnapshot = mock(DataSnapshot::class.java)
        `when`(mockSnapshot.exists()).thenReturn(true)
        `when`(mockSnapshot.getValue(UserModel::class.java)).thenReturn(UserModel())

        repository.getUserFromDatabase("123") { user, success, message ->
            assert(success)
            assert(user != null)
        }

        verify(mockDbRef).addListenerForSingleValueEvent(valueEventListenerCaptor.capture())
        valueEventListenerCaptor.value.onDataChange(mockSnapshot)
    }

    @Test
    fun `editProfile should update user data and fetch updated info`() {
        val data = mutableMapOf<String, Any>("name" to "New Name")
        val task = mock(Task::class.java) as Task<Void>
        `when`(mockDbRef.updateChildren(data)).thenReturn(task)
        `when`(task.isSuccessful).thenReturn(true)

        repository.editProfile("123", data) { success, message ->
            assert(success)
        }
    }

    @Test
    fun `uploadImage should return image URL on success`() {
        val mockUri = mock(Uri::class.java)
        `when`(mockContext.contentResolver.openInputStream(mockUri))
            .thenReturn(ByteArrayInputStream(ByteArray(0)))
        `when`(mockCloudinary.uploader()).thenReturn(mockUploader)
        `when`(mockUploader.upload(any(), any()))
            .thenReturn(mapOf("url" to "https://res.cloudinary.com/image.jpg"))

        repository.uploadImage(mockContext, mockUri) { url ->
            assert(url == "https://res.cloudinary.com/image.jpg")
        }
    }

    @Test
    fun `getFileNameFromUri should return correct filename`() {
        val mockUri = mock(Uri::class.java)
        val cursor = MatrixCursor(arrayOf(OpenableColumns.DISPLAY_NAME))
        cursor.addRow(arrayOf("test.jpg"))
        `when`(mockContext.contentResolver.query(mockUri, null, null, null, null))
            .thenReturn(cursor)

        val fileName = repository.getFileNameFromUri(mockContext, mockUri)
        assert(fileName == "test.jpg")
    }
}