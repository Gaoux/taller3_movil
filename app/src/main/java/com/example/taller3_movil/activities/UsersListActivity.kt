package com.example.taller3_movil.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taller3_movil.R
import com.example.taller3_movil.data.User
import com.example.taller3_movil.UserAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class UsersListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val usersList = mutableListOf<User>()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users_list)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the user adapter with the list of users
        userAdapter = UserAdapter(this, usersList) { user ->
            // When button clicked, navigate to com.example.taller3_movil.activities.UserLocationActivity
            val intent = Intent(this, UserLocationActivity::class.java)
            // Create a location string with latitude and longitude
            val location = "${user.lat},${user.lon}"
            intent.putExtra("userLocation", location)
            startActivity(intent)
        }

        recyclerView.adapter = userAdapter

        // Load available users from Firebase Realtime Database
        loadAvailableUsers()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadAvailableUsers() {
        // Listen to the "users" node in Firebase Database
        database.orderByChild("available").equalTo(true) // Only fetch available users
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    usersList.clear()  // Clear the current list

                    for (userSnapshot in snapshot.children) {
                        // Convert snapshot to User object
                        val user = userSnapshot.getValue(User::class.java)
                        user?.let {
                            usersList.add(it)  // Add user to the list
                        }
                    }

                    // Notify the adapter that the data has been updated
                    userAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@UsersListActivity, "Error al cargar los usuarios: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
