package com.example.chatapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.adapters.ChatAdapter
import com.example.chatapp.databinding.ActivityGroupChatBinding
import com.example.chatapp.models.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GroupChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGroupChatBinding
    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var groupChatId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        groupChatId = intent.getStringExtra("GROUP_CHAT_ID") ?: ""

        adapter = ChatAdapter(chatMessages, auth.currentUser?.uid ?: "")
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.buttonSend.setOnClickListener {
            sendMessage()
        }

        binding.buttonSignOut.setOnClickListener {
            signOut()
        }

        listenForMessages()
    }

    private fun sendMessage() {
        val messageText = binding.editTextMessage.text.toString().trim()

        if (messageText.isNotEmpty()) {
            val userRef = FirebaseDatabase.getInstance().getReference("/users/${auth.currentUser?.uid}")
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val displayName = snapshot.child("displayName").value.toString()

                    val chatMessage = ChatMessage(
                        message = messageText,
                        sender = auth.currentUser?.uid ?: "",
                        timestamp = System.currentTimeMillis(),
                        displayName = displayName
                    )

                    val ref = FirebaseDatabase.getInstance().getReference("/groupChats/$groupChatId/messages").push()
                    ref.setValue(chatMessage)
                        .addOnSuccessListener {
                            binding.editTextMessage.setText("")
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                this@GroupChatActivity,
                                "Failed to send message: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        } else {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
        }
    }

    private fun listenForMessages() {
        val ref = FirebaseDatabase.getInstance().getReference("/groupChats/$groupChatId/messages")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatMessages.clear()
                snapshot.children.forEach { data ->
                    val message = data.getValue(ChatMessage::class.java)
                    message?.let { chatMessages.add(it) }
                }
                adapter.notifyDataSetChanged()
                binding.recyclerView.scrollToPosition(chatMessages.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun signOut() {
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
