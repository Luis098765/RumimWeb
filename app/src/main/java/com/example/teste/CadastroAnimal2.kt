package com.example.teste

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.teste.databinding.ActivityCadastroAnimal1Binding
import com.example.teste.databinding.ActivityCadastroAnimal2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CadastroAnimal2 : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var binding: ActivityCadastroAnimal2Binding? = null
    private val db = FirebaseFirestore.getInstance()

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
        }
    }

    private fun createAnimal () {
        val user = auth.currentUser
        val email = user?.email.toString()
        val intent = intent

        val numeroAnimal = intent.getStringExtra("numero animal").toString()
        val nascimentoAnimal = intent.getStringExtra("nascimento").toString()
        val raca = intent.getStringExtra("raça").toString()
        val sexo = intent.getStringExtra("sexo").toString()
        val categoria = binding?.spinnerCategoria?.selectedItem.toString()
        val pesoNascimento = binding?.editPesoNascimento?.text.toString() + " Kg"
        val pesoDesmame = binding?.editPesoDesmame?.text.toString() + " Kg"
        val dataDesmame = binding?.editDataDesmame?.text.toString()

        val animalMap = hashMapOf (
            "Número de identificação" to numeroAnimal,
            "Data de nascimento" to nascimentoAnimal,
            "Raça" to raca,
            "Sexo" to sexo,
            "Categoria" to categoria,
            "Peso ao nascimento" to pesoNascimento,
            "Peso ao desmame" to pesoDesmame,
            "Data do desmame" to dataDesmame
        )

        db.collection("Usuarios").document(email).collection("Propriedades").get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                val nomePropriedade = querySnapshot.documents[0].id

                db.collection("Usuarios").document(email).collection("Propriedades").document(nomePropriedade).collection("Animais").document(numeroAnimal).set(animalMap)
            }
        }
    }
}