package com.example.taller3_movil.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taller3_movil.R
import com.example.taller3_movil.data.User
import com.example.taller3_movil.UserAdapter

class UsersListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val usersList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users_list)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the user adapter with the list of users
        userAdapter = UserAdapter(this, usersList) { user ->
            // Call the activity to show the user's location
            val intent = Intent(this, UserLocationActivity::class.java)
            // Create a location string with latitude and longitude
            val location = "${user.lat},${user.lon}"
            intent.putExtra("userLocation", location)
            startActivity(intent)
        }

        recyclerView.adapter = userAdapter

        // Load available users
        loadAvailableUsers()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadAvailableUsers() {
        // Simulate adding users from Firebase or other sources
        usersList.add(
            User(
            name = "Juan",
            lastName = "Pérez",
            email = "juan@example.com",
            identification = "123456789",
            lat = 10.123,
            lon = 20.456,
            imageUrl = "https://example.com/juan.jpg",
            isAvailable = true
        )
        )
        usersList.add(
            User(
            name = "Maria",
            lastName = "Gomez",
            email = "maria@example.com",
            identification = "987654321",
            lat = 10.789,
            lon = 20.123,
            imageUrl = "https://example.com/maria.jpg",
            isAvailable = false
        )
        )
        usersList.add(
            User(
            name = "Carlos",
            lastName = "Lopez",
            email = "carlos@example.com",
            identification = "192837465",
            lat = 11.456,
            lon = 19.789,
            imageUrl = "https://example.com/carlos.jpg",
            isAvailable = true
        )
        )

        // Notify the adapter that the data has been updated
        userAdapter.notifyDataSetChanged()
    }
}
