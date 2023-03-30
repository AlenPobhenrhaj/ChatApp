package com.example.chatapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.databinding.ItemSelectableUserBinding
import com.example.chatapp.models.User

class GroupMemberAdapter : RecyclerView.Adapter<GroupMemberAdapter.GroupMemberViewHolder>() {
    private val users = mutableListOf<User>()
    private val selectedUsers = mutableSetOf<String>()

    fun submitList(newUsers: List<User>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupMemberViewHolder {
        val binding = ItemSelectableUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupMemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupMemberViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    fun getSelectedUserIds(): MutableList<String> {
        return selectedUsers.toMutableList()
    }

    inner class GroupMemberViewHolder(private val binding: ItemSelectableUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.userName.text = user.displayName
            binding.checkbox.isChecked = selectedUsers.contains(user.id)

            binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedUsers.add(user.id!!)
                } else {
                    selectedUsers.remove(user.id)
                }
            }
        }
    }
}
