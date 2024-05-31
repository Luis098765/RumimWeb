package com.example.teste.data.classesDoBanco

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import com.example.teste.data.classesDeDados.Animal
import com.example.teste.data.classesDeDados.AnimalWithRegisters
import com.example.teste.data.classesDeDados.Register
import com.example.teste.data.classesDeDados.User
import com.example.teste.data.classesDeDados.UserWithAnimals

class UserRepository(private val userDao: UserDao) {

    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun insertAnimal(animal: Animal) {
        userDao.insertAnimal(animal)
    }

    suspend fun insertRegister(register: Register) {
        userDao.insertRegister(register)
    }

    suspend fun getAllUsers(): List<User> {
        return userDao.getAllUsers()
    }

    suspend fun getUserWithAnimals(userEmail: String): List<UserWithAnimals> {
        return userDao.getUserWithAnimals(userEmail)
    }

    suspend fun getAnimalsWithRegisters(animalNumber: String): List<AnimalWithRegisters> {
        return userDao.getAnimalWithRegisters(animalNumber)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getPesoAtualFromAnimal(animalNumber: String): String {
        return userDao.getPesoAtualFromAnimal(animalNumber)
    }


}