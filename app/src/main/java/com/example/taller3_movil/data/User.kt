package com.example.taller3_movil.data

data class User(
    val name: String = "",
    val lastName: String = "",
    val email: String = "",
    val identification: String = "",
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val imageUrl: String = "",
    val isAvailable: Boolean = true
)

