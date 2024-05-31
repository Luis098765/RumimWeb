package com.example.teste.data.classesDeDados

import androidx.room.Embedded
import androidx.room.Relation

data class UserWithAnimals (
    @Embedded val user: User,
    @Relation(
        parentColumn = "email",
        entityColumn = "userEmail"
    )
    val animals: List<Animal>
)