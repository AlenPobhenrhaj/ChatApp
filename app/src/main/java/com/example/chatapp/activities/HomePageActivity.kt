package com.example.chatapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.databinding.ActivityHomePageBinding

class HomePageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up onClick listeners for the buttons
        binding.buttonChat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        binding.buttonUserList.setOnClickListener {
            startActivity(Intent(this, UserListActivity::class.java))
        }
    }
}
