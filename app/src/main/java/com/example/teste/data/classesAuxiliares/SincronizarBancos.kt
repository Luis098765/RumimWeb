package com.example.teste.data.classesAuxiliares

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.example.teste.data.classesDeDados.Animal
import com.example.teste.data.classesDeDados.Register
import com.example.teste.data.classesDoBanco.UserViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

class SincronizarBancos: Service() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var mUserViewModel: UserViewModel
    private var cont= 0

    private val viewModelStore: ViewModelStore = ViewModelStore()

    companion object {
        var email = ""
        var nomePropriedade = ""
    }

    override fun onCreate() {
        super.onCreate()
        mUserViewModel = ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory.getInstance(application))[UserViewModel::class.java]

        Log.d("Ponto", "5")

        val animaisOffline = AnimaisOffline.getInstance(email, null).getAnimaisOffline()
        AnimaisOffline.getInstance(email, null).delete()

        Log.d("Animaisoffline", animaisOffline.toString())

        Log.d("email", email)
        Log.d("nomePropriedade", nomePropriedade)
        Log.d("Animais", animaisOffline.toString())
        Log.d("Ponto", "6")

        CoroutineScope(Dispatchers.Main).launch {
            sincronizarBancosDeDados(email, nomePropriedade, animaisOffline)

            Log.d("Sincronização", "Conluída")

            CoroutineScope(Dispatchers.Main).cancel()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()

        CoroutineScope(Dispatchers.Main).cancel()
    }

    private suspend fun sincronizarBancosDeDados (email: String, nomePropriedade: String, animaisOffline: List<Animal>?) {
        Log.d("Ponto", "7")
        val storageReference = db.collection("Usuarios").document(email).collection("Propriedades").document(nomePropriedade).collection("Animais")

        withContext(Dispatchers.IO) {
            if (animaisOffline.isNullOrEmpty()) {
                Log.d("Ponto", "8")
                val animaisOnline = storageReference.get().await()

                animaisOnline.forEach { animal ->
                    Log.d("Ponto", "9")

                    val numeroIdentificacao = animal.data?.get("Número de identificação").toString()
                    val dataDesmame = animal.data?.get("Data do desmame").toString()
                    val pesoDesmame = animal.data?.get("Peso ao desmame").toString()
                    val dataNascimento = animal.getString("Data de nascimento").toString()
                    val pesoNascimento = animal.getString("Peso ao nascimento").toString()
                    val status = animal.data?.get("Status do animal").toString()
                    val categoria = animal.getString("Categoria").toString()
                    val raca = animal.getString("Raça").toString()
                    val sexo = animal.getString("Sexo").toString()
                    val imageUrl = animal.data?.get("Url da imagem do animal").toString()
                    var imageByteArray: ByteArray?

                    Log.d("numeroidentificacao", numeroIdentificacao)
                    Log.d("imageurl", imageUrl)
                    val storageRef = if (imageUrl != "null") {
                        FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                    } else {
                        FirebaseStorage.getInstance()
                            .getReferenceFromUrl("https://firebasestorage.googleapis.com/v0/b/teste-ruminweb.appspot.com/o/Imagens%2F66682.png?alt=media&token=c8ba32de-ea76-4d63-8caf-03c42971961e")
                    }

                    Log.d("storageRef", storageRef.toString())

                    val localFile = File.createTempFile("localFile", ".png")

                    storageRef.getFile(localFile).await()
                    val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    imageByteArray = outputStream.toByteArray()

                    val registrosOnline: MutableList<Register>? = null
                    val registros =
                        storageReference.document(numeroIdentificacao).collection("Registros")
                            .get().await()

                    registros.forEach { registroOnline ->
                        val nomeRegistro = registroOnline.id
                        val dataRegistro = when {
                            nomeRegistro.contains("Alteração de status") -> registroOnline.data?.get(
                                "Data da alteração"
                            )

                            nomeRegistro.contains("Vacina") -> registroOnline.data?.get("Data da vacina")
                            nomeRegistro.contains("Pesagem ao desmame") -> registroOnline.data?.get(
                                "Data do desmame"
                            )

                            else -> registroOnline.data?.get("Data da pesagem")
                        }
                        val valorRegistro = when {
                            nomeRegistro.contains("Alteração de status") -> registroOnline.data?.get(
                                "Status do animal"
                            )

                            nomeRegistro.contains("Pesagem ao desmame") -> registroOnline.data?.get(
                                "Peso ao desmame"
                            )

                            nomeRegistro.contains("Pesagem") -> registroOnline.data?.get("Peso atual")
                            else -> null
                        }
                        val descricaoRegistro = registroOnline.data?.get("Descrição")

                        registrosOnline?.add(
                            Register(
                                nomeRegistro,
                                dataRegistro.toString(),
                                valorRegistro.toString(),
                                descricaoRegistro.toString()
                            )
                        )
                    }

                    val animalOnline = Animal(
                        0,
                        0,
                        numeroIdentificacao,
                        dataNascimento,
                        raca,
                        sexo,
                        imageByteArray,
                        categoria,
                        status,
                        pesoNascimento,
                        pesoDesmame,
                        dataDesmame,
                        registrosOnline
                    )

                    Log.d("Animal", animalOnline.toString())

                    mUserViewModel.addAnimalToUser(email, animalOnline)
                    cont++
                    Log.d("Animais sincronizados", cont.toString())
                }
            }

            if (animaisOffline != null) {
                animaisOffline.forEach { animalOffline ->
                    val document = storageReference.document(animalOffline.numeroIdentificacao).get().await()

                    if (document.exists()) {
                        if (document.data?.get("Peso desmame") == null) {
                            storageReference.document(animalOffline.numeroIdentificacao).update("Peso ao desmame", animalOffline.pesoDesmame, "Data do desmame", animalOffline.dataDesmame)
                        }

                        if (document.data?.get("Status do animal") != animalOffline.status) {
                            storageReference.document(animalOffline.numeroIdentificacao).update("Status do animal", animalOffline.status)
                        }
                    } else {
                        val novoAnimal = if (animalOffline.pesoDesmame != null) {
                            hashMapOf(
                                "Número de identificação" to animalOffline.numeroIdentificacao,
                                "Data de nascimento" to animalOffline.nascimento,
                                "Raça" to animalOffline.raca,
                                "Sexo" to animalOffline.sexo,
                                "Categoria" to animalOffline.categoria,
                                "Peso ao nascimento" to animalOffline.pesoNascimento,
                                "Status do animal" to "Ativo",
                                "Peso ao desmame" to animalOffline.pesoDesmame,
                                "Data do desmame" to animalOffline.dataDesmame
                            )
                        } else {
                            hashMapOf(
                                "Número de identificação" to animalOffline.numeroIdentificacao,
                                "Data de nascimento" to animalOffline.nascimento,
                                "Raça" to animalOffline.raca,
                                "Sexo" to animalOffline.sexo,
                                "Categoria" to animalOffline.categoria,
                                "Peso ao nascimento" to animalOffline.pesoNascimento,
                                "Status do animal" to "Ativo"
                            )
                        }

                        storageReference.document(animalOffline.numeroIdentificacao).set(novoAnimal).await()
                        val firebaseStorageReference = FirebaseStorage.getInstance().reference.child("Imagens").child(email).child("Propriedades").child(nomePropriedade).child("Animais").child(animalOffline.numeroIdentificacao)

                        if (animalOffline.image != null) {
                            firebaseStorageReference.putBytes(animalOffline.image).await()
                        }
                    }

                    animalOffline.registros?.forEach { registroOffline ->
                        val registroOnline = storageReference.document(registroOffline.nome).get().await()
                        if (!registroOnline.exists()) {
                            val nomeData = when  {
                                registroOffline.nome.contains("Alteração de status") -> "Data da alteração"
                                registroOffline.nome.contains("Vacina") -> "Data da vacina"
                                registroOffline.nome.contains("Pesagem ao desmame") -> "Data do desmame"
                                else -> "Data da pesagem"
                            }
                            val nomeValor = when {
                                registroOffline.nome.contains("Alteração de status") -> "Status do animal"
                                registroOffline.nome.contains("Pesagem ao desmame") -> "Peso ao desmame"
                                else -> "Peso atual"
                            }

                            val novoRegistro = if (registroOffline.nome.contains("Vacina")) {
                                hashMapOf(
                                    nomeData to registroOffline.data,
                                    "Descrição" to registroOffline.descricao
                                )
                            } else {
                                hashMapOf(
                                    nomeData to registroOffline.data,
                                    nomeValor to registroOffline.valor,
                                    "Descrição" to registroOffline.descricao
                                )
                            }

                            storageReference.document(registroOffline.nome).set(novoRegistro).await()
                            cont++
                            Log.d("Animais sincronizados", cont.toString())
                        }
                    }
                }
            }
        }
    }
}