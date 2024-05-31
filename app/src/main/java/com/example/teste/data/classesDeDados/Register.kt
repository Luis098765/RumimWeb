package com.example.teste.data.classesDeDados

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Register (
    @PrimaryKey(autoGenerate = false)
    val nome: String,
    val data: String,
    val valor: String?,
    val descricao: String?,
    val numeroDoAnimal: String
)