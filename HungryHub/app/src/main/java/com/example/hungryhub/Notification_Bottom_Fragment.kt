package com.example.hungryhub

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hungryhub.adapter.NotificationAdapter
import com.example.hungryhub.databinding.FragmentNotificationBottomBinding
import com.example.hungryhub.model.Notification
import com.google.firebase.database.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth

class Notification_Bottom_Fragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentNotificationBottomBinding
    private lateinit var notifications: ArrayList<Notification>
    private lateinit var notificationImages: ArrayList<Int>
    private lateinit var notificationRef: DatabaseReference
    private lateinit var childEventListener: ChildEventListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentNotificationBottomBinding.inflate(inflater, container, false)

        // Initialize notification lists
        notifications = arrayListOf()
        notificationImages = arrayListOf()

        // Get the current user ID
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            // Handle case where the user is not signed in
            return binding.root
        }

        val userId = currentUser.uid
        notificationRef = FirebaseDatabase.getInstance().reference.child("Notifications").child(userId)

        // Initialize RecyclerView
        val adapter = NotificationAdapter(notifications, notificationImages, ::markAsRead)
        binding.notificationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.notificationRecyclerView.adapter = adapter

        // Listen for new notifications
        childEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // Get the Notification object from the snapshot
                val notification = snapshot.getValue(Notification::class.java)
                notification?.let {
                    // Add the entire Notification object
                    it.key = snapshot.key.toString()
                    notifications.add(it)
                    notificationImages.add(getNotificationImage(it.message))

                    // Notify adapter of the change
                    binding.notificationRecyclerView.adapter?.notifyItemInserted(notifications.size - 1)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }

        notificationRef.addChildEventListener(childEventListener)

        return binding.root
    }

    private fun markAsRead(notificationKey: String) { // Accept the key now
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: return

        // Find the notification index using the key
        val index = notifications.indexOfFirst { it.key == notificationKey }
        if (index != -1) {
            // Remove the notification from the local list
            notifications.removeAt(index)
            notificationImages.removeAt(index)
            binding.notificationRecyclerView.adapter?.notifyItemRemoved(index)

            // Remove the notification from Firebase
            notificationRef.child(notificationKey).removeValue().addOnCompleteListener {
                if (it.isSuccessful) {
                    // Successfully removed, update notification count
                    (activity as MainActivity).fetchNotificationCount() // Update the badge count
                } else {
                    Log.e("Notification", "Failed to delete notification: ${it.exception?.message}")
                }
            }
        }
    }

    private fun getNotificationImage(notification: String): Int {
        return when {
            notification.contains("cancelled", true) -> R.drawable.sademoji
            notification.contains("accepted", true) -> R.drawable.congrats
            notification.contains("dispatched", true) -> R.drawable.truck
            else -> R.drawable.notification_icon // Default icon
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove the listener to prevent memory leaks
        notificationRef.removeEventListener(childEventListener)
    }
}
