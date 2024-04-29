package com.example.teste.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AnimalDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addAnimal(animal: Animal)

    @Query("SELECT * FROM animal_table ORDER BY id ASC")
    fun readAllData(): LiveData<List<Animal>>

    @Query("DELETE FROM animal_table")
    fun deleteAllData()
}