package com.example.chatapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.adapters.ChatAdapter
import com.example.chatapp.databinding.ActivityChatBinding
import com.example.chatapp.models.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter
    private lateinit var auth: FirebaseAuth

    // Replace this with a unique identifier for the user, e.g., FirebaseAuth currentUser UID.
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "User1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Initialize RecyclerView
        adapter = ChatAdapter(chatMessages, auth.currentUser?.uid ?: "")
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        Log.d("TAG", "ChatActivity launched")

        // Set up listeners
        binding.buttonSend.setOnClickListener {
            Log.d("TAG", "Send button clicked")
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

                    val ref = FirebaseDatabase.getInstance().getReference("/messages").push()
                    Log.d("TAG", "Before calling setValue() for ref: $ref")
                    ref.setValue(chatMessage)
                        .addOnSuccessListener {
                            binding.editTextMessage.setText("")
                            Log.d("TAG", "Message sent: $messageText")
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this@ChatActivity, "Failed to send message: ${exception.message}", Toast.LENGTH_SHORT).show()
                            Log.e("TAG", "Failed to send message", exception)
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("TAG", "Error fetching display name", error.toException())
                }
            })
        } else {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
        }
    }


    private fun listenForMessages() {
        val ref = FirebaseDatabase.getInstance().getReference("/messages")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatMessages.clear()
                snapshot.children.forEach { data ->
                    val message = data.getValue(ChatMessage::class.java)
                    message?.let { chatMessages.add(it) }
                }
                adapter.notifyDataSetChanged()
                binding.recyclerView.scrollToPosition(chatMessages.size - 1)
                Log.d("TAG", "DataSnapshot received: $snapshot")
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.e("TAG", "Error fetching messages", error.toException())
            }
        })
    }

    private fun signOut() {
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}



