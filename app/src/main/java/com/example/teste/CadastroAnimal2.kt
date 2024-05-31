package com.example.teste

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.teste.data.classesAuxiliares.ImagemAnimal
import com.example.teste.databinding.ActivityCadastroAnimal2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class CadastroAnimal2 : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var binding: ActivityCadastroAnimal2Binding? = null
    private val db = FirebaseFirestore.getInstance()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_animal2)
        binding = ActivityCadastroAnimal2Binding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = FirebaseAuth.getInstance()

        val tipo = intent.getStringExtra("tipo").toString()
        val sexo = intent.getStringExtra("sexo").toString()

        val spinner = findViewById<Spinner>(R.id.spinnerCategoria)
        spinner.prompt = ""
        if (tipo == "Ovino") {
            if (sexo == "Fêmea") {
                val opcoesOvinoFemea = arrayOf("Cordeira", "Borrega", "Ovelha")
                val ovinoFemeaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcoesOvinoFemea)
                ovinoFemeaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = ovinoFemeaAdapter
            } else {
                val opcoesOvinoMacho = arrayOf("Cordeiro", "Borrego", "Carneiro")
                val ovinoMachoAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcoesOvinoMacho)
                ovinoMachoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = ovinoMachoAdapter
            }
        } else {
            if (sexo == "Fêmea") {
                val opcoesCaprinoFemea = arrayOf("Cabrita em aleitamento", "Cabrita desmamada", "Cabrita em engorda", "Cabra")
                val caprinoFemeaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcoesCaprinoFemea)
                caprinoFemeaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = caprinoFemeaAdapter
            } else {
                val opcoesCaprinoMacho = arrayOf("Cabrito em aleitamento", "Cabrito desmamado", "Cabrito em engorda", "Bode")
                val caprinoMachoAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcoesCaprinoMacho)
                caprinoMachoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = caprinoMachoAdapter
            }
        }

        binding?.btVoltar?.setOnClickListener {
            val voltarCadastroAnimal1 = Intent(this, CadastroAnimal1::class.java)
            startActivity(voltarCadastroAnimal1)
        }

        binding?.btSalvar?.setOnClickListener {
            createAnimal()

            val voltarTelaPropriedade = Intent(this, InformacoesPropriedade::class.java)
            startActivity(voltarTelaPropriedade)
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun createAnimal () {
        val intent = intent
        val email = intent.getStringExtra("email").toString()
        val nomePropriedade = intent.getStringExtra("nomePropriedade").toString()

        val numeroAnimal = intent.getStringExtra("numero animal").toString()
        val nascimentoAnimal = intent.getStringExtra("nascimento").toString()
        val raca = intent.getStringExtra("raça").toString()
        val sexo = intent.getStringExtra("sexo").toString()
        val categoria = binding?.spinnerCategoria?.selectedItem.toString()
        val pesoNascimento = binding?.editPesoNascimento?.text.toString() + " Kg"
        var imageUrl: String? = null

        val storageReference = FirebaseStorage.getInstance().reference.child("Imagens").child(email).child("Propriedades").child(nomePropriedade).child("Animais").child(numeroAnimal)

        Log.d("email", email)
        Log.d("nomePropriedade", nomePropriedade)
        Log.d("numeroAnimal", numeroAnimal)
        if (isNetworkAvailable()) {
            storageReference.downloadUrl.addOnSuccessListener {
                imageUrl = it.toString()
                Log.d("Url baixada!", imageUrl.toString())

                val animalMap = hashMapOf (
                    "Número de identificação" to numeroAnimal,
                    "Data de nascimento" to nascimentoAnimal,
                    "Raça" to raca,
                    "Sexo" to sexo,
                    "Categoria" to categoria,
                    "Peso ao nascimento" to pesoNascimento,
                    "Status do animal" to "Ativo",
                    "Url da imagem do animal" to imageUrl
                )

                db.collection("Usuarios").document(email).collection("Propriedades").get().addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val nomePropriedade = querySnapshot.documents[0].id

                        if (nomePropriedade != null) {
                            db.collection("Usuarios").document(email).collection("Propriedades").document(nomePropriedade).collection("Animais").document(numeroAnimal).set(animalMap)
                        } else {
                            Toast.makeText(this@CadastroAnimal2, "Falha ao salvar animal, tente novamente", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }.addOnFailureListener {
                Log.e("Url não baixada", it.toString())

                val animalMap = hashMapOf (
                    "Número de identificação" to numeroAnimal,
                    "Data de nascimento" to nascimentoAnimal,
                    "Raça" to raca,
                    "Sexo" to sexo,
                    "Categoria" to categoria,
                    "Peso ao nascimento" to pesoNascimento,
                    "Status do animal" to "Ativo",
                )

                db.collection("Usuarios").document(email).collection("Propriedades").get().addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val nomePropriedade = querySnapshot.documents[0].id

                        if (nomePropriedade != null) {
                            db.collection("Usuarios").document(email).collection("Propriedades").document(nomePropriedade).collection("Animais").document(numeroAnimal).set(animalMap)
                        } else {
                            Toast.makeText(this@CadastroAnimal2, "Falha ao salvar animal, tente novamente", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } else {
            val imageUriString = intent.getStringExtra("imageUri")

            val imagemAnimal = ImagemAnimal.getInstance(numeroAnimal, null)

            if (imageUriString != "null") {

//                val animal = User(
//                    0,
//                    numeroAnimal,
//                    nascimentoAnimal,
//                    raca,
//                    sexo,
//                    imagemAnimal.getImage(),
//                    categoria,
//                    "Ativo",
//                    pesoNascimento
//                    /*null,
//                    * null*/
//                )
//
//                Log.d("numeroAnimal", numeroAnimal)
//                Log.d("nascimentoAnimal", nascimentoAnimal)
//                Log.d("raca", raca)
//                Log.d("sexo", sexo)
////                Log.d("imageUri", imageUriString)
//                Log.d("categoria", categoria)
//                Log.d("pesoNascimento", pesoNascimento)
//                Log.d("Animal", animal.toString())
//
//                val mAnimalViewModel = ViewModelProvider(this)[UserViewModel::class.java]
//
//                mAnimalViewModel.addAnimal(animal)
//                Log.d("Animal salvo offline", "success")
//                Toast.makeText(this@CadastroAnimal2, "Animal salvo offline!", Toast.LENGTH_SHORT).show()
//
//                imagemAnimal.delete()
//            } else {
//                val animal = User(
//                    0,
//                    numeroAnimal,
//                    nascimentoAnimal,
//                    raca,
//                    sexo,
//                    null,
//                    categoria,
//                    "Ativo",
//                    pesoNascimento
//                    /*null,
//                    * null*/
//                )
//
//                Log.d("numeroAnimal", numeroAnimal)
//                Log.d("nascimentoAnimal", nascimentoAnimal)
//                Log.d("raca", raca)
//                Log.d("sexo", sexo)
//                Log.d("categoria", categoria)
//                Log.d("pesoNascimento", pesoNascimento)
//                Log.d("Animal", animal.toString())
//
//                val mAnimalViewModel = ViewModelProvider(this)[UserViewModel::class.java]
//
//                mAnimalViewModel.addAnimal(animal)
//                Log.d("Animal salvo offline", "success")
//                Toast.makeText(this@CadastroAnimal2, "Animal salvo offline!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null &&
                (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }
}