package com.example.teste.data.classesDeDados

import androidx.room.Embedded
import androidx.room.Relation

data class AnimalWithRegisters(
    @Embedded val animal: Animal,
    @Relation(
        parentColumn = "numeroIdentificacao",
        entityColumn = "numeroDoAnimal"
    )
    val registers: List<Register>
)