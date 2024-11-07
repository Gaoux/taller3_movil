package com.example.taller3_movil

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.taller3_movil.data.User

class UserAdapter(
    private val context: Context,
    private val userList: List<User>,
    private val onItemClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.user_name)
        private val lastNameTextView: TextView = itemView.findViewById(R.id.user_last_name)
        private val emailTextView: TextView = itemView.findViewById(R.id.user_email)
        private val availabilityTextView: TextView = itemView.findViewById(R.id.user_availability)
        private val imageView: ImageView = itemView.findViewById(R.id.user_image)
        private val locationButton: Button = itemView.findViewById(R.id.button_location)

        fun bind(user: User) {
            nameTextView.text = user.name
            lastNameTextView.text = user.lastName
            emailTextView.text = user.email
            availabilityTextView.text = if (user.isAvailable) "Available" else "Not Available"

            // Load the user's image using Glide (make sure you add Glide as a dependency)
            Glide.with(context).load(user.imageUrl).into(imageView)

            // Set up the location button to view the user's location on the map
            locationButton.setOnClickListener {
                onItemClick(user)
            }
        }
    }
}
