package com.example.hungryhub

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.hungryhub.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var notificationCount = 0
    private lateinit var notificationRef: DatabaseReference // Declare notificationRef

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up Navigation
        val navController = findNavController(R.id.fragmentContainerView)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView7)
        bottomNav.setupWithNavController(navController)

        // Set up notification button click listener
        binding.notificationButton.setOnClickListener {
            val bottomSheetDialog = Notification_Bottom_Fragment()
            bottomSheetDialog.show(supportFragmentManager, "Test")
        }

        // Initialize Firebase reference for notifications
        fetchNotificationCount() // Fetch notification count on create
    }

    // Method to update the notification badge
    fun updateNotificationBadge(count: Int) {
        notificationCount = count
        binding.notificationBadge.apply {
            text = if (count > 0) count.toString() else ""
            visibility = if (count > 0) View.VISIBLE else View.GONE
        }
    }

    // Method to fetch notification count from Firebase
    fun fetchNotificationCount() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            notificationRef = FirebaseDatabase.getInstance().reference.child("Notifications").child(currentUser.uid)
            notificationRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    notificationCount = snapshot.childrenCount.toInt() // Get the count of notifications
                    updateNotificationBadge(notificationCount) // Update badge
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if needed
                }
            })
        }
    }

    // Method to handle the notification count update from the fragment
    fun updateNotificationCountOnRead() {
        fetchNotificationCount() // Re-fetch the notification count
    }
}
