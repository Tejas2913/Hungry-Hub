package com.example.hungryhub.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hungryhub.databinding.NotificationItemBinding
import com.example.hungryhub.model.Notification

class NotificationAdapter(
    private val notifications: ArrayList<Notification>,
    private val notificationImages: ArrayList<Int>,
    private val onRead: (String) -> Unit,
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = NotificationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding, onRead)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification.message, notificationImages[position]) // Pass the message directly
    }

    override fun getItemCount(): Int = notifications.size

    class NotificationViewHolder(
        private val binding: NotificationItemBinding,
        private val onRead: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(notificationMessage: String, notificationImage: Int) {
            binding.notificationtextView.text = notificationMessage // Set the message text
            binding.notificationimageView.setImageResource(notificationImage)

            // Set click listener to mark as read
            binding.root.setOnClickListener {
                onRead(notificationMessage) // Call onRead with the notification message
            }
        }
    }
}
