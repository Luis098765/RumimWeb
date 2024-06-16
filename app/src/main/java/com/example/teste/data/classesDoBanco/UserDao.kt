package com.example.teste.data.classesDoBanco

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.teste.data.classesDeDados.Animal
import com.example.teste.data.classesDeDados.AnimalAndImage
import com.example.teste.data.classesDeDados.AnimalWithRegisters
import com.example.teste.data.classesDeDados.Image
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: Image)

    @Transaction
    @Query("SELECT * FROM User ORDER BY email ASC")
    suspend fun getAllUsers(): List<User>

    @Transaction
    @Query("SELECT * FROM User WHERE email = :userEmail")
    suspend fun getUserWithAnimals(userEmail: String): List<UserWithAnimals>

    @Transaction
    @Query("SELECT * FROM Animal WHERE numeroIdentificacao = :animalNumber")
    suspend fun getAnimalWithRegisters(animalNumber: String): List<AnimalWithRegisters>

    @Transaction
    @Query("SELECT * FROM Animal WHERE numeroIdentificacao = :animalNumber")
    suspend fun getAnimalAndImage(animalNumber: String): List<AnimalAndImage>

    @Transaction
    @Query("SELECT * FROM Image WHERE numeroDoAnimal = :imagemPadrao")
    suspend fun getNoImage(imagemPadrao: String = "imagemPadrao"): ByteArray

    @RequiresApi(Build.VERSION_CODES.O)
    @Transaction
    suspend fun getPesoAtualFromAnimal(animalNumber: String): String {
        val animal = getAnimalWithRegisters(animalNumber)
        var pesoAtual: String = if (animal.first().registers.isNullOrEmpty()) {
            if (animal.first().animal.pesoDesmame == "null" || animal.first().animal.pesoDesmame.isNullOrEmpty()) {
                animal.first().animal.pesoNascimento
            } else {
                animal.first().animal.pesoDesmame.toString()
            }
        } else {
            val registrosFiltrados = animal.first().registers.filter { it.nome.contains("Pesagem") }

            val registrosOrdenados = registrosFiltrados.sortedByDescending {
                LocalDate.parse(
                    it.data,
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
                )
            }

            registrosOrdenados.first().valor.toString()
        }

        return pesoAtual
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Transaction
    suspend fun getStatusFromAnimal(animalNumber: String): String {
        val animal = getAnimalWithRegisters(animalNumber)

        val registrosFiltrados = animal.first().registers.filter { it.nome.contains("Alteração de status") }

        var status = if (registrosFiltrados.isNullOrEmpty()) {
            "Ativo"
        } else {
            val registrosOrdenados = registrosFiltrados.sortedByDescending {
                LocalDate.parse(it.data, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            }

            registrosOrdenados.first().valor.toString()
        }

        return status
    }

    @Transaction
    @Query("DELETE FROM Animal WHERE numeroIdentificacao = :nulo")
    suspend fun killNullAnimals(nulo: String = "null")
}
