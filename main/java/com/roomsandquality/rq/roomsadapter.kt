package com.roomsandquality.rq

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class roomsadapter(val user: ArrayList<Upload>,val clickListener: (Upload) -> Unit): RecyclerView.Adapter<roomsadapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): roomsadapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.roomslayout, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return user.size
    }

    override fun onBindViewHolder(holder: roomsadapter.ViewHolder, position: Int) {
        holder.bindItems(user[position],clickListener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(user: Upload,clickListener: (Upload) -> Unit) {
            val textViewName = itemView.findViewById(R.id.textViewName) as TextView
            val addrName = itemView.findViewById(R.id.Address) as TextView
            val priceName = itemView.findViewById(R.id.price) as TextView
            val imageViewName = itemView.findViewById(R.id.imageView) as ImageView
            textViewName.text = user.Nameofroom
            addrName.text = user.Address
            priceName.text = user.Price
            Picasso.get().load(user.Imgurl1).into(imageViewName)
            itemView.setOnClickListener {
                clickListener(user)
            }
        }

    }
}
