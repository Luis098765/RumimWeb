package com.example.teste

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.example.teste.data.classesDeDados.Animal
import com.example.teste.data.classesDeDados.UserWithAnimals
import com.example.teste.data.classesDoBanco.UserViewModel
import com.example.teste.databinding.ActivityInformacoesPropriedadeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.lang.reflect.Method
import java.util.UUID


class InformacoesPropriedade : AppCompatActivity() {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var binding: ActivityInformacoesPropriedadeBinding? = null
    private val db = FirebaseFirestore.getInstance()
    private lateinit var bluetoothManager: BluetoothManager
    var bluetoothAdapter: BluetoothAdapter? = null
    private val REQUEST_BLUETOOTH_PERMISSION = 1
    lateinit var data: String
    private var tagRFID: String = ""
    private val handler = Handler()
    private var handlerAtivo = true
    private var selectedDeviceName: String? = null
    var pairedDevices: Set<BluetoothDevice>? = null
    private var nfcAdapter: NfcAdapter? = null
    private var tagNumber: String? = null
    private var nfc = false
    lateinit var email: String
    lateinit var nomePropriedade: String
    lateinit var mUserViewModel: UserViewModel
    var animais: List<Animal>? = null

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
        setContentView(R.layout.activity_informacoes_propriedade)
        binding = ActivityInformacoesPropriedadeBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        mUserViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        email = intent.getStringExtra("email").toString()
        nomePropriedade = intent.getStringExtra("nomePropriedade").toString()

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ), REQUEST_BLUETOOTH_PERMISSION)
        }

        handler.post(mostrarDispositivos)

        CoroutineScope(Dispatchers.IO).launch {
            val animals = mUserViewModel.getUserWithAnimals(email)?.firstOrNull()?.animals

            animais = if (animals.isNullOrEmpty()) {
                null
            } else {
                animals
            }

            binding?.textViewNome?.text = mUserViewModel.getUserWithAnimals(email!!)?.first()?.user?.nomePropriedade.toString()
            binding?.textViewLocal?.text = mUserViewModel.getUserWithAnimals(email!!)?.first()?.user?.localPropriedade.toString()
            binding?.textViewQtdAtivos?.text = mUserViewModel.getUserWithAnimals(email!!)?.first()?.animals?.size.toString()
        }

        binding?.btAdicionar?.setOnClickListener {
            val navegarCadastroAnimal1 = Intent(this, CadastroAnimal1::class.java)
            navegarCadastroAnimal1.putExtra("email", email)
            navegarCadastroAnimal1.putExtra("nome propriedade", nomePropriedade)
            startActivity(navegarCadastroAnimal1)
        }

        binding?.btPesquisar?.setOnClickListener {
            val navegarTelaRebanho = Intent(this, Rebanho::class.java)
            navegarTelaRebanho.putExtra("email", email)
            navegarTelaRebanho.putExtra("nomePropriedade", nomePropriedade)
            startActivity(navegarTelaRebanho)
        }

        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

        if (checkSelfPermission(android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
            pairedDevices = bluetoothAdapter?.bondedDevices
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.BLUETOOTH), REQUEST_BLUETOOTH_PERMISSION)
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
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun listPairedDevices () {
        val pairedDevicesNames = mutableListOf<String>()
        if (checkSelfPermission(android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
            pairedDevices = bluetoothAdapter?.bondedDevices
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.BLUETOOTH), REQUEST_BLUETOOTH_PERMISSION)
            pairedDevices = bluetoothAdapter?.bondedDevices
        }

        pairedDevices?.forEach { device->
            val deviceName = device.name
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
                    Toast.makeText(this@InformacoesPropriedade, "Dspositivo Conectado", Toast.LENGTH_SHORT).show()
                }

                val dataThread = mmSocket?.let { DataThread(it, this@InformacoesPropriedade) }
                dataThread?.start()

            } catch (e: IOException) {
                bluetoothHandler.post {
                    //Log.e("Erro", e.toString())
                    Toast.makeText(this@InformacoesPropriedade, "Erro ao conectar dispositivo", Toast.LENGTH_SHORT).show()
                }
            }
        }

        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                //Log.e("mmSocket.close", "Could not close the client socket", e)
            }
        }
    }

    inner class DataThread(private val socket: BluetoothSocket, private val activityContext: Context) : Thread() {
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
            var tagBuffer = ""
            var cont = 0

            while (true) {
                try {
                    bytesRead = inputStream.read(buffer)
                    data = String(buffer, 0 , bytesRead)

                    runOnUiThread() {
                        tagRFID = data.replace(" ", "")
                            .replace("\r", "")
                            .replace("\n", "")
                            .replace("ConexãoBluetoothestabelecidacomsucesso!", "")

                        if (tagRFID.contains("Conexão")) {
                            tagRFID = ""
                        }

                        Log.d("Tag", tagRFID)

                        if (tagRFID.length != 14 && cont < 2) {
                            tagBuffer += tagRFID
                            cont++
                        }

                        Log.d("Tag buffer", tagBuffer)

                        if (cont == 2) {
                            tagRFID = tagBuffer
                            tagBuffer = ""
                            cont = 0

                            Log.d("Tag", tagRFID)
                        }

                        if (tagRFID.isNotEmpty() && tagRFID != "" && tagRFID != "ConexãoBluetoothestabelecidacomsucesso!" && tagRFID.length == 14) {
                            var contAnimal = 0

                            if (animais != null) {
                                for (animal in animais!!) {
                                    contAnimal++

                                    if (animal.numeroIdentificacao == tagRFID) {
                                        val navegarPerfilAnimal =
                                            Intent(activityContext, PerfilAnimal::class.java)
                                        navegarPerfilAnimal.putExtra("documentId", tagRFID)
                                        startActivity(navegarPerfilAnimal)

                                        break
                                    } else if (contAnimal == animais!!.size) {
                                        Toast.makeText(
                                            this@InformacoesPropriedade,
                                            "Animal não encontrado",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    this@InformacoesPropriedade,
                                    "Nenhum animal registrado",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this@InformacoesPropriedade,
                                "Leia a Tag novamente!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: IOException) {
                    break
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        startActivity(Intent(this, Principal::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()

        handler.removeCallbacks(mostrarDispositivos)
    }
}