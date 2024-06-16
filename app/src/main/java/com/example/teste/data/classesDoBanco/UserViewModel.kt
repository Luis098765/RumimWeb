package com.example.teste.data.classesDoBanco

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.teste.data.classesDeDados.Animal
import com.example.teste.data.classesDeDados.AnimalAndImage
import com.example.teste.data.classesDeDados.AnimalWithRegisters
import com.example.teste.data.classesDeDados.Image
import com.example.teste.data.classesDeDados.Register
import com.example.teste.data.classesDeDados.User
import com.example.teste.data.classesDeDados.UserWithAnimals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel(application: Application): AndroidViewModel(application) {
    private val repository: UserRepository

    init {
        val userDao = UserDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
    }

    fun insertUser(user: User) {
        viewModelScope.launch (Dispatchers.IO) {
            repository.insertUser(user)
        }
    }

    fun insertAnimal(animal: Animal) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAnimal(animal)
        }
    }

    fun insertRegister(register: Register) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertRegister(register)
        }
    }

    fun insertImage(image: Image) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertImage(image)
        }
    }

    fun getAllUsers(): List<User>? {
        var users: List<User>? = null

        viewModelScope.launch(Dispatchers.IO) {
            users = repository.getAllUsers()
        }

        return users
    }

    suspend fun getUserWithAnimals(email: String): List<UserWithAnimals>? {
        return repository.getUserWithAnimals(email)
    }

    suspend fun getAnimalWithRegisters(animalNumber: String): List<AnimalWithRegisters>? {
        return repository.getAnimalsWithRegisters(animalNumber)
    }

    suspend fun getNoImage(): ByteArray {
        return repository.getNoImage()
    }

    suspend fun getAnimalAndImage(animalNumber: String): List<AnimalAndImage>? {
        return repository.getAnimalAndImage(animalNumber)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getPesoAtualFromAnimal(animalNumber: String): String? {
        return repository.getPesoAtualFromAnimal(animalNumber)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getStatusFromAnimal(animalNumber: String): String {
        return repository.getStatusFromAnimal(animalNumber)
    }

    suspend fun killNullAnimals() {
        return repository.killNullAnimals()
    }
}