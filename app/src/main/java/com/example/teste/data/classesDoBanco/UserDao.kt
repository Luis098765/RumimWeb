package com.example.teste.data.classesDoBanco

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.teste.data.classesDeDados.Animal
import com.example.teste.data.classesDeDados.AnimalWithRegisters
import com.example.teste.data.classesDeDados.Register
import com.example.teste.data.classesDeDados.User
import com.example.teste.data.classesDeDados.UserWithAnimals
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimal(animal: Animal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegister(register: Register)

    @Query("SELECT * FROM User")
    suspend fun getAllUsers(): List<User>

    @Transaction
    @Query("SELECT * FROM User WHERE email = :userEmail")
    suspend fun getUserWithAnimals(userEmail: String): List<UserWithAnimals>

    @Transaction
    @Query("SELECT * FROM Animal WHERE numeroIdentificacao = :animalNumber")
    suspend fun getAnimalWithRegisters(animalNumber: String): List<AnimalWithRegisters>

    @RequiresApi(Build.VERSION_CODES.O)
    @Transaction
    suspend fun getPesoAtualFromAnimal(animalNumber: String): String {
        val animal = getAnimalWithRegisters(animalNumber)
        var pesoAtual: String = if (animal.first().registers.isNullOrEmpty()) {
            if (animal.first().animal.pesoDesmame.isNullOrEmpty()) {
                animal.first().animal.pesoNascimento.toString()
            } else {
                animal.first().animal.pesoDesmame.toString()
            }
        } else {
            val registrosFiltrados = animal.first().registers.filter { it.nome.contains("Pesagem") }

            val registrosOrdenados = registrosFiltrados.sortedByDescending {
                LocalDate.parse(
                    it.data,
                    DateTimeFormatter.ofPattern("dd-MM-yyyy")
                )
            }

            registrosOrdenados.first().valor.toString()
        }

        return pesoAtual
    }
}
