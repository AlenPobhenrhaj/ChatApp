package com.example.chatapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.buttonRegister.setOnClickListener {
            register()
        }

        binding.buttonLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun register() {
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()
        val displayName = binding.editTextDisplayName.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty() && displayName.isNotEmpty()) {
            // Create a new user with email and password
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Get the user's unique ID
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            // Save the display name to the Realtime Database
                            val userRef = FirebaseDatabase.getInstance().getReference("/users/$userId")
                            userRef.child("displayName").setValue(displayName)
                                .addOnCompleteListener { saveTask ->
                                    if (saveTask.isSuccessful) {
                                        // If successful, go to the chat screen
                                        startActivity(Intent(this, HomePageActivity::class.java))
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Failed to save display name: ${saveTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    } else {
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }

}