package com.example.teste.data.classesDoBanco

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.teste.data.classesDeDados.Animal
import com.example.teste.data.classesDeDados.AnimalWithRegisters
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

    fun getAllUsers(): List<User>? {
        var users: List<User>? = null

        viewModelScope.launch(Dispatchers.IO) {
            users = repository.getAllUsers()
        }

        return users
    }

    fun getUserWithAnimals(email: String): List<UserWithAnimals>? {
        var user: List<UserWithAnimals>? = null

        viewModelScope.launch(Dispatchers.IO) {
            user = repository.getUserWithAnimals(email)
        }

        return user
    }

    fun getAnimalWithRegisters(animalNumber: String): List<AnimalWithRegisters>? {
        var animal: List<AnimalWithRegisters>? = null

        viewModelScope.launch(Dispatchers.IO) {
            animal = repository.getAnimalsWithRegisters(animalNumber)
        }

        return animal
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getPesoAtualFromAnimal(animalNumber: String): String? {
        var pesoAtual: String? = null

        viewModelScope.launch(Dispatchers.IO) {
            pesoAtual = repository.getPesoAtualFromAnimal(animalNumber)
        }

        return pesoAtual
    }
}