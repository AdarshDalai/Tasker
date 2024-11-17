package com.cloudsbay.tasker.ui.screens.profile

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.cloudsbay.tasker.R
import com.cloudsbay.tasker.nav.BottomNavigationItem
import com.cloudsbay.tasker.nav.NavigationRoute
import com.cloudsbay.tasker.ui.screen.Authentication.AuthenticationViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePage(
    navController: NavHostController,
    viewModel: AuthenticationViewModel
) {
    val user by viewModel.userData.collectAsState()


    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        ProfilePictureAndUserName(
            text = user?.username ?: "Dummy",
            authViewModel = viewModel,
            navController = navController,
            profileImageUrl = user?.profilePictureUrl ?: null
        )
        Spacer(modifier = Modifier.height(16.dp))
        ElevatedCard(
            modifier = Modifier
                .padding(2.dp)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("User details",
                        fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        expanded = !expanded
                    }) {
                        if(!expanded)
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Show")
                        else
                            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Hide")
                    }
                }

                if (expanded) {
                    // Display profile details when expanded
                    LazyColumn(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        item {
                            EditDetailRow(
                                label = "Name",
                                value = user?.name ?: "",
                                isEditing = viewModel.isEditingName.collectAsState().value,
                                onEditClick = { viewModel.toggleEditName() },
                                onUpdate = { viewModel.updateName(it) }
                            )
                        }

                        item {
                            EditDetailRow(
                                label = "Email",
                                value = user?.email ?: "",
                                isEditing = viewModel.isEditingEmail.collectAsState().value,
                                onEditClick = { viewModel.toggleEditEmail() },
                                onUpdate = { viewModel.updateEmail(it) }
                            )
                        }

                        item {
                            EditDetailRow(
                                label = "Phone Number",
                                value = user?.phoneNumber ?: "",
                                isEditing = viewModel.isEditingPhoneNumber.collectAsState().value,
                                onEditClick = { viewModel.toggleEditPhoneNumber() },
                                onUpdate = { viewModel.updatePhoneNumber(it) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.deleteAccount() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Delete Account")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.resetPassword(user?.email ?: "")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset Password")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.logout()
                navController.navigate(NavigationRoute.Login.route) {
                    popUpTo(BottomNavigationItem.Home.route) {
                        inclusive = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
    }

}

@Composable
fun EditDetailRow(
    label: String,
    value: String,
    isEditing: Boolean,
    onEditClick: () -> Unit,
    onUpdate: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    fontWeight = FontWeight(300),
                    modifier = Modifier
                        .weight(9f)
                        .padding(end = 8.dp), // Add padding between text and icon
                )

                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isEditing) {
                var newValue by remember { mutableStateOf(value) }
                OutlinedTextField(
                    value = newValue,
                    onValueChange = { newValue = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = { onUpdate(newValue) }) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
fun ProfilePictureAndUserName(
    text: String,
    authViewModel: AuthenticationViewModel,
    navController: NavHostController,
    profileImageUrl: String? = null
) {
    val context = LocalContext.current

    // Launcher to select an image from the gallery
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            authViewModel.selectImageFromGallery(it)

            // Get current user ID from Firebase Auth
            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid ?: return@let

            // Upload image to Firebase Storage and update Firestore
            authViewModel.uploadImageToFirebase(
                userId,
                onUploadSuccess = { imageUrl ->
                    // Update Firestore with the new image URL
                    authViewModel.updateProfilePictureUrl(userId, imageUrl)
                },
                onFailure = { exception ->
                    Log.e("ProfilePictureAndUserName", "Image upload failed: ${exception.message}")
                }
            )
        }
    }


    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(300.dp)
            ) {
                // Profile Photo Card
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    val painter = rememberAsyncImagePainter(
                        model = profileImageUrl ?: R.drawable.ic_launcher_background,
                        contentScale = ContentScale.Crop
                    )

                    Image(
                        painter = painter,
                        contentDescription = "Profile Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Edit Button Overlay
                IconButton(
                    onClick = { launcher.launch("image/*") }, // Trigger the image picker
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile Photo",
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile name text
        Text(
            text = text,
            fontSize = 35.sp,
            fontWeight = FontWeight(10),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfilePagePreview() {
    //ProfilePictureAndUserName("Dummy")
}

@Preview(showBackground = true)
@Composable
fun EditDetailRowPreview() {
    EditDetailRow(
        label = "Name",
        value = "John Doe Lorem Ipsum Dolor Sit Amet WQrshghahhvhbjbjhbjhsbjhbjhbjhbkjhbjhkvh",
        isEditing = false,
        onEditClick = { /* Handle Edit Click */ },
        onUpdate = { /* Handle Update */ }
    )
}
