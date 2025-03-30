package com.example.hungryhub

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.hungryhub.databinding.ActivityDetailsBinding
import com.example.hungryhub.model.CartItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding
    private var foodName: String?=null
    private var foodImage: String?=null
    private var foodDescription: String?=null
    private var foodNutrients: String?=null
    private var foodPrice: String?=null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        foodName = intent.getStringExtra("MenuItemName")
        foodDescription = intent.getStringExtra("MenuItemDescription")
        foodNutrients = intent.getStringExtra("MenuItemNutrients")
        foodPrice = intent.getStringExtra("MenuItemPrice")
        foodImage = intent.getStringExtra("MenuItemImage")

        with(binding){
            detailFoodName.text = foodName
            detailDescription.text = foodDescription
            detailNutrients.text = foodNutrients
            Glide.with(this@DetailsActivity).load(Uri.parse(foodImage)).into(detailFoodImage)

        }

        binding.imageButton.setOnClickListener {
            finish()
        }
        binding.addItemButton.setOnClickListener {
            addItemToCart()
        }
    }

    private fun addItemToCart() {
        val database = FirebaseDatabase.getInstance().reference
        val userId = auth.currentUser?.uid ?: ""
        val cartRef = database.child("user").child(userId).child("CartItems")

        // Create a new CartItems object without the unique key
        val cartItem = CartItems(
            foodName = foodName,
            foodPrice = foodPrice,
            foodDescription = foodDescription,
            foodImage = foodImage,
            foodQuantity = 1,
            foodNutrient = foodNutrients // If you have this value
        )

        // Generate a unique key
        val uniqueKey = cartRef.push().key ?: return // Generate a unique key

        // Save the item with the unique key
        cartRef.child(uniqueKey).setValue(cartItem.copy(key = uniqueKey)).addOnSuccessListener {
            Toast.makeText(this, "Item added to the cart successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Item not added", Toast.LENGTH_SHORT).show()
        }
    }


}