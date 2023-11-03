package com.example.teste

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Adapter
import android.widget.ArrayAdapter
import android.widget.SimpleAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.teste.databinding.ActivityCadastroAnimal1Binding
import com.example.teste.databinding.ActivityInformacoesPropriedadeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CadastroAnimal1 : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var binding: ActivityCadastroAnimal1Binding? = null
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private lateinit var storageReference: StorageReference
    private lateinit var imageUri: Uri
    private var imagemSelecionadaUri: Uri? = null
    private lateinit var bluetoothManager: BluetoothManager
    var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private val REQUEST_BLUETOOTH_PERMISSION = 1

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_animal1)
        binding = ActivityCadastroAnimal1Binding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        val email = user?.email.toString()

        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

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

        var image = false

        binding?.btAdicionarImagem?.setOnClickListener {
            selectImage()

            image = true
        }

        listPairedDevices()

        binding?.btProximo?.setOnClickListener{
            val numeroIdentificacao = binding?.editNumeroAnimal?.text.toString()
            val nascimentoAnimal = binding?.editData?.text.toString()
            val raca: String = binding?.spinnerRaca?.selectedItem.toString()
            val sexo = if (binding?.radioGroupSexo?.checkedRadioButtonId == R.id.checkFemea) { "Fêmea" } else { "Macho" }
            val tipo = if (binding?.radioGroupTipo?.checkedRadioButtonId == R.id.checkOvino) { "Ovino" } else { "Caprino" }

            val fileName = numeroIdentificacao
            val nomePropriedade = intent.getStringExtra("nome propriedade").toString()

            if (numeroIdentificacao.isNotEmpty() && sexo.isNotEmpty() && raca.isNotEmpty()) {
                if (image == true) {
                    uploadImage(numeroIdentificacao, email)

                    storageReference = FirebaseStorage.getInstance().getReference().child("Imagens").child(email).child("Propriedades").child(nomePropriedade).child("Animais").child(fileName)
                    storageReference.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()

                        val navegarCadastroAnimal2 = Intent(this, CadastroAnimal2::class.java)
                        navegarCadastroAnimal2.putExtra("numero animal", numeroIdentificacao)
                        navegarCadastroAnimal2.putExtra("nascimento", nascimentoAnimal)
                        navegarCadastroAnimal2.putExtra("raça", raca)
                        navegarCadastroAnimal2.putExtra("sexo", sexo)
                        navegarCadastroAnimal2.putExtra("tipo", tipo)
                        navegarCadastroAnimal2.putExtra("imageUrl", imageUrl)
                        startActivity(navegarCadastroAnimal2)
                    }
                } else {
                    val navegarCadastroAnimal2 = Intent(this, CadastroAnimal2::class.java)
                    navegarCadastroAnimal2.putExtra("numero animal", numeroIdentificacao)
                    navegarCadastroAnimal2.putExtra("nascimento", nascimentoAnimal)
                    navegarCadastroAnimal2.putExtra("raça", raca)
                    navegarCadastroAnimal2.putExtra("sexo", sexo)
                    navegarCadastroAnimal2.putExtra("tipo", tipo)
                    startActivity(navegarCadastroAnimal2)
                }

            } else {
                Toast.makeText(this@CadastroAnimal1, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun listPairedDevices () {
        var pairedDevices: Set<BluetoothDevice>? = null
        val pairedDevicesNames = mutableListOf<String>()
        if (checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
             pairedDevices = bluetoothAdapter?.bondedDevices
        } else {
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH), REQUEST_BLUETOOTH_PERMISSION)
        }

        pairedDevices?.forEach { device->
            val deviceName = device.name
            val deviceHardwareAdress = device.address
            pairedDevicesNames.add(deviceName)
        }

        val adapter = ArrayAdapter (this, android.R.layout.simple_spinner_item, pairedDevicesNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val spinner = findViewById<Spinner>(R.id.spinnerDevices)

        spinner.adapter = adapter
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
        storageReference.putFile(imageUri)
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