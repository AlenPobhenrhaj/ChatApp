package com.example.chatapp.models

data class GroupChatMessage(
    val message: String = "",
    val sender: String = "",
    val timestamp: Long = 0L,
    val displayName: String = "",
    val groupId: String = ""
)
