package com.example.teste.data.classesAuxiliares

class ImagemAnimal private constructor (numeroIdentidicacao: String, imagem: ByteArray?) {
    private val imagem = imagem

    companion object {
        private var instance: ImagemAnimal? = null

        fun getInstance(numeroIdentidicacao: String, imagem: ByteArray?): ImagemAnimal {
            if (instance == null) {
                instance = ImagemAnimal(numeroIdentidicacao, imagem)
            }
            return instance!!
        }
    }

    fun getImage(): ByteArray? {
        return imagem
    }

    fun delete() {
        instance = null
    }
}