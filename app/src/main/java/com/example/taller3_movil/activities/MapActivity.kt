package com.example.taller3_movil.activities

import AvailabilityDialogFragment
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.taller3_movil.Constants.REQUEST_LOCATION_PERMISSION
import com.example.taller3_movil.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.json.JSONObject
import java.io.InputStream
import java.io.InputStreamReader

enum class ActionType {
    LOGOUT,
    AVAILABILITY,
    USERS_LIST
}

class MapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var userLocation: Location
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configurar OSM
        Configuration.getInstance().userAgentValue = packageName

        setContentView(R.layout.activity_map)

        // Inicializar el mapa
        mapView = findViewById(R.id.mapview)
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        mapView.controller.setZoom(14)

        // Inicializar BottomNavigationView
        bottomNav = findViewById(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    handleAction(ActionType.LOGOUT)
                    true
                }
                R.id.action_availability -> {
                    handleAction(ActionType.AVAILABILITY)
                    true
                }
                R.id.action_users_list -> {
                    handleAction(ActionType.USERS_LIST)
                    true
                }
                else -> false
            }
        }

        // Obtener la ubicación del usuario
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Verificar permisos y obtener ubicación
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Si los permisos no están concedidos, solicitarlos
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            // Si ya tenemos permisos, obtener la ubicación
            getLocation()
        }

        // Cargar el archivo JSON de las localizaciones
        val json = loadJSONFromAsset()
        if (json != null) {
            val locationsArray = json.getJSONArray("locationsArray")
            // Mostrar hasta 5 localizaciones
            for (i in 0 until Math.min(locationsArray.length(), 5)) {
                val location = locationsArray.getJSONObject(i)
                val latitude = location.getDouble("latitude")
                val longitude = location.getDouble("longitude")
                val name = location.getString("name")

                // Crear un marcador para cada localización
                val marker = Marker(mapView)
                marker.position = org.osmdroid.util.GeoPoint(latitude, longitude)
                marker.title = name
                mapView.overlays.add(marker)
            }
        } else {
            Toast.makeText(this, "Error al cargar los puntos de interés", Toast.LENGTH_SHORT).show()
        }
    }

    // Método para obtener la ubicación del usuario
    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Solicitar permisos si no se han concedido
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                userLocation = location
                // Centrar el mapa en la ubicación del usuario
                mapView.controller.setCenter(org.osmdroid.util.GeoPoint(userLocation.latitude, userLocation.longitude))

                // Crear un marcador para la ubicación del usuario
                val userMarker = Marker(mapView)
                userMarker.position = org.osmdroid.util.GeoPoint(userLocation.latitude, userLocation.longitude)
                userMarker.title = "Tu Ubicación"
                mapView.overlays.add(userMarker)
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Cargar el archivo JSON desde assets
    private fun loadJSONFromAsset(): JSONObject? {
        val json: String
        try {
            val inputStream: InputStream = assets.open("locations.json")
            val inputStreamReader = InputStreamReader(inputStream)
            val buffer = StringBuilder()
            var data = inputStreamReader.read()
            while (data != -1) {
                buffer.append(data.toChar())
                data = inputStreamReader.read()
            }
            json = buffer.toString()
            return JSONObject(json)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // Método para manejar la respuesta de la solicitud de permisos
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso concedido, obtener ubicación
                    getLocation()
                } else {
                    // Permiso denegado
                    Toast.makeText(this, "Se necesitan permisos de ubicación", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Centralizado handler para las acciones
    private fun handleAction(action: ActionType) {
        when (action) {
            ActionType.LOGOUT -> {
                Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()

                // Lógica para cerrar sesión (limpiar sesión, redirigir a LoginActivity)
                // Aquí podrías limpiar la sesión del usuario, como borrar tokens, credenciales guardadas, etc.

                val intent = Intent(this, LoginActivity::class.java)
                this.startActivity(intent)
                (this as? AppCompatActivity)?.finish()  // Cierra la actividad actual
            }
            ActionType.AVAILABILITY -> {
                val availabilityDialog = AvailabilityDialogFragment()
                availabilityDialog.setListener(object : AvailabilityDialogFragment.AvailabilityListener {
                    override fun onStatusSelected(status: Boolean) {
                        // Show a Toast to confirm the selected status
//                        Toast.makeText(this@MapActivity, "Estado: $status", Toast.LENGTH_SHORT).show()

                        // Get the current user's ID from Firebase
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        if (userId != null) {
                            // Create a reference to the user's data in Firebase
                            val userReference = FirebaseDatabase.getInstance().reference.child("users").child(userId)

                            // Update the availability status in Firebase
                            userReference.child("isAvailable").setValue(status)
                                .addOnSuccessListener {
                                    Toast.makeText(this@MapActivity, "Estado de disponibilidad actualizado", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this@MapActivity, "Error al actualizar el estado de disponibilidad", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this@MapActivity, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
                availabilityDialog.show(supportFragmentManager, "AvailabilityDialog")
            }

            ActionType.USERS_LIST -> {
                val intent = Intent(this, UsersListActivity::class.java)
                this.startActivity(intent)
            }
        }
    }
}
