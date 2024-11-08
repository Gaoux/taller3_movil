package com.example.taller3_movil.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taller3_movil.Constants.REQUEST_LOCATION_PERMISSION
import com.example.taller3_movil.R
import com.example.taller3_movil.data.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.osmdroid.api.IMapController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.util.GeoPoint
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class UserLocationActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var user: User
    private lateinit var database: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference
    private lateinit var distanceTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_location)

        distanceTextView = findViewById(R.id.distanceTextView)
        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance()
        usersRef = database.reference.child("users")

        // Get the email from the intent (or other source)
        @Suppress("DEPRECATION")
        val userToTrack = intent.getSerializableExtra("user") as? User
        val emailToSearch = userToTrack?.email

        Log.d("emailToSearch", "emailToSearch: $emailToSearch")

        // Query to find the user by email
        val query = usersRef.orderByChild("email").equalTo(emailToSearch)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        // Retrieve the user ID (key)
                        val userId = userSnapshot.key
                        if (userId != null) {
                            // Get the user details using the user ID
                            user = userSnapshot.getValue(User::class.java) ?: return
                            // Initialize the map
                            initMap(user)

                            // Listen for real-time location changes
                            val userLocationRef = database.getReference("users/$userId/location")
                            userLocationRef.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(locationSnapshot: DataSnapshot) {
                                    val latitude = locationSnapshot.child("lat").getValue(Double::class.java)
                                    val longitude = locationSnapshot.child("lon").getValue(Double::class.java)

                                    if (latitude != null && longitude != null) {
                                        updateUserLocationOnMap(latitude, longitude)
                                        Log.d("Update location", "Location updated")
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@UserLocationActivity, "Error fetching location", Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                    }
                } else {
                    Log.d("Firebase", "User not found!")
                    Toast.makeText(this@UserLocationActivity, "User not found", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching user: ${error.message}")
                Toast.makeText(this@UserLocationActivity, "Error fetching user", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Initialize the map with user's initial location
    private fun initMap(user: User) {
        mapView = findViewById(R.id.mapView)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        // Log map initialization
        Log.d("UserLocationActivity", "Initializing map with user's location: ${user.lat}, ${user.lon}")

        // Set the map to the user's location
        val userLocation = GeoPoint(user.lat, user.lon)
        val mapController: IMapController = mapView.controller
        mapController.setCenter(userLocation)
        mapController.setZoom(18)

        // Add a marker for the user's location
        val marker = Marker(mapView)
        marker.position = userLocation
        marker.title = "${user.name} ${user.lastName}"
        mapView.overlays.add(marker)

        // Add location overlay to show user's real-time location (optional)
        val myLocationOverlay = MyLocationNewOverlay(mapView)
        if (checkLocationPermission()) {
            myLocationOverlay.enableMyLocation()
        } else {
            requestLocationPermission()
        }
        mapView.overlays.add(myLocationOverlay)
    }

    // Update the user's location on the map as it changes
    private fun updateUserLocationOnMap(latitude: Double, longitude: Double) {
        val userLocation = GeoPoint(latitude, longitude)
        val mapController: IMapController = mapView.controller
        mapController.setCenter(userLocation)

        // Update the marker position
        val marker = Marker(mapView)
        marker.position = userLocation
        mapView.overlays.clear() // Remove old markers
        mapView.overlays.add(marker)

        // Calculate the distance (straight line)
        val distance = calculateDistance(user.lat, user.lon, latitude, longitude)
        // Update the distance TextView on the main thread
        runOnUiThread {
            distanceTextView.text = "Distance: ${String.format("%.2f", distance)} meters"
        }
    }

    // Calculate distance in meters between two GeoPoints
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371 // km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = earthRadius * c * 1000 // Distance in meters

        // Log calculated distance
        Log.d("UserLocationActivity", "Calculated distance: $distance meters")
        return distance
    }

    // Check if the app has location permission
    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // Request location permission
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("UserLocationActivity", "Location permission granted.")
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
