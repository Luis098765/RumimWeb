package com.example.teste.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnimalViewModel(application: Application): AndroidViewModel(application) {
    val readAllData: LiveData<List<Animal>>
    private val repository: AnimalRepository

    init {
        val animalDao = AnimalDatabase.getDatabase(application).animalDao()
        repository = AnimalRepository(animalDao)
        readAllData = repository.readAllData
    }

    fun addAnimal(animal: Animal) {
        viewModelScope.launch (Dispatchers.IO) {
            repository.addAnimal(animal)
        }
    }

    fun deleteAllData() {
        viewModelScope.launch (Dispatchers.IO) {
            repository.deleteAllData()
        }
    }
}