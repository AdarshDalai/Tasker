package com.cloudsbay.tasker.data

import java.util.UUID

data class Task(
    val taskId: String = UUID.randomUUID().toString(), // Store UUID as a String
    val taskName: String = "",
    val description: String? = null,
    val deadline: String = "",
    val priority: String = "Low",
    val status: String = "Pending",
    val userId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    constructor() : this(
        taskId = UUID.randomUUID().toString(),
        taskName = "",
        description = null,
        deadline = "",
        priority = "Low",
        status = "Pending",
        userId = UUID.randomUUID().toString(),
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}