package com.example.chatapp.adapters


import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.activities.GroupChatActivity
import com.example.chatapp.databinding.ItemGroupChatBinding
import com.example.chatapp.models.Group

class GroupListAdapter : ListAdapter<Group, GroupListAdapter.GroupViewHolder>(GroupDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = ItemGroupChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GroupViewHolder(private val binding: ItemGroupChatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(group: Group) {
            binding.groupChatName.text = group.groupName
            binding.root.setOnClickListener {
                onItemClick(group, binding)
            }
        }
    }

    private fun onItemClick(group: Group, binding: ItemGroupChatBinding) {
        val context = binding.root.context
        val intent = Intent(context, GroupChatActivity::class.java)
        intent.putExtra("GROUP_ID", group.id)
        intent.putExtra("GROUP_NAME", group.groupName)
        context.startActivity(intent)
    }

    class GroupDiffCallback : DiffUtil.ItemCallback<Group>() {
        override fun areItemsTheSame(oldItem: Group, newItem: Group): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Group, newItem: Group): Boolean {
            return oldItem == newItem
        }
    }
}
