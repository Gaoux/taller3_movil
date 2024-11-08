package com.example.taller3_movil.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.widget.Toast
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taller3_movil.Constants.REQUEST_LOCATION_PERMISSION
import com.example.taller3_movil.R
import com.example.taller3_movil.data.User
import com.google.firebase.database.*
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

class UserLocationActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private var myLocationOverlay: MyLocationNewOverlay? = null
    private var mapController: IMapController? = null
    private lateinit var database: DatabaseReference
    private lateinit var distanceTextView: TextView
    private var trackingUserEmail: String? = null // Email of the user to track
    private lateinit var currentLocation: GeoPoint // Declare the lateinit var for current location
    private var handler: Handler = Handler(Looper.getMainLooper()) // Handler to post periodic tasks
    private var marker: Marker? = null // Store reference to the marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_location)

        // Load OSMDroid configuration for tile caching
        val context = applicationContext
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))

        distanceTextView = findViewById(R.id.distanceTextView)
        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().reference

        // Retrieve the tracking user email from intent extras
        val user: User? = intent.getSerializableExtra("user") as? User
        trackingUserEmail = user!!.email
        Log.d("UserLocationActivity", "Tracking user email: $trackingUserEmail")

        // Initialize the map view
        mapView = findViewById(R.id.mapView)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        // Initialize map controller
        mapController = mapView.controller
        mapController?.setZoom(14) // Set default zoom level

        // Check location permission and set up location overlay
        if (checkLocationPermission()) {
            setupLocationOverlay()
        } else {
            requestLocationPermission()
        }

        // Fetch the tracking user's location from Firebase
        fetchTrackedUserLocation()

        // Listen for location changes in the database
        listenForLocationChanges()

        // Start periodic location check every 10 seconds
        startLocationCheck()
    }

    // Initialize the location overlay to display device's current location on the map
    private fun setupLocationOverlay() {
        myLocationOverlay = MyLocationNewOverlay(mapView)
        myLocationOverlay?.enableMyLocation()

        // Set up location listener to update the currentLocation variable
        myLocationOverlay?.runOnFirstFix {
            val initialLocation = myLocationOverlay?.myLocation
            if (initialLocation != null) {
                currentLocation = initialLocation
                runOnUiThread {
                    Log.d("UserLocationActivity", "Current location: ${currentLocation.latitude}, ${currentLocation.longitude}")
                }
            }
        }

        // Listen for location updates and store the current location
        myLocationOverlay?.enableFollowLocation()
        mapView.overlays.add(myLocationOverlay)
    }

    // Fetch the tracked user's location from Firebase
    private fun fetchTrackedUserLocation() {
        trackingUserEmail?.let { email ->
            // Query Firebase for the user with the matching email
            database.child("users").orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        Log.d("UserLocationActivity", "Data snapshot retrieved for email: $email")
                        for (snapshot in dataSnapshot.children) {
                            val lat = snapshot.child("lat").getValue(Double::class.java)
                            val lon = snapshot.child("lon").getValue(Double::class.java)

                            if (lat != null && lon != null) {
                                // Create GeoPoint for the tracked user's location
                                val geoPoint = GeoPoint(lat, lon)
                                Log.d("UserLocationActivity", "User location: lat = $lat, lon = $lon")

                                // Add a marker at the tracked user's location
                                if (marker == null) {
                                    marker = addMarker(geoPoint, "Tracked User Location")
                                } else {
                                    marker?.position = geoPoint // Update existing marker position
                                }

                                // Center the map on the user's location
                                runOnUiThread {
                                    mapController?.setCenter(geoPoint)
                                    if (::currentLocation.isInitialized) {
                                        val distance = currentLocation.distanceToAsDouble(geoPoint) / 1000.0
                                        distanceTextView.text = "Distance: %.2f km".format(distance)
                                    }
                                }
                            } else {
                                Log.d("UserLocationActivity", "Location data missing for user")
                                Toast.makeText(
                                    this@UserLocationActivity,
                                    "Location data missing for user",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("UserLocationActivity", "Failed to retrieve user location: ${databaseError.message}")
                        Toast.makeText(
                            this@UserLocationActivity,
                            "Failed to retrieve user location: ${databaseError.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }

    // Listen for location changes in Firebase and update the marker
    private fun listenForLocationChanges() {
        trackingUserEmail?.let { email ->
            // Listen for any changes in the location of the user with the matching email
            database.child("users").orderByChild("email").equalTo(email)
                .addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        // Handle user added, if necessary
                    }

                    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                        // Handle location change
                        val lat = snapshot.child("lat").getValue(Double::class.java)
                        val lon = snapshot.child("lon").getValue(Double::class.java)

                        if (lat != null && lon != null) {
                            val geoPoint = GeoPoint(lat, lon)

                            // Update the marker position
                            runOnUiThread {
                                marker?.position = geoPoint
                                mapController?.setCenter(geoPoint)
                                if (::currentLocation.isInitialized) {
                                    val distance = currentLocation.distanceToAsDouble(geoPoint) / 1000.0
                                    distanceTextView.text = "Distance: %.2f km".format(distance)
                                }
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
                        Log.e("UserLocationActivity", "Error reading data: ${error.message}")
                    }
                })
        }
    }

    // Add a marker on the map at the specified location
    private fun addMarker(location: GeoPoint, title: String): Marker {
        Log.d("UserLocationActivity", "Adding marker at: $location")
        val marker = Marker(mapView)
        marker.position = location
        marker.title = title
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(marker)
        return marker
    }

    // Start checking for location changes every 10 seconds
    private fun startLocationCheck() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Check if the current location is initialized
                if (::currentLocation.isInitialized) {
                    myLocationOverlay?.myLocation?.let {
                        val updatedLocation = it
                        // Check if the location has changed
                        if (currentLocation.latitude != updatedLocation.latitude ||
                            currentLocation.longitude != updatedLocation.longitude) {
                            currentLocation = updatedLocation
                            // Update the distance to the tracked user
                            trackingUserEmail?.let { email ->
                                fetchTrackedUserLocation()
                            }
                        }
                    }
                }
                // Repeat this task after 10 seconds
                handler.postDelayed(this, 1000)
            }
        }, 100)
    }

    // Check if location permission is granted
    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Request location permission if not granted
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }

    // Handle the result of location permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setupLocationOverlay()
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

}
