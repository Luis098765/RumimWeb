package com.example.teste.data.classesDoBanco

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import com.example.teste.data.classesDeDados.Animal
import com.example.teste.data.classesDeDados.AnimalAndImage
import com.example.teste.data.classesDeDados.AnimalWithRegisters
import com.example.teste.data.classesDeDados.Image
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

    suspend fun insertImage(image: Image) {
        userDao.insertImage(image)
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

    suspend fun getAnimalAndImage(animalNumber: String): List<AnimalAndImage> {
        return userDao.getAnimalAndImage(animalNumber)
    }

    suspend fun getNoImage(): ByteArray {
        return userDao.getNoImage()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getPesoAtualFromAnimal(animalNumber: String): String {
        return userDao.getPesoAtualFromAnimal(animalNumber)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getStatusFromAnimal(animalNumber: String): String {
        return userDao.getStatusFromAnimal(animalNumber)
    }

    suspend fun killNullAnimals() {
        return userDao.killNullAnimals()
    }
}