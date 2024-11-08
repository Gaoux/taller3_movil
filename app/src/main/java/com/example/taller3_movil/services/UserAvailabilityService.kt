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
import com.google.firebase.database.ChildEventListener

class UserAvailabilityService : Service() {

    private val database: DatabaseReference = FirebaseDatabase.getInstance()
        .reference.child("users")

    private var previousAvailability: Boolean? = null

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
        // Listen to changes in the "users" node (specific user updates)
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle user added, if necessary
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Check if the "available" field has changed
                val available = snapshot.child("available").getValue(Boolean::class.java)

                if (available != null && available != previousAvailability) {
                    previousAvailability = available  // Update the previous availability

                    val name = snapshot.child("name").getValue(String::class.java) ?: "Unknown"

                    // Show a toast based on the availability status of the user that changed
                    if (available) {
                        Toast.makeText(this@UserAvailabilityService, "$name está disponible", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@UserAvailabilityService, "$name está desconectado", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Handle user removed, if necessary
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle user moved, if necessary
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
        database.removeEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
