package com.example.teste

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
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
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.reflect.KProperty

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
    private var tagRFId: String = ""
    private val handler = Handler()
    private var handlerAtivo = true
    private var selectedDeviceName: String? = null
    var pairedDevices: Set<BluetoothDevice>? = null
    private lateinit var email: String
    private var image = false

    private val mostrarDispositivos = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.M)
        override fun run() {
            if (handlerAtivo) {
                listPairedDevices()

                handler.postDelayed(this, 1000)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_animal1)
        binding = ActivityCadastroAnimal1Binding.inflate(layoutInflater)
        setContentView(binding?.root)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            ), REQUEST_BLUETOOTH_PERMISSION)
        }

        handler.post(mostrarDispositivos)

        try {
            auth = FirebaseAuth.getInstance()

            val user = auth.currentUser
            email = user?.email.toString()
        } catch (e: IOException) {
            email = intent.getStringExtra("email") ?: ""
        }

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

        binding?.btAdicionarImagem?.setOnClickListener {
            selectImage()
        }

        if (checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
            pairedDevices = bluetoothAdapter?.bondedDevices
        } else {
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH), REQUEST_BLUETOOTH_PERMISSION)
        }

        binding?.btConectar?.setOnClickListener {
            if (bluetoothAdapter?.isEnabled == false) {
                val ligarBluetooth = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(ligarBluetooth, REQUEST_BLUETOOTH_PERMISSION)

            } else {
                var selectedDevice: BluetoothDevice? = null
                selectedDevice = pairedDevices?.find { it.name == selectedDeviceName }

                if (checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
                    if (selectedDevice != null) {
                        connectToDevice(selectedDevice)
                    }
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.BLUETOOTH),
                        REQUEST_BLUETOOTH_PERMISSION
                    )
                }
            }
        }

        binding?.btProximo?.setOnClickListener{
            val numeroIdentificacao = binding?.editNumeroAnimal?.text.toString()
            val nascimentoAnimal = binding?.editData?.text.toString()
            val raca: String = binding?.spinnerRaca?.selectedItem.toString()
            val sexo = if (binding?.radioGroupSexo?.checkedRadioButtonId == R.id.checkFemea) {
                "Fêmea"
            } else {
                "Macho"
            }
            val tipo = if (binding?.radioGroupTipo?.checkedRadioButtonId == R.id.checkOvino) {
                "Ovino"
            } else {
                "Caprino"
            }

            val fileName = numeroIdentificacao
            val nomePropriedade = intent.getStringExtra("nome propriedade").toString()

            if (isNetworkAvailable()) {
                if (numeroIdentificacao.isNotEmpty() && sexo.isNotEmpty() && raca.isNotEmpty()) {
                    if (image == true) {

                        uploadImage(numeroIdentificacao, email)

                        Log.d("Numero do animal", "$numeroIdentificacao")
                        Log.d("Filename", "$fileName")

                        val navegarCadastroAnimal2 = Intent(this, CadastroAnimal2::class.java)
                        navegarCadastroAnimal2.putExtra("email", email)
                        navegarCadastroAnimal2.putExtra("nomePropriedade", nomePropriedade)
                        navegarCadastroAnimal2.putExtra("numero animal", numeroIdentificacao)
                        navegarCadastroAnimal2.putExtra("nascimento", nascimentoAnimal)
                        navegarCadastroAnimal2.putExtra("raça", raca)
                        navegarCadastroAnimal2.putExtra("sexo", sexo)
                        navegarCadastroAnimal2.putExtra("tipo", tipo)
                        startActivity(navegarCadastroAnimal2)
                        finish()

                    } else {
                        val navegarCadastroAnimal2 = Intent(this, CadastroAnimal2::class.java)
                        navegarCadastroAnimal2.putExtra("email", email)
                        navegarCadastroAnimal2.putExtra("nomePropriedade", nomePropriedade)
                        navegarCadastroAnimal2.putExtra("numero animal", numeroIdentificacao)
                        navegarCadastroAnimal2.putExtra("nascimento", nascimentoAnimal)
                        navegarCadastroAnimal2.putExtra("raça", raca)
                        navegarCadastroAnimal2.putExtra("sexo", sexo)
                        navegarCadastroAnimal2.putExtra("tipo", tipo)
                        startActivity(navegarCadastroAnimal2)
                        finish()
                    }

                } else {
                    Toast.makeText(
                        this@CadastroAnimal1,
                        "Preencha todos os campos!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                if (image) {
                    val navegarCadastroAnimal2 = Intent(this, CadastroAnimal2::class.java)
                    navegarCadastroAnimal2.putExtra("email", email)
                    navegarCadastroAnimal2.putExtra("nomePropriedade", nomePropriedade)
                    navegarCadastroAnimal2.putExtra("numero animal", numeroIdentificacao)
                    navegarCadastroAnimal2.putExtra("nascimento", nascimentoAnimal)
                    navegarCadastroAnimal2.putExtra("raça", raca)
                    navegarCadastroAnimal2.putExtra("sexo", sexo)
                    navegarCadastroAnimal2.putExtra("tipo", tipo)
                    navegarCadastroAnimal2.putExtra("fileName", fileName)
                    navegarCadastroAnimal2.putExtra("imageUri", imageUri.toString())
                    startActivity(navegarCadastroAnimal2)
                    finish()
                } else {
                    val navegarCadastroAnimal2 = Intent(this, CadastroAnimal2::class.java)
                    navegarCadastroAnimal2.putExtra("email", email)
                    navegarCadastroAnimal2.putExtra("nomePropriedade", nomePropriedade)
                    navegarCadastroAnimal2.putExtra("numero animal", numeroIdentificacao)
                    navegarCadastroAnimal2.putExtra("nascimento", nascimentoAnimal)
                    navegarCadastroAnimal2.putExtra("raça", raca)
                    navegarCadastroAnimal2.putExtra("sexo", sexo)
                    navegarCadastroAnimal2.putExtra("tipo", tipo)
                    navegarCadastroAnimal2.putExtra("fileName", fileName)
                    navegarCadastroAnimal2.putExtra("imageUri", "null")
                    startActivity(navegarCadastroAnimal2)
                    finish()
                }
            }
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, InformacoesPropriedade::class.java)
        startActivity(intent)
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null &&
                (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || networkCapabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_CELLULAR))
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun listPairedDevices () {
        //var pairedDevices: Set<BluetoothDevice>? = null
        val pairedDevicesNames = mutableListOf<String>()
        if (checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
             pairedDevices = bluetoothAdapter?.bondedDevices
        } else {
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH), REQUEST_BLUETOOTH_PERMISSION)
            pairedDevices = bluetoothAdapter?.bondedDevices
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
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                selectedDeviceName = spinner.selectedItem.toString()

                handlerAtivo = false
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun connectToDevice(device: BluetoothDevice) {
        val connectThread = ConnectThread(device)
        connectThread.start()
    }

    private val bluetoothHandler = Handler(Looper.getMainLooper())

    @RequiresApi(Build.VERSION_CODES.M)
    inner class ConnectThread(device: BluetoothDevice) : Thread() {
        private var mmSocket: BluetoothSocket? = null

        init {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
                mmSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
            } else {
                requestPermissions(arrayOf(Manifest.permission.BLUETOOTH), REQUEST_BLUETOOTH_PERMISSION)
            }
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun run() {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
                bluetoothAdapter?.cancelDiscovery()
            }

            try {
                mmSocket?.connect()
                bluetoothHandler.post {
                    Toast.makeText(this@CadastroAnimal1, "Dspositivo Conectado", Toast.LENGTH_SHORT).show()
                }

                val dataThread = mmSocket?.let { it1 -> DataThread(it1) }
                dataThread?.start()

            } catch (e: IOException) {
                bluetoothHandler.post {
                    Toast.makeText(this@CadastroAnimal1, "Erro ao conectar dispositivo", Toast.LENGTH_SHORT).show()
                }
            }
        }

        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e("mmSocket.close", "Could not close the client socket", e)
            }
        }
    }

    inner class DataThread(private val socket: BluetoothSocket) : Thread() {
        private val inputStream: InputStream

        init {
            try {
                inputStream = socket.inputStream
            } catch (e: IOException) {
                throw e
            }
        }

        override fun run() {
            val buffer = ByteArray(1024)
            var bytesRead: Int

            while (true) {
                try {
                    bytesRead = inputStream.read(buffer)
                    val data = String(buffer, 0 , bytesRead)

                    runOnUiThread() {
                        tagRFId = data.replace(" ", "").replace("\r", "").replace("\n", "")
                        tagRFId.replace("ConexãoBluetoothEstabelecidaComSucesso!", "Leia a Tag!")

                        binding?.editNumeroAnimal?.text = tagRFId
                        Log.d("Tag RFID", "$data")
                        Log.d("Tag RFID", "$tagRFId")
                    }
                } catch (e: IOException) {
                    break
                }
            }
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

        storageReference = FirebaseStorage.getInstance().reference.child("Imagens").child(email).child("Propriedades").child(nomePropriedade).child("Animais").child(fileName)
        Log.d("email", email)
        Log.d("nomePropriedade", nomePropriedade)
        Log.d("numeroAnimal", fileName)
        storageReference.putFile(imageUri).addOnSuccessListener {
            Log.d("Imagem", "carregada")
        }.addOnFailureListener{ exception ->
            Log.e("Imagem", "não carregada", exception)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && data != null && data.data != null) {
            data?.data?.let {
                imageUri = it

                image = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        handler.removeCallbacks(mostrarDispositivos)
    }
}
