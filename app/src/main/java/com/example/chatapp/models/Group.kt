package com.example.chatapp.models

data class Group(
    var id: String = "",
    val groupName: String = "",
    val users: List<String> = listOf()
)