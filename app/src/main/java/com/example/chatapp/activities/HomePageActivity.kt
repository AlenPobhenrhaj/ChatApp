package com.example.chatapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.R
import com.example.chatapp.databinding.ActivityHomePageWithBottomNavBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class HomePageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomePageWithBottomNavBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageWithBottomNavBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the bottom navigation view
        binding.bottomNavigationView.setOnItemSelectedListener(bottomNavListener)

        auth = FirebaseAuth.getInstance()

        // Set the default screen to ChatActivity
        /*startActivity(Intent(this, ChatActivity::class.java))*/

        binding.buttonSignOut.setOnClickListener {
            signOut()
        }
    }

    private val bottomNavListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_chat -> {
                startActivity(Intent(this, ChatActivity::class.java))
                true
            }
            R.id.nav_user_list -> {
                startActivity(Intent(this, UserListActivity::class.java))
                true
            }
            else -> false
        }
    }

    private fun signOut() {
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
