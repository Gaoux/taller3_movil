package com.example.taller3_movil.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.example.taller3_movil.data.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserAvailabilityService : Service() {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("users")

    override fun onBind(intent: Intent?): IBinder? {
        // No binding necessary for this service
        return null
    }

    override fun onCreate() {
        super.onCreate()
        // Start listening for changes in availability
        listenForUserAvailabilityChanges()
    }

    private fun listenForUserAvailabilityChanges() {
        // Listening for any changes in the users' availability status
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    // Manually get the values from the snapshot
                    val isAvailable = userSnapshot.child("isAvailable").getValue(Boolean::class.java) ?: false // Default to false if not found
                    val name = userSnapshot.child("name").getValue(String::class.java) ?: "Unknown"
                    // Show a toast based on the availability status
                    if (isAvailable) {
                        Toast.makeText(this@UserAvailabilityService, "$name está disponible", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@UserAvailabilityService, "$name está desconectado", Toast.LENGTH_SHORT).show()
                    }
                }
            }



            override fun onCancelled(error: DatabaseError) {
                // Handle database read failure
                Toast.makeText(this@UserAvailabilityService, "Error al leer los datos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up the listener when the service is destroyed (optional)
        database.removeEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
