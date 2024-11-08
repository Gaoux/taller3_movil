package com.example.taller3_movil

import com.example.taller3_movil.activities.UserLocationActivity
import android.content.Context
import android.content.Intent
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
    private val onLocationClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.nameTextView.text = "${user.name} ${user.lastName}"
        holder.emailTextView.text = user.email

        // Load user image using Glide
        Glide.with(context).load(user.imageUrl).into(holder.imageView)

        // Set click listener for the "View Location" button
        holder.locationButton.setOnClickListener {
            // When the "View Location" button is clicked, start com.example.taller3_movil.activities.UserLocationActivity
            val intent = Intent(context, UserLocationActivity::class.java)
            intent.putExtra("user", user)  // Pass the user object to the activity
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.userName)
        val emailTextView: TextView = itemView.findViewById(R.id.userEmail)
        val imageView: ImageView = itemView.findViewById(R.id.userImage)
        val locationButton: Button = itemView.findViewById(R.id.locationButton)
    }
}
