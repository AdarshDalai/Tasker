package com.cloudsbay.tasker.repository

import com.cloudsbay.tasker.data.Task
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class TaskRepository {

    private val db = FirebaseFirestore.getInstance()
    private val tasksCollection = db.collection("tasks")


    fun saveTask(task: Task, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val taskMap = hashMapOf(
            "taskName" to task.taskName,
            "description" to task.description,
            "deadline" to task.deadline,
            "priority" to task.priority,
            "status" to task.status,
            "userId" to task.userId.toString(),
            "createdAt" to task.createdAt,
            "updatedAt" to task.updatedAt
        )

        tasksCollection.document(task.taskId.toString())
            .set(taskMap)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    fun getTasks(userId: UUID, onComplete: (List<Task>) -> Unit, onFailure: (Exception) -> Unit) {
        tasksCollection
            .whereEqualTo("userId", userId.toString())
            .get()
            .addOnSuccessListener { result ->
                val tasks = result.mapNotNull { document ->
                    document.toObject(Task::class.java).copy(taskId = document.id)
                }
                onComplete(tasks)
            }
            .addOnFailureListener { exception -> onFailure(exception) }
    }
}