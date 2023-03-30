package com.example.chatapp.models

data class Group(
    val id: String = "",
    val groupName: String = "",
    val users: List<String> = listOf()
)