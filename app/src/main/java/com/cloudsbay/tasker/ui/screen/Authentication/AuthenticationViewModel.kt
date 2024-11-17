package com.cloudsbay.tasker.ui.screen.Authentication

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.cloudsbay.tasker.UiState
import com.cloudsbay.tasker.data.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

class AuthenticationViewModel : ViewModel() {

    private val _uiState: MutableStateFlow<UiState> =
        MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> =
        _uiState.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.UnAuthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData.asStateFlow()

    val auth = Firebase.auth
    private val db = Firebase.firestore
    private val storage = FirebaseStorage.getInstance()

    var isLoggedIn by mutableStateOf(false)

    private val _isEditingName = MutableStateFlow(false)
    val isEditingName: StateFlow<Boolean> = _isEditingName.asStateFlow()

    private val _isEditingEmail = MutableStateFlow(false)
    val isEditingEmail: StateFlow<Boolean> = _isEditingEmail.asStateFlow()

    private val _isEditingPhoneNumber = MutableStateFlow(false)
    val isEditingPhoneNumber: StateFlow<Boolean> = _isEditingPhoneNumber.asStateFlow()

    private val storageRef = FirebaseStorage.getInstance().reference

    // Uri to hold the selected image
    var selectedImageUri: Uri? by mutableStateOf(null)
        private set

    private var userSnapshotListener: ListenerRegistration? = null

    val currentUser = auth.currentUser

    init {
        checkIfUserIsLoggedIn()
    }

    fun checkIfUserIsLoggedIn() {
        if (currentUser != null) {
            isLoggedIn = true
            fetchUserData(currentUser.uid)
            addUserSnapshotListener(currentUser.uid)  // Restore snapshot listener for real-time updates
            _authState.value = AuthState.Authenticated
        } else {
            isLoggedIn = false
            _authState.value = AuthState.UnAuthenticated
        }
    }

    // Function to select image from gallery
    fun selectImageFromGallery(uri: Uri) {
        selectedImageUri = uri
    }

    sealed class AuthState {
        object UnAuthenticated : AuthState()
        object Authenticated : AuthState()
        object Loading : AuthState()
        data class Error(val message: String) : AuthState()
    }

