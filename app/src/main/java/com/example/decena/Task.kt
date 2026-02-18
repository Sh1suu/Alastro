package com.example.decena

data class Task(
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val date: Long, // timestamp
    val time: String,
    val priority: String = "Medium",
    val category: String = "General",
    val isCompleted: Boolean = false
)