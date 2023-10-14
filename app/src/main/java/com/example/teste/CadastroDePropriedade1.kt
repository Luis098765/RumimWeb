package com.example.teste

import android.content.Intent
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
            val area: String = if (binding?.editArea?.text.toString().isNotEmpty()) { binding?.editArea?.text.toString() } else { "Desconhecida" }
            val checkOvino = findViewById<CheckBox>(R.id.check_ovino)
            val checkCaprino = findViewById<CheckBox>(R.id.check_caprino)
            val pequenosRuminantes: String = if (checkOvino.isChecked && checkCaprino.isChecked) { "Ovinos e Caprinos" }
            else if (checkOvino.isChecked) { "Ovinos" } else if (checkCaprino.isChecked) { "Caprinos" } else { "Nenhum" }
            val outrasCriacoes: String = if (binding?.editOutros?.text.toString().isNotEmpty()) { binding?.editOutros?.text.toString() } else { "Nenhuma" }
            val responsavel: String = if (binding?.editResponsavel?.text.toString().isNotEmpty()) { binding?.editResponsavel?.text.toString() } else { "Não cadastrado" }

            if (nome.isNotEmpty() && localizacao.isNotEmpty()) {
                val navegarCadastroDePropriedade2 = Intent(this,CadastroDePropriedade2::class.java)
                navegarCadastroDePropriedade2.putExtra("nome", nome)
                navegarCadastroDePropriedade2.putExtra("localizacao", localizacao)
                navegarCadastroDePropriedade2.putExtra("area", area)
                navegarCadastroDePropriedade2.putExtra("pequenos ruminantes", pequenosRuminantes)
                navegarCadastroDePropriedade2.putExtra("outras criacoes", outrasCriacoes)
                navegarCadastroDePropriedade2.putExtra("responsavel", responsavel)
                startActivity(navegarCadastroDePropriedade2)
            }
        }
    }
}