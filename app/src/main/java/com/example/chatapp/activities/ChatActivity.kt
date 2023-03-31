package com.example.chatapp.activities

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.adapters.ChatAdapter
import com.example.chatapp.databinding.ActivityChatBinding
import com.example.chatapp.models.ChatMessage
import com.example.chatapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatActivity : AppCompatActivity() {
    // Variables for user interface and data
    private lateinit var binding: ActivityChatBinding
    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var chatId: String
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "User1"

    // This is called when the activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase authentication
        auth = FirebaseAuth.getInstance()

        // Get the chat ID from the intent
        chatId = intent.getStringExtra("CHAT_ID") ?: ""

        // Set up the list of messages (RecyclerView)
        adapter = ChatAdapter(chatMessages, auth.currentUser?.uid ?: "")
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Listen for button clicks
        binding.buttonSend.setOnClickListener {
            sendMessage()
        }

        binding.buttonSignOut.setOnClickListener {
            signOut()
        }

        // Start listening for new messages
        listenForMessages()
    }

    // Function to send a message
    private fun sendMessage() {
        // Get the message text from the input field
        val messageText = binding.editTextMessage.text.toString().trim()

        // Check if the message is not empty
        if (messageText.isNotEmpty()) {
            // Get user info from Firebase
            val userRef =
                FirebaseDatabase.getInstance().getReference("/users/${auth.currentUser?.uid}")
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                // When we get the user data
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Create a new message and send it
                    val displayName = snapshot.child("displayName").value.toString()

                    val chatMessage = ChatMessage(
                        message = messageText,
                        sender = auth.currentUser?.uid ?: "",
                        timestamp = System.currentTimeMillis(),
                        displayName = displayName
                    )

                    val ref = FirebaseDatabase.getInstance().getReference("/chats/$chatId/messages").push()
                    ref.setValue(chatMessage)
                        .addOnSuccessListener {
                            // Clear the input field
                            binding.editTextMessage.setText("")
                        }
                        .addOnFailureListener { exception ->
                            // Show an error message if sending fails
                            Toast.makeText(
                                this@ChatActivity,
                                "Failed to send message: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error when fetching user data
                }
            })
        } else {
            // Show a message if the input is empty
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to listen for new messages
    private fun listenForMessages() {
        // Get the messages from Firebase
        val ref = FirebaseDatabase.getInstance().getReference("/chats/$chatId/messages")
        ref.addValueEventListener(object : ValueEventListener {
            // When we get new messages
            override fun onDataChange(snapshot: DataSnapshot) {
                // Update the list of messages
                chatMessages.clear()
                snapshot.children.forEach { data ->
                    val message = data.getValue(ChatMessage::class.java)
                    message?.let { chatMessages.add(it) }
                }
                // Update the user interface
                adapter.notifyDataSetChanged()
                binding.recyclerView.scrollToPosition(chatMessages.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error when fetching messages
            }
        })
    }

    // Function to sign out
    private fun signOut() {
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}


