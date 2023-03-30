package com.example.chatapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.databinding.ItemChatMessageBinding
import com.example.chatapp.models.GroupChatMessage

class GroupChatAdapter(private val groupChatMessages: List<GroupChatMessage>, private val currentUserId: String) :
    RecyclerView.Adapter<GroupChatAdapter.GroupChatMessageViewHolder>() {

    // Create a new message view holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupChatMessageViewHolder {
        val binding = ItemChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupChatMessageViewHolder(binding)
    }

    // Show a chat message in the view holder
    override fun onBindViewHolder(holder: GroupChatMessageViewHolder, position: Int) {
        val groupChatMessage = groupChatMessages[position]

        // Check if the message is sent by the current user
        if (groupChatMessage.sender == currentUserId) {
            // Show the message on the right side
            holder.binding.rightMessageLayout.visibility = View.VISIBLE
            holder.binding.leftMessageLayout.visibility = View.GONE
            holder.binding.rightMessageText.text = groupChatMessage.message
            holder.binding.rightMessageLayout.setBackgroundResource(R.drawable.rounded_corner_sent)
            holder.binding.rightMessageSender.text = "You"
        } else {
            // Show the message on the left side
            holder.binding.leftMessageLayout.visibility = View.VISIBLE
            holder.binding.rightMessageLayout.visibility = View.GONE
            holder.binding.leftMessageText.text = groupChatMessage.message
            holder.binding.leftMessageLayout.setBackgroundResource(R.drawable.rounded_corner_received)
            holder.binding.leftMessageSender.text = groupChatMessage.displayName
        }
    }

    // Get the total number of chat messages
    override fun getItemCount(): Int {
        return groupChatMessages.size
    }

    class GroupChatMessageViewHolder(val binding: ItemChatMessageBinding) : RecyclerView.ViewHolder(binding.root)
}
