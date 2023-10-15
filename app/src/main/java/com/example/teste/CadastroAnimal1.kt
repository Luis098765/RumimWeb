package com.example.teste

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Adapter
import android.widget.ArrayAdapter
import android.widget.SimpleAdapter
import android.widget.Spinner
import android.widget.Toast
import com.example.teste.databinding.ActivityCadastroAnimal1Binding
import com.example.teste.databinding.ActivityInformacoesPropriedadeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CadastroAnimal1 : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var binding: ActivityCadastroAnimal1Binding? = null
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private lateinit var storageReference: StorageReference
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_animal1)
        binding = ActivityCadastroAnimal1Binding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        val email = user?.email.toString()

        binding?.btVoltar?.setOnClickListener{
            val voltarInformacoesPropriedade = Intent(this, InformacoesPropriedade::class.java)
            startActivity(voltarInformacoesPropriedade)
        }

        val spinner = findViewById<Spinner>(R.id.spinnerRaca)
        spinner.prompt = ""

        val opcoesOvino = arrayOf("Crioula Lanada", "Cariri", "Rabo Largo ou Dâmara", "Somalis Brasileira", "Santa Inês", "Barriga Negra", "Morada Nova", "Não especificado")
        val opcoesCaprino = arrayOf("Boer", "Saanen", "Anglo-Nubiana", "Toggenburg", "Angorá", "Moxotó", "Pardo Alpino", "Não especificado")

        binding?.radioGroupTipo?.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.checkOvino -> {
                    val ovinoAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcoesOvino)
                    ovinoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = ovinoAdapter
                }
                R.id.checkCaprino -> {
                    val caprinoAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcoesCaprino)
                    caprinoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = caprinoAdapter
                }
            }
        }

        binding?.btAdicionarImagem?.setOnClickListener {
            selectImage()
        }

        binding?.btProximo?.setOnClickListener{
            val numeroIndentificacao = binding?.editNumeroAnimal?.text.toString()
            val nascimentoAnimal = binding?.editData?.text.toString()
            val raca = binding?.spinnerRaca?.selectedItem.toString()
            val sexo = if (binding?.radioGroupSexo?.checkedRadioButtonId == R.id.checkFemea) { "Fêmea" } else { "Macho" }

            val animalMap = hashMapOf (
                "numero" to numeroIndentificacao,
                "nascimento" to nascimentoAnimal,
                "raça" to raca,
                "sexo" to sexo
            )

            db.collection("Usuarios").document(email).collection("Propriedades").get().addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val nomePropriedade = querySnapshot.documents[0].id

                    db.collection("Usuarios").document(email).collection("Propriedades").document(nomePropriedade).collection("Animais").document(numeroIndentificacao).set(animalMap)
                    uploadImage(numeroIndentificacao, email)
                }
            }

            val navegarCadastroAnimal2 = Intent(this, CadastroAnimal2::class.java)
            navegarCadastroAnimal2.putExtra("numero animal", numeroIndentificacao)
            navegarCadastroAnimal2.putExtra("nascimento", nascimentoAnimal)
            navegarCadastroAnimal2.putExtra("raça", raca)
            navegarCadastroAnimal2.putExtra("sexo", sexo)
            startActivity(navegarCadastroAnimal2)
        }
    }

    private fun selectImage() {
        val selecionarImagem = Intent ()
        selecionarImagem.type = "image/*"
        selecionarImagem.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(selecionarImagem, 100)
    }

    private fun uploadImage(numeroIdentificacao: String, email: String, ) {
        val fileName = numeroIdentificacao
        val nomePropriedade = intent.getStringExtra("nome propriedade").toString()

        storageReference = FirebaseStorage.getInstance().getReference().child("Imagens").child(email).child("Propriedades").child(nomePropriedade).child("Animais").child(fileName)

        storageReference.putFile(imageUri).addOnSuccessListener {

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && data != null && data.data != null) {
            data?.data?.let {
                imageUri = it
            }
        }
    }
}