package com.example.teste.data.classesDoBanco

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.teste.data.classesDeDados.Animal
import com.example.teste.data.classesDeDados.Image
import com.example.teste.data.classesDeDados.Register
import com.example.teste.data.classesDeDados.User

@Database(
    entities = [
        User::class,
        Animal::class,
        Register::class,
        Image::class
    ],
    version = 1,
    exportSchema = false
)
abstract class UserDatabase: RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: UserDatabase? = null

        fun getDatabase(context: Context): UserDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserDatabase::class.java,
                    "user_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}