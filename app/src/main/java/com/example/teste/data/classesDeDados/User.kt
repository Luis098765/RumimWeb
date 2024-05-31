package com.example.teste.data.classesDeDados

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.teste.InformacoesPropriedade

@Entity
data class User(
    @PrimaryKey(autoGenerate = false)
    val email: String,
    val nomePropriedade: String,
    var sincronizacaoNecessaria: Boolean
)