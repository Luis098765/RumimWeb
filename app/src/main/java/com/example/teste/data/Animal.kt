package com.example.teste.data

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.InputStream

@Entity(tableName = "animal_table")
data class Animal(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val numeroIdentificacao: String,
    val nascimento: String,
    val raca: String,
    val sexo: String,
    val image: ByteArray?,
    val categoria: String,
    val status: String,
    val pesoNascimento: String
)