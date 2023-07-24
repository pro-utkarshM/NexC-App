package com.example.nittalk.ui.groupchat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nittalk.data.User
import com.example.nittalk.databinding.ItemUserStatusBinding
import com.example.nittalk.util.Comparators.USER_COMPARATOR

class OfflineStatusAdapter: ListAdapter<User, OfflineStatusAdapter.OfflineStatusViewHolder>(USER_COMPARATOR) {

    inner class OfflineStatusViewHolder(private val binding: ItemUserStatusBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.apply {
                userStatusNameTextView.text = user.name
                Glide.with(binding.root).load(user.profileImageUrl).circleCrop().into(userStatusDp)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfflineStatusViewHolder {
        val binding = ItemUserStatusBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val statusViewHolder = OfflineStatusViewHolder(binding)
        return statusViewHolder
    }

    override fun onBindViewHolder(holderOnline: OfflineStatusViewHolder, position: Int) {
        val currentItem = getItem(position)
        holderOnline.bind(currentItem)
    }
}