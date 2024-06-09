package com.example.teste.data.classesDeDados

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Animal (
    @PrimaryKey(autoGenerate = false)
    val numeroIdentificacao: String,
    val nascimento: String,
    val raca: String,
    val sexo: String,
    val categoria: String,
    var status: String,
    val pesoNascimento: String,
    var pesoDesmame: String?,
    var dataDesmame: String?,
    val userEmail: String
)