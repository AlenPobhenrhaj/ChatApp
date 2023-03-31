package com.example.chatapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.adapters.GroupListAdapter
import com.example.chatapp.adapters.GroupMemberAdapter
import com.example.chatapp.adapters.UserListAdapter
import com.example.chatapp.databinding.ActivityUserlistPageBinding
import com.example.chatapp.databinding.DialogCreateGroupBinding
import com.example.chatapp.models.Group
import com.example.chatapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserlistPageBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userRef: DatabaseReference
    private lateinit var adapter: UserListAdapter
    private lateinit var groupAdapter: GroupListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserlistPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userRef = database.getReference("users")

        // Set up RecyclerView
        adapter = UserListAdapter { user ->
            val chatId = createOneToOneChatRoom(user)
            openChatActivity(user, chatId)
        }
        binding.userRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.userRecyclerView.adapter = adapter

        // Set up Group RecyclerView
        groupAdapter = GroupListAdapter()
        binding.groupChatRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.groupChatRecyclerView.adapter = groupAdapter

        binding.buttonSignOut.setOnClickListener {
            signOut()
        }

        // Set up onClick listeners for the buttons
        binding.buttonUsers.setOnClickListener {
            showUserList()
        }

        binding.buttonGroupChat.setOnClickListener {
            showGroupChatList()
        }

        // Add the FAB click listener
        binding.createGroupFab.setOnClickListener {
            createGroupChatDialog()
        }

        loadUsers()
        loadGroups()

    }

    private fun loadUsers() {
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val users = mutableListOf<User>()
                val currentUserId = auth.currentUser?.uid ?: return
                for (childSnapshot in dataSnapshot.children) {
                    val user = childSnapshot.getValue(User::class.java)
                    user?.id = childSnapshot.key
                    if (user != null && user.id != currentUserId) {
                        users.add(user)
                    }
                }
                adapter.submitList(users)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
            }
        })
    }

    private fun loadGroups() {
        val groupRef = database.getReference("groups")
        val currentUserId = auth.currentUser?.uid ?: return

        groupRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val groups = mutableListOf<Group>()
                for (childSnapshot in dataSnapshot.children) {
                    val group = childSnapshot.getValue(Group::class.java)
                    if (group != null && group.users.contains(currentUserId)) {
                        group.id = childSnapshot.key.toString()
                        groups.add(group)
                    }
                }
                groupAdapter.submitList(groups)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
            }
        })
    }


    private fun showUserList() {
        binding.userRecyclerView.visibility = View.VISIBLE
        binding.groupChatRecyclerView.visibility = View.GONE
        binding.createGroupFab.visibility = View.GONE
    }

    private fun showGroupChatList() {
        binding.userRecyclerView.visibility = View.GONE
        binding.groupChatRecyclerView.visibility = View.VISIBLE
        binding.createGroupFab.visibility = View.VISIBLE
    }


    private fun createGroupChatDialog() {
        val binding = DialogCreateGroupBinding.inflate(layoutInflater)
        val groupMemberAdapter = GroupMemberAdapter()

        binding.groupMembersRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = groupMemberAdapter
        }

        loadSelectableUsers(groupMemberAdapter)

        val builder = AlertDialog.Builder(this).apply {
            setTitle("Create Group Chat")
            setView(binding.root)
            setPositiveButton("Create") { _, _ ->
                val groupName = binding.groupNameEditText.text.toString().trim()
                if (groupName.isNotEmpty()) {
                    val selectedUserIds = groupMemberAdapter.getSelectedUserIds()
                    Log.d("SelectedUserIds", "Selected users: $selectedUserIds")
                    selectedUserIds.add(auth.currentUser?.uid ?: return@setPositiveButton)
                    createGroupChat(groupName, selectedUserIds)
                } else {
                    Toast.makeText(this@UserListActivity, "Group name cannot be empty.", Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        }
        builder.show()
    }


    private fun loadSelectableUsers(adapter: GroupMemberAdapter) {
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val users = mutableListOf<User>()
                val currentUserId = auth.currentUser?.uid ?: return
                for (childSnapshot in dataSnapshot.children) {
                    val user = childSnapshot.getValue(User::class.java)
                    if (user != null && user.id != currentUserId) {
                        user.id = childSnapshot.key
                        users.add(user)
                    }
                }
                adapter.submitList(users)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
            }
        })
    }


    private fun createGroupChat(groupName: String, selectedUserIds: List<String>) {
        if (selectedUserIds.size < 2) { // Minimum 2 selected users + current user
            Toast.makeText(this, "Please select at least 2 more users to create a group.", Toast.LENGTH_SHORT).show()
            return
        }

        val groupId = FirebaseDatabase.getInstance().getReference("/groups").push().key ?: return
        val groupUsers = selectedUserIds.toMutableList()
        groupUsers.add(auth.currentUser?.uid ?: return)

        val group = Group(id = groupId, groupName = groupName, users = groupUsers)
        FirebaseDatabase.getInstance().getReference("/groups/$groupId").setValue(group)
            .addOnSuccessListener {
                startActivity(Intent(this, GroupChatActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                // Handle group chat creation failure
            }
    }

    private fun createOneToOneChatRoom(user: User): String {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return ""
        val chatId = if (currentUserId < user.id.toString()) {
            "$currentUserId-${user.id}"
        } else {
            "${user.id}-$currentUserId"
        }
        return chatId
    }

    private fun openChatActivity(user: User, chatId: String) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("USER_ID", user.id)
        intent.putExtra("USER_NAME", user.displayName)
        intent.putExtra("CHAT_ID", chatId)
        startActivity(intent)
    }

    private fun signOut() {
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
