package com.example.hungryhub.Fragment

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.hungryhub.MainActivity
import com.example.hungryhub.R
import com.example.hungryhub.adapter.BuyAgainAdapter
import com.example.hungryhub.databinding.FragmentHistoryBinding
import com.example.hungryhub.model.OrderDetails
import com.example.hungryhub.RecentOrderItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.Serializable

class HistoryFragment : Fragment() {
    private lateinit var binding: FragmentHistoryBinding
    private lateinit var buyAgainAdapter: BuyAgainAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var userId : String
    private var listOfOrderItem : MutableList<OrderDetails> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHistoryBinding.inflate(layoutInflater,container,false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Retrieve and display the user order history
        retrieveBuyHistory()

        binding.recentbuyItem.setOnClickListener {
            seeItemsRecentBuy()
        }
        binding.receivedButton.setOnClickListener {

            updateOrderStatus()
        }
        return binding.root
    }

    private fun updateOrderStatus() {
        val itemPushKey = listOfOrderItem[0].itemPushKey
        val completeOrderReference = database.reference.child("CompletedOrder").child(itemPushKey!!)
        val buyOrderReference = database.reference.child("user").child(userId).child("BuyHistory").child(itemPushKey!!)
        completeOrderReference.child("paymentReceived").setValue(true)
        completeOrderReference.child("orderAccepted").setValue(true)
        buyOrderReference.child("paymentReceived").setValue(true)
        showThankYouNotification(requireContext())
        binding.receivedButton.visibility = View.INVISIBLE
    }

    private fun seeItemsRecentBuy() {
        listOfOrderItem.sortByDescending { it.currentTime }
        listOfOrderItem.firstOrNull().let { recentBuy ->
            val intent = Intent(requireContext(),RecentOrderItems::class.java)
            intent.putExtra("RecentBuyOrderItem",listOfOrderItem as Serializable)
            startActivity(intent)
        }
    }

    private fun retrieveBuyHistory() {
        binding.recentbuyItem.visibility = View.INVISIBLE
        userId = auth.currentUser?.uid?:""
        val buyItemReference : DatabaseReference = database.reference.child("user").child(userId).child("BuyHistory")
        val sortingQuery = buyItemReference.orderByChild("currentTime")
        sortingQuery.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (buySnapshot in snapshot.children){
                    val buyHistoryItem = buySnapshot.getValue(OrderDetails::class.java)
                    buyHistoryItem?.let {
                        listOfOrderItem.add(it)
                    }
                }
                listOfOrderItem.reverse()
                if (listOfOrderItem.isNotEmpty()){
                    setDataInRecentBuyItem()
                    setPreviousBuyItemsRecyclerView()
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    @SuppressLint("SetTextI18n")
    private fun setDataInRecentBuyItem() {
        binding.recentbuyItem.visibility = View.VISIBLE
        val recentOrderItem = listOfOrderItem.firstOrNull()
        recentOrderItem?.let {
            with(binding) {
                buyAgainFoodName.text = it.foodNames?.firstOrNull() ?: ""
                buyAgainFoodPrice.text = "₹${it.foodPrices?.firstOrNull() ?: ""}"
                val image = it.foodImages?.firstOrNull() ?: ""
                val uri = Uri.parse(image)
                Glide.with(requireContext()).load(uri).into(buyAgainFoodImage)

                val isOrderAccepted = it.orderAccepted ?: false
                val isPaymentReceived = it.paymentReceived ?: false
                if (isOrderAccepted) {
                    orderStatus.background.setTint(android.graphics.Color.GREEN)
                    if (isPaymentReceived) {
                        receivedButton.visibility = View.INVISIBLE
                    } else {
                        receivedButton.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun setPreviousBuyItemsRecyclerView() {
        val buyAgainFoodName = mutableListOf<String>()
        val buyAgainFoodPrice = mutableListOf<String>()
        val buyAgainFoodImage = mutableListOf<String>()

        for (i in 1 until listOfOrderItem.size) {
            listOfOrderItem[i].foodNames?.firstOrNull()?.let { foodName ->
                buyAgainFoodName.add(foodName)
                listOfOrderItem[i].foodPrices?.firstOrNull()?.let { foodPrice ->
                    buyAgainFoodPrice.add("₹$foodPrice")
                    listOfOrderItem[i].foodImages?.firstOrNull()?.let { foodImage ->
                        buyAgainFoodImage.add(foodImage)

                        // Initialize RecyclerView with the correct adapter and listener
                        val rv = binding.BuyAgainRecyclerView
                        rv.layoutManager = LinearLayoutManager(requireContext())

                        buyAgainAdapter = BuyAgainAdapter(
                            buyAgainFoodName,
                            buyAgainFoodPrice,
                            buyAgainFoodImage,
                            requireContext()
                        )
                        rv.adapter = buyAgainAdapter
                    }
                }
            }
        }
    }

    // Add the notification function here
    private fun showThankYouNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "order_confirmation_channel"

        // Create a notification channel (required for API 26+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Order Confirmation", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // Create an intent for when the user taps the notification
        val intent = Intent(context, MainActivity::class.java) // Replace with your activity
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)


        // Build the notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.notification_icon) // Replace with your notification icon
            .setContentTitle("Thank You for Your Order!")
            .setContentText("We appreciate your business. Please continue to use our service!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Dismiss the notification when tapped
            .build()

        // Show the notification
        notificationManager.notify(1001, notification) // Unique ID for the notification
    }
}