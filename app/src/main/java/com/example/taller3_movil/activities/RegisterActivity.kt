package com.example.taller3_movil.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taller3_movil.Constants.REQUEST_LOCATION_PERMISSION
import com.example.taller3_movil.Constants.REQUEST_IMAGE_PICK
import com.example.taller3_movil.R
import com.example.taller3_movil.data.User
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var selectedImageUri: Uri? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        auth = FirebaseAuth.getInstance()

        val selectImageButton = findViewById<Button>(R.id.btn_select_image)
        selectImageButton.setOnClickListener {
            requestStoragePermissionAndOpenGallery()
        }

        val registerButton = findViewById<Button>(R.id.btn_register)
        registerButton.setOnClickListener {
            checkLocationPermissionAndRegisterUser()
        }
    }

    private fun requestStoragePermissionAndOpenGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_IMAGE_PICK)
        } else {
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    private fun checkLocationPermissionAndRegisterUser() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        } else {
            getLocationAndRegisterUser()
        }
    }

    private fun getLocationAndRegisterUser() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                registerUser(latitude, longitude)
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(latitude: Double, longitude: Double) {
        val name = findViewById<EditText>(R.id.et_name).text.toString()
        val lastName = findViewById<EditText>(R.id.et_last_name).text.toString()
        val email = findViewById<EditText>(R.id.et_email).text.toString()
        val password = findViewById<EditText>(R.id.et_password).text.toString()
        val identification = findViewById<EditText>(R.id.et_identification_number).text.toString()

        if (name.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || identification.isEmpty() || selectedImageUri == null) {
            Toast.makeText(this, "Por favor, complete todos los campos y seleccione una imagen", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    uploadProfileImage(userId, name, lastName, email, identification, latitude, longitude)
                }
            } else {
                Toast.makeText(this, "Error al registrar el usuario", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadProfileImage(userId: String, name: String, lastName: String, email: String, identification: String, latitude: Double, longitude: Double) {
        if (selectedImageUri != null) {
            val storageReference = FirebaseStorage.getInstance().reference.child("profile_images/$userId.jpg")

            storageReference.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    // After successful upload, retrieve the download URL
                    storageReference.downloadUrl.addOnSuccessListener { uri ->
                        saveUserToDatabase(userId, name, lastName, email, identification, latitude, longitude, uri.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error al subir la imagen: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }


    private fun saveUserToDatabase(userId: String, name: String, lastName: String, email: String, identification: String, latitude: Double, longitude: Double, profileImageUrl: String) {
        val user = User(name, lastName, email, identification, latitude, longitude, profileImageUrl, true)
        val userReference = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        userReference.setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error al registrar el usuario", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocationAndRegisterUser()
                } else {
                    Toast.makeText(this, "Se necesitan permisos de ubicación", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_IMAGE_PICK -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(this, "Se necesitan permisos de almacenamiento", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            findViewById<ImageView>(R.id.imageView).setImageURI(selectedImageUri)
        }
    }
}
