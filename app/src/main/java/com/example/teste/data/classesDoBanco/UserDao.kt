package com.example.teste.data.classesDoBanco

import android.os.Build
import android.util.Log
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

    @Insert(onConflict = OnConflictStrategy.NONE)
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

        val pesoNascimento = animal.first().animal.pesoNascimento
        val pesoDesmame = animal.first().registers.find { it.nome.contains("Pesagem ao desmame") }?.valor ?: "null"
        //Log.d("PesoAnimal", "Peso Nascimento: $pesoNascimento, Peso Desmame: $pesoDesmame")
        val registrosPesagem = animal.first().registers.filter { it.nome.contains("Pesagem") && !it.nome.contains("Pesagem ao desmame") }
        //Log.d("PesoAnimal", "Registros de Pesagem: $registrosPesagem")

        val pesoAtual = if (registrosPesagem.isNullOrEmpty()) {
            if (pesoDesmame == "null") {
                pesoNascimento
            } else {
                pesoDesmame
            }
        } else {
            val registrosOrdenados = registrosPesagem.sortedByDescending {
                LocalDate.parse(
                    it.data,
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
                )
            }

            registrosOrdenados.first().valor.toString()
        }

        val registrosOrdenados = registrosPesagem.sortedByDescending {
            LocalDate.parse(
                it.data,
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
            )
        }


        //Log.d("PesoAnimal", "Peso Atual: ${registrosOrdenados.first().valor.toString()}")

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
