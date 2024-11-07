package com.example.taller3_movil

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import com.example.taller3_movil.data.User

class UserAvailabilityService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    fun userStatusChanged(user: User) {
        // Notificar cambios de estado
        if (user.isAvailable) {
            Toast.makeText(this, "${user.name} está disponible", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "${user.name} ya no está disponible", Toast.LENGTH_SHORT).show()
        }
    }
}
