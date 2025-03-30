package com.example.hungryhub

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hungryhub.databinding.ActivityPayOutBinding
import com.example.hungryhub.model.OrderDetails
import com.example.hungryhub.service.TwilioNotificationService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PayOutActivity : AppCompatActivity() {
    lateinit var binding: ActivityPayOutBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var name : String
    private lateinit var address : String
    private lateinit var phone : String
    private lateinit var totalAmount : String
    private lateinit var foodItemName : ArrayList<String>
    private lateinit var foodItemPrice : ArrayList<String>
    private lateinit var foodItemImage : ArrayList<String>
    private lateinit var foodItemDescription : ArrayList<String>
    private lateinit var foodItemNutrient : ArrayList<String>
    private lateinit var foodItemQuantities : ArrayList<Int>
    private lateinit var databaseReference: DatabaseReference
    private lateinit var userId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayOutBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference()
        setUserData()

        val intent = intent
        foodItemName= intent.getStringArrayListExtra("FoodItemName") as ArrayList<String>
        foodItemPrice= intent.getStringArrayListExtra("FoodItemPrice") as ArrayList<String>
        foodItemImage= intent.getStringArrayListExtra("FoodItemImage") as ArrayList<String>
        foodItemDescription= intent.getStringArrayListExtra("FoodItemDescription") as ArrayList<String>
        foodItemNutrient= intent.getStringArrayListExtra("FoodItemNutrient") as ArrayList<String>
        foodItemQuantities= intent.getIntegerArrayListExtra("FoodItemQuantities") as ArrayList<Int>

        totalAmount = "₹"+calculateTotalAmount().toString()
        //binding.totalAmount.isEnabled = false
        binding.totalAmount.setText(totalAmount)
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.PlaceMyOrder.setOnClickListener {

            name = binding.name.text.toString().trim()
            address= binding.address.text.toString().trim()
            phone = binding.phone.text.toString().trim()

            if(name.isBlank()&&address.isBlank()&&phone.isBlank()){
                Toast.makeText(this,"Please enter all fields 😒",Toast.LENGTH_SHORT).show()
            }else{
                placeOrder()
            }
        }
    }

    private fun placeOrder() {
        userId = auth.currentUser?.uid?:""
        val time = System.currentTimeMillis()
        val itemPushKey = databaseReference.child("OrderDetails").push().key
        val orderDetails = OrderDetails(userId,name,foodItemName,foodItemPrice,foodItemImage,foodItemQuantities,address,totalAmount,phone,time,itemPushKey,false,false)

        val orderReference = databaseReference.child("OrderDetails").child(itemPushKey!!)
        orderReference.setValue(orderDetails).addOnSuccessListener {
            val bottomSheetDialog = CongratsBottomSheet()
            bottomSheetDialog.show(supportFragmentManager,"Test")
            removeItemFromCart()
            addOrderToHistory(orderDetails)
            // Send SMS notification to admin
            val adminPhoneNumber = "+91 9066660386" // Replace with admin's phone number
            val messageBody = "New order placed by $name for $foodItemName. Check admin panel."

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    TwilioNotificationService.sendSms(adminPhoneNumber, messageBody)
                    Log.d("OrderNotification", "Notification sent to admin")
                } catch (e: Exception) {
                    Log.e("OrderNotification", "Failed to send notification to admin: ${e.message}")
                }
            }
        }
            .addOnFailureListener {
                Toast.makeText(this,"Failed to place order 😥",Toast.LENGTH_SHORT).show()

            }
    }
    private fun addOrderToHistory(orderDetails: OrderDetails){
        databaseReference.child("user").child(userId).child("BuyHistory")
            .child(orderDetails.itemPushKey!!)
            .setValue(orderDetails).addOnSuccessListener {

            }
    }

    private fun removeItemFromCart() {
        val cartItemsReference = databaseReference.child("user").child(userId).child("CartItems")
        cartItemsReference.removeValue()
    }

    private fun calculateTotalAmount(): Int {
        var totalAmount = 0
        for (i in 0 until foodItemPrice.size) {
            var price = foodItemPrice[i]
            val lastChar = price.last()
            val priceIntValue = if (lastChar == '₹') {
                price.dropLast(1).toInt()
            } else {
                price.toInt()
            }
            val quantity = foodItemQuantities[i]
            totalAmount += priceIntValue * quantity
        }
        return totalAmount
    }

    private fun setUserData() {
        val user = auth.currentUser
        if(user!=null){
            val userId = user.uid
            val userReference = databaseReference.child("user").child(userId)

            userReference.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val names = snapshot.child("name").getValue(String::class.java)?:""
                        val addresses = snapshot.child("address").getValue(String::class.java)?:""
                        val phones = snapshot.child("phone").getValue(String::class.java)?:""

                        binding.apply {
                            name.setText(names)
                            address.setText(addresses)
                            phone.setText(phones)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }
}