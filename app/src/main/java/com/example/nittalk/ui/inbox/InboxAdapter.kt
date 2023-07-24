package com.example.nittalk.ui.inbox

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nittalk.data.Inbox
import com.example.nittalk.databinding.ItemFriendChatBinding
import com.example.nittalk.util.Comparators.INBOX_COMPARATOR
import com.example.nittalk.util.MessageTimeUtil

class InboxAdapter(
    private val onFriendItemClickListener: OnFriendItemClickListener
) : ListAdapter<Inbox, InboxAdapter.InboxViewHolder>(INBOX_COMPARATOR) {

    inner class InboxViewHolder(private val binding: ItemFriendChatBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(inbox: Inbox) {
            binding.apply {
                Glide.with(binding.root).load(inbox.friendDp).circleCrop()
                    .into(friendDp)
                friendName.text = inbox.friendName
                if (inbox.lastMessage == "") {
                    friendLastMessage.text = "Start Chat With ${inbox.friendName}"
                } else {
                    friendLastMessage.text = inbox.lastMessage
                }

                if (inbox.lastMessageTime == null) {
                    friendLastMessageTime.visibility = View.GONE
                } else {
                    val sentTime = MessageTimeUtil.getTimeAgoFriendChat(inbox.lastMessageTime!!)
                    friendLastMessageTime.text = sentTime
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InboxViewHolder {
        val binding =
            ItemFriendChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val friendChatViewHolder = InboxViewHolder(binding)
        binding.friendCardView.setOnClickListener {
            val current = getItem(friendChatViewHolder.absoluteAdapterPosition)
            onFriendItemClickListener.onFriendItemClick(
                current.friendId,
                current.friendName,
                current.lastMessage
            )
        }
        return friendChatViewHolder
    }

    override fun onBindViewHolder(holder: InboxViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }
}

interface OnFriendItemClickListener {
    fun onFriendItemClick(friendId: String, friendName: String, lastMessage: String?)
}