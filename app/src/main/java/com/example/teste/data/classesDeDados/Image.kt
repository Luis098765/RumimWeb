package com.example.teste.data.classesDeDados

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Image(
    @PrimaryKey(autoGenerate = false)
    val numeroDoAnimal: String,
    val image: ByteArray
)