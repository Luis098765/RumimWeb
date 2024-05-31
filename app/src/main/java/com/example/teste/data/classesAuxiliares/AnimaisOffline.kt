package com.example.teste.data.classesAuxiliares

import com.example.teste.data.classesDeDados.Animal

class AnimaisOffline private constructor (email: String, animaisOffline: List<Animal>?) {
    private val animaisOffline = animaisOffline

    companion object {
        private var instance: AnimaisOffline? = null

        fun getInstance(email: String, animaisOffline: List<Animal>?): AnimaisOffline {
            if (instance == null) {
                instance = AnimaisOffline(email, animaisOffline)
            }
            return instance!!
        }
    }

    fun getAnimaisOffline(): List<Animal>? {
        return animaisOffline
    }

    fun delete() {
        instance = null
    }
}