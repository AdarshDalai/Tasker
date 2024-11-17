package com.cloudsbay.tasker.data

data class User(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val username: String = "",
    val phoneNumber: String = "",
    val profilePictureUrl: String? = null // TODO: Add profile photo URL
)