    private fun addUserSnapshotListener(userId: String) {
        userSnapshotListener?.remove()  // Remove any existing listener before adding a new one

        userSnapshotListener = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("AuthenticationViewModel", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                    _userData.value = user
                    Log.d("AuthenticationViewModel", "User data updated via snapshot listener: $user")
                } else {
                    Log.d("AuthenticationViewModel", "No such document for snapshot listener")
                }
            }
    }

    fun loginWithEmailAndPassword(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                _authState.value = AuthState.Authenticated
                isLoggedIn = true
                val user = authResult.user
                if (user != null) {
                    fetchUserData(user.uid)
                    addUserSnapshotListener(user.uid) // Add snapshot listener after successful login
                }
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
    }

    fun registerWithData(email: String, password: String, selectedCountryCode: String, phoneNumber: String, username: String, name: String) {
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                _authState.value = AuthState.Authenticated
                val user = authResult.user

                val userData = User(
                    email = email,
                    password = password,
                    name = name,
                    username = username,
                    phoneNumber = "+${selectedCountryCode}${phoneNumber}"
                )

                if (user != null) {
                    db.collection("users").document(user.uid).set(userData)
                        .addOnSuccessListener {
                            fetchUserData(user.uid)
                            addUserSnapshotListener(user.uid) // Add snapshot listener after successful registration
                        }
                        .addOnFailureListener { e ->
                            _authState.value = AuthState.Error(e.message ?: "Registration failed")
                        }
                }
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
    }

    private fun fetchUserData(userId: String) {
        viewModelScope.launch {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val user = document.toObject(User::class.java)
                        _userData.value = user
                        Log.d("AuthenticationViewModel", "User data fetched: $user")
                    } else {
                        Log.d("AuthenticationViewModel", "No such document")
                    }
                }
                .addOnFailureListener { e ->
                    _authState.value = AuthState.Error(e.message ?: "Failed to fetch user data")
                    Log.e("AuthenticationViewModel", "Error fetching user data", e)
                }
        }
    }

    fun logout() {
        auth.signOut()
        isLoggedIn = false
        _authState.value = AuthState.UnAuthenticated
        _userData.value = null
        userSnapshotListener?.remove()  // Remove snapshot listener when the user logs out
    }


    init {
        fetchUserData()
    }

    fun toggleEditName() {
        _isEditingName.value = !_isEditingName.value
    }

    fun toggleEditEmail() {
        _isEditingEmail.value = !_isEditingEmail.value
    }

    fun toggleEditPhoneNumber() {
        _isEditingPhoneNumber.value = !_isEditingPhoneNumber.value
    }

    fun updateName(newName: String) {
        val currentUser = auth.currentUser ?: return
        val userRef = db.collection("users").document(currentUser.uid)

        userRef.update("name", newName)
            .addOnSuccessListener {
                _userData.value = _userData.value?.copy(name = newName)
                _isEditingName.value = false
            }
            .addOnFailureListener { exception ->
                // Handle error updating name
            }
    }

    // Function to update the profile picture in Firestore
    fun updateProfilePictureUrl(userId: String, profileImageUrl: String) {
        viewModelScope.launch {
            val userDocumentRef = FirebaseFirestore.getInstance().collection("users").document(userId)
            userDocumentRef.update("profilePictureUrl", profileImageUrl)
                .addOnSuccessListener {
                    // Handle success if necessary
                }
                .addOnFailureListener {
                    // Handle failure
                }
        }
    }

    private fun fetchUserData() {

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userRef = db.collection("users").document(currentUser.uid)
            userRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    _userData.value = user
                }
            }.addOnFailureListener { exception ->
                // Handle error fetching user data
            }
        }
    }

    fun updateEmail(newEmail: String) {
        val currentUser = auth.currentUser ?: return
        val userRef = db.collection("users").document(currentUser.uid)

        userRef.update("email", newEmail)
            .addOnSuccessListener {
                // Email updated successfully
                _userData.value = _userData.value?.copy(email = newEmail)
                _isEditingEmail.value = false
            }
            .addOnFailureListener { exception ->
                // Handle error updating email
            }
    }
    fun updatePhoneNumber(newPhoneNumber: String) {
        val currentUser = auth.currentUser ?: return
        val userRef = db.collection("users").document(currentUser.uid)

        userRef.update("phoneNumber", newPhoneNumber)
            .addOnSuccessListener {
                // Phone number updated successfully
                _userData.value = _userData.value?.copy(phoneNumber = newPhoneNumber)
                _isEditingPhoneNumber.value = false
            }
            .addOnFailureListener { exception ->
                // Handle error updating phone number
            }
    }
    suspend fun uploadProfilePhoto(imageFile: File) {
        val currentUser = auth.currentUser ?: return
        val storageRef = storage.reference.child("profile_pictures/${currentUser.uid}.jpg")
        val uploadTask = storageRef.putFile(imageFile.toUri())
        uploadTask.await() // Wait for upload to complete
        val downloadUrl = storageRef.downloadUrl.await().toString()
        // Update profilePictureUrl in Firestore and update _user state
    }

    fun deleteAccount() {
        val currentUser = auth.currentUser ?: return
        // Delete user data from Firestore
        db.collection("users").document(currentUser.uid).delete()
            .addOnSuccessListener {
                // Delete user from Firebase Authentication
                currentUser.delete()
                    .addOnSuccessListener {
                        // Account deleted successfully, navigate to login screen
                    }
                    .addOnFailureListener { exception ->
                        // Handle error deleting user from authentication
                    }
            }
            .addOnFailureListener { exception ->
                // Handle error deleting user data from Firestore
            }
    }

    fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                // Password reset email sentsuccessfully
            }
            .addOnFailureListener { exception ->
                // Handle error sending password reset email
            }
    }
    fun selectImageFromGallery(navController: NavHostController) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        navController.context.startActivity(Intent.createChooser(intent, "Select Picture"))
    }


    fun uploadImageToFirebase(userId: String, onUploadSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        selectedImageUri?.let { uri ->
            val ref = storageRef.child("profile_pictures/$userId.jpg")
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { downloadUri ->
                        // Call onUploadSuccess with the image URL
                        onUploadSuccess(downloadUri.toString())
                    }.addOnFailureListener { exception ->
                        onFailure(exception)
                    }
                }.addOnFailureListener { exception ->
                    onFailure(exception)
                }
        }
    }
}