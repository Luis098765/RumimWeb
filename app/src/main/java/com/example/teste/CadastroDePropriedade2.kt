package com.example.teste

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.CheckBox
import android.widget.RadioButton
import com.example.teste.databinding.ActivityCadastroDePropriedade1Binding
import com.example.teste.databinding.ActivityCadastroDePropriedade2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CadastroDePropriedade2 : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var binding: ActivityCadastroDePropriedade2Binding? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_de_propriedade2)
        binding = ActivityCadastroDePropriedade2Binding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = FirebaseAuth.getInstance()

        binding?.btVoltar?.setOnClickListener {
            val voltarTela = Intent (this,CadastroDePropriedade1::class.java)
            startActivity(voltarTela)
        }

        binding?.btSalvar?.setOnClickListener{
            createProperty()

            val voltarTelaPrincipal = Intent (this,Principal::class.java)
            startActivity(voltarTelaPrincipal)
        }
    }

    private fun createProperty () {
        val user = auth.currentUser
        val intent = intent
        val email = user?.email.toString()

        val nome = intent.getStringExtra("nome").toString()
        val localizacao = intent.getStringExtra("localizacao").toString()
        val area = intent.getStringExtra("area").toString()
        val pequenosRuminantes = intent.getStringExtra("pequenos ruminantes").toString()
        val outrasCriacoes = intent.getStringExtra("outras criacoes")
        val responsavel = intent.getStringExtra("responsavel")

        val checkPoco = findViewById<CheckBox>(R.id.checkPoco)
        val checkAbastecimento = findViewById<CheckBox>(R.id.checkAbastecimento)
        val checkFontesNaturais = findViewById<CheckBox>(R.id.checkRios)

        val docRef = db.collection("Usuarios").document(email).collection("Propriedades").document(nome)

        val recursosHidricos: String = if (checkPoco.isChecked && checkAbastecimento.isChecked && checkFontesNaturais.isChecked) {
            "Poço profundo, abastecimento de água da rua e fontes de água naturais"
        } else if (checkPoco.isChecked && checkAbastecimento.isChecked) {
            "Poço profundo e abastecimento de água da rua"
        } else if (checkPoco.isChecked && checkFontesNaturais.isChecked) {
            "Poço profundo e fontes de água naturais"
        } else if (checkAbastecimento.isChecked && checkFontesNaturais.isChecked) {
            "Abastecimento de água da rua e fontes de água naturais"
        } else if (checkPoco.isChecked) {
            "Poço profundo"
        } else if (checkAbastecimento.isChecked) {
            "Abastecimento de água da rua"
        } else if (checkFontesNaturais.isChecked) {
            "Fontes de água naturais"
        } else {
            "Nenhum"
        }

        val checkPraticasAmbientais = binding?.radioGroup?.checkedRadioButtonId
        val praticasAmbientais: String = if (checkPraticasAmbientais == R.id.checkSim) {
            binding?.editPraticasAmbientais?.text.toString()
        } else {
            "Nenhuma"
        }

        val propriedadeMap = mapOf(
            "Nome da propriedade" to nome,
            "Localização da propriedade" to localizacao,
            "Area em hectares" to area,
            "Pequenos ruminantes" to pequenosRuminantes,
            "Outras criações" to outrasCriacoes,
            "Responsável técnico" to responsavel,
            "Recursos hídricos da propriedade" to recursosHidricos,
            "Práticas ambientais" to praticasAmbientais
        )

        docRef.set(propriedadeMap)
    }
}