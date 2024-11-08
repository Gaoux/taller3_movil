package com.example.taller3_movil.data

import java.io.Serializable

data class User(
    val name: String = "",
    val lastName: String = "",
    val email: String = "",
    val identification: String = "",
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val imageUrl: String = "",
    val available: Boolean = true
) : Serializable

