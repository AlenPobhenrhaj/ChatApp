package com.example.chatapp.models

data class ChatMessage(
    val message: String = "",
    val sender: String = "",
    val timestamp: Long = 0L,
    val displayName: String = "" // Add this property
)

