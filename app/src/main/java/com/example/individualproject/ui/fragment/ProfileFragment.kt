package com.example.individualproject.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.individualproject.databinding.FragmentProfileBinding
import com.example.individualproject.repository.UserRepositoryImpl
import com.example.individualproject.ui.activity.LoginActivity
import com.example.individualproject.utils.ImageUtils
import com.example.individualproject.utils.LoadingUtils
import com.example.individualproject.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso


class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var loadingUtils: LoadingUtils
    private lateinit var imageUtils: ImageUtils
    private var imageUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageUtils = ImageUtils(this)
        imageUtils.registerActivity { uri ->
            if (uri != null) {
                imageUri = uri
                binding.profileImage.setImageURI(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repo = UserRepositoryImpl(FirebaseAuth.getInstance())
        userViewModel = UserViewModel(repo,requireContext())


        val currentUser = userViewModel.getCurrentUser()

        currentUser?.let {
            userViewModel.getUserFromDatabase(it.uid)
        }

        userViewModel.userData.observe(viewLifecycleOwner) { user ->
            Log.d("ProfileFragment", "Fetched user data: $user")
            if (user != null) {
                Log.d("ProfileFragment", "User Email: ${user.email}, User Name: ${user.firstName}, Image URL: ${user.imageUrl}")
                binding.profileEmail.text = user.email ?: "No Email"
                binding.profileName.text = user.firstName ?: "No Name"
                Log.d("ProfileFragment", "User Data: ${user?.email}, ${user?.firstName}")

                if (user.imageUrl?.isNotEmpty() == true) {
                    Picasso.get().load(user.imageUrl).into(binding.profileImage)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to fetch user details",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.profileImage.setOnClickListener {
            imageUtils.launchGallery(requireContext())
        }

        binding.btnUploadImage.setOnClickListener {
            uploadProfileImage()
        }
        binding.btnLogout.setOnClickListener {
            userViewModel.logout { success, message ->
                if (success) {
                    Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show()
                    // Navigate to login screen and clear back stack
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "Logout failed: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uploadProfileImage() {
        val userId = userViewModel.getCurrentUser()?.uid
        if (userId != null && imageUri != null) {
            loadingUtils = LoadingUtils(requireContext())
            loadingUtils.show()

            userViewModel.repo.uploadImage(requireContext(), imageUri!!) { imageUrl ->
                loadingUtils.dismiss()
                if (imageUrl != null) {
                    val currentUser = userViewModel.userData.value
                    val updateData = mutableMapOf<String, Any>(
                        "imageUrl" to imageUrl,
                        "email" to (currentUser?.email ?: ""),
                        "firstName" to (currentUser?.firstName ?: "")
                    )
                    userViewModel.editProfile(userId, updateData) { success, message ->
                        if (success) {
                            Toast.makeText(requireContext(), "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Failed to update profile!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Image upload failed!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "Select an image first!", Toast.LENGTH_SHORT).show()
        }
    }

}