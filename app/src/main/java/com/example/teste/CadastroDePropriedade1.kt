package com.example.teste

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.CheckBox
import com.example.teste.databinding.ActivityCadastroDePropriedade1Binding
import com.example.teste.databinding.ActivityTelaDeCadastroBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CadastroDePropriedade1 : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var binding: ActivityCadastroDePropriedade1Binding? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_de_propriedade1)
        binding = ActivityCadastroDePropriedade1Binding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = FirebaseAuth.getInstance()

        binding?.BtProximaPagina?.setOnClickListener {
            val nome: String = binding?.editNome?.text.toString()
            val localizacao: String = binding?.editLocalizacao?.text.toString()

            if (nome.isNotEmpty() && localizacao.isNotEmpty()) {
                createProperty()
            }
        }
    }

    private fun createProperty () {
        val user = auth.currentUser
        val email = user?.email.toString()
        val nome: String = binding?.editNome?.text.toString()
        val localizacao: String = binding?.editLocalizacao?.text.toString()
        val area: String = binding?.editArea?.text.toString()
        val checkOvino = findViewById<CheckBox>(R.id.check_ovino)
        val checkCaprino = findViewById<CheckBox>(R.id.check_caprino)
        val pequenosRuminantes: String = if (checkOvino.isChecked && checkCaprino.isChecked) { "Ovinos e Caprinos" }
        else if (checkOvino.isChecked) { "Ovinos" } else if (checkCaprino.isChecked) { "Caprinos" } else { "Nenhum" }
        val outrasCriacoes: String = binding?.editOutros?.text.toString()
        val responsavel: String = binding?.editResponsavel?.text.toString()

        val propriedadeMap = hashMapOf (
            "Nome da propriedade" to nome,
            "Localização da propriedade" to localizacao,
            "Area em hectares" to area,
            "Pequenos ruminantes" to pequenosRuminantes,
            "Outras criações" to outrasCriacoes,
            "Responsável técnico" to responsavel
        )

        db.collection("Usuarios").document(email).collection("Propriedades").document(nome).set(propriedadeMap)
    }
}