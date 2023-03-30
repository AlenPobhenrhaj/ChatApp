package com.example.chatapp.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.activities.ChatActivity
import com.example.chatapp.activities.GroupChatActivity
import com.example.chatapp.databinding.ItemUserBinding
import com.example.chatapp.models.User

class UserListAdapter : ListAdapter<User, UserListAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(private val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.userName.text = user.displayName
            // Set up an onClickListener
            binding.root.setOnClickListener {
                onItemClick(user, binding)
            }
        }
    }

    private fun onItemClick(user: User, binding: ItemUserBinding) {
        val context = binding.root.context
        val intent: Intent = if (user.isGroup) {
            Intent(context, GroupChatActivity::class.java)
        } else {
            Intent(context, ChatActivity::class.java)
        }
        intent.putExtra("USER_ID", user.id)
        intent.putExtra("USER_NAME", user.displayName)
        context.startActivity(intent)
    }

    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}
