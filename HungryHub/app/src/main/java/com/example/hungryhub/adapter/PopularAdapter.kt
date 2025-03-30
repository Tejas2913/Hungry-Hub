package com.example.hungryhub.adapter

import android.content.Context
import android.content.Intent
import android.media.Image
import android.provider.MediaStore.Images
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hungryhub.DetailsActivity
import com.example.hungryhub.databinding.PopularItemBinding

class PopularAdapter ( private val Items:List<String>,private val price:List<String>,private val image:List<Int>, private val requireContext: Context) : RecyclerView.Adapter<PopularAdapter.PopularViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularViewHolder {
    return PopularViewHolder(PopularItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }


    override fun onBindViewHolder(holder: PopularViewHolder, position: Int) {
val item = Items[position]
        val images = image[position]
        val price = price[position]
        holder.bind(item,price,images)

        holder.itemView.setOnClickListener {
            val intent = Intent( requireContext, DetailsActivity::class.java)
            intent.putExtra("MenuItemName",item)
            intent.putExtra("MenuItemImage",images)
            requireContext.startActivity(intent)
        }
    }
    override fun getItemCount(): Int {
        return Items.size
    }
    class PopularViewHolder (private val binding:PopularItemBinding) : RecyclerView.ViewHolder(binding.root){
        private val imagesView = binding.imageFood
        fun bind(item: String,price: String, images: Int) {
            binding.foodNamePopular.text = item
            binding.PricePopular.text = price
            imagesView.setImageResource(images)
        }

    }
}