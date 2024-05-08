package com.example.teste

import android.Manifest
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.NfcA
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
import com.example.teste.databinding.ActivityInformacoesPropriedadeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.IOException
import java.io.InputStream
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
    val user = auth.currentUser
    val email = user?.email.toString()
    private val handler = Handler()
    private var handlerAtivo = true
    private var selectedDeviceName: String? = null
    var pairedDevices: Set<BluetoothDevice>? = null
    private var nfcAdapter: NfcAdapter? = null
    private var tagNumber: String? = null
    private var nfc = false

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

        try {
            db.collection("Usuarios").document(email).collection("Propriedades").get().addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val nomePropriedade = querySnapshot.documents[0].id

                    db.collection("Usuarios").document(email).collection("Propriedades").document(nomePropriedade).addSnapshotListener { documento, error ->
                        if (documento?.exists() == true) {
                            binding?.textViewNome?.text = documento.getString("Nome da propriedade")
                            binding?.textViewLocal?.text = documento.getString("Localização da propriedade")

                            db.collection("Usuarios").document(email).collection("Propriedades").document(nomePropriedade).collection("Animais").get().addOnSuccessListener { querySnapshot ->
                                val numeroAnimaisAtivos = querySnapshot.size()

                                binding?.textViewQtdAtivos?.text = numeroAnimaisAtivos.toString()
                            }
                        }
                    }
                }
            }
        } catch (e: IOException) {
            val intent = intent

            binding?.textViewNome?.text = intent.getStringExtra("Nome da propriedade")
            binding?.textViewLocal?.text = intent.getStringExtra("Localização")
            binding?.textViewQtdAtivos?.text = intent.getStringExtra("Quantidade")
        }


        binding?.btAdicionar?.setOnClickListener {
            try {
                db.collection("Usuarios").document(email).collection("Propriedades").get().addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val nomePropriedade = querySnapshot.documents[0].id

                        val navegarCadastroAnimal1 = Intent(this, CadastroAnimal1::class.java)
                        navegarCadastroAnimal1.putExtra("nome propriedade", nomePropriedade)
                        startActivity(navegarCadastroAnimal1)
                    }
                }
            } catch (e: IOException) {
                val intent = intent

                val navegarCadastroAnimal1 = Intent(this, CadastroAnimal1::class.java)
                navegarCadastroAnimal1.putExtra("email", email)
                navegarCadastroAnimal1.putExtra("nome propriedade", intent.getStringExtra("Nome da propriedade"))
                startActivity(navegarCadastroAnimal1)
            }
        }

        binding?.btPesquisar?.setOnClickListener {
            val navegarTelaRebanho = Intent(this, Rebanho::class.java)
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

//        binding?.btEscanearComCelular?.setOnClickListener {
//            nfcAdapter = NfcAdapter.getDefaultAdapter(this)
//
//            if (nfcAdapter == null) {
//                Toast.makeText(this@InformacoesPropriedade, "Seu dispositivo não suporta essa função", Toast.LENGTH_SHORT).show()
//            } else {
//                nfc = true
//            }
//        }
    }

//    override fun onResume() {
//        super.onResume()
//
//
//            val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
//            val pendingIntent = PendingIntent.getActivity(
//                this,
//                0,
//                Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
//                PendingIntent.FLAG_IMMUTABLE
//            )
//            val intentFilters = arrayOf<IntentFilter>(
//                IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
//                IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
//                IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
//            )
//            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null)
//
//    }

//    override fun onPause() {
//        super.onPause()
//        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
//        nfcAdapter.disableForegroundDispatch(this)
//    }

//    override fun onNewIntent(intent: Intent) {
//        super.onNewIntent(intent)
//
//        Log.d("a", "a")
//            if (intent?.action == NfcAdapter.ACTION_TAG_DISCOVERED || intent?.action == NfcAdapter.ACTION_NDEF_DISCOVERED || intent?.action == NfcAdapter.ACTION_TECH_DISCOVERED) {
//                Log.d("b", "b")
//                val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                    Log.d("c","c")
//                    intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
//                } else {
//                    Log.d("d","d")
//                    intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
//                }
//                Log.d("e","e")
//                tag?.id?.let {
//                    Log.d("f", "f")
//                    val tagValue = it.toHexString()
//                    Log.d("Tag", tagValue)
//                    Toast.makeText(this, "NFC tag detected: $tagValue", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//    }
//
//    fun ByteArray.toHexString(): String {
//        val hexChars = "0123456789ABCDEF"
//        val result = StringBuilder(size * 2)
//
//        map { byte ->
//            val value = byte.toInt()
//            val hexChar1 = hexChars[value shr 4 and 0x0F]
//            val hexChar2 = hexChars[value and 0x0F]
//            result.append(hexChar1)
//            result.append(hexChar2)
//        }
//
//        return result.toString()
//    }
//
//    private fun createNFCIntentFilter(): Array<IntentFilter> {
//        val intentFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
//        try {
//            intentFilter.addDataType("*/*")
//        } catch (e: IntentFilter.MalformedMimeTypeException) {
//            throw RuntimeException("Failed to add MIME type.", e)
//        }
//        return arrayOf(intentFilter)
//    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun listPairedDevices () {
        //var pairedDevices: Set<BluetoothDevice>? = null
        val pairedDevicesNames = mutableListOf<String>()
        if (checkSelfPermission(android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
            pairedDevices = bluetoothAdapter?.bondedDevices
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.BLUETOOTH), REQUEST_BLUETOOTH_PERMISSION)
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
                    Toast.makeText(this@InformacoesPropriedade, "Dspositivo Conectado", Toast.LENGTH_SHORT).show()
                }

                val dataThread = mmSocket?.let { DataThread(it, this@InformacoesPropriedade) }
                dataThread?.start()

            } catch (e: IOException) {
                bluetoothHandler.post {
                    Toast.makeText(this@InformacoesPropriedade, "Erro ao conectar dispositivo", Toast.LENGTH_SHORT).show()
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

            while (true) {
                try {
                    bytesRead = inputStream.read(buffer)
                    data = String(buffer, 0 , bytesRead)

                    runOnUiThread() {
                        tagRFID = data.replace(" ", "").replace("\r", "").replace("\n", "")

                        if (tagRFID.isNotEmpty() && tagRFID != "" && tagRFID != "ConexãoBluetoothestabelecidacomsucesso!") {
                            db.collection("Usuarios").document(email).collection("Propriedades").get().addOnSuccessListener { querySnapshot ->
                                if (!querySnapshot.isEmpty) {
                                    val nomePropriedade = querySnapshot.documents[0].id
                                    Toast.makeText(this@InformacoesPropriedade, "Tag lida!", Toast.LENGTH_SHORT).show()

                                    if (nomePropriedade != null) {
                                        db.collection("Usuarios").document(email).collection("Propriedades").document(nomePropriedade).collection("Animais").get().addOnSuccessListener { querySnapshot ->
                                            var cont = 0

                                            for (document in querySnapshot) {
                                                cont++

                                                if (document.id == tagRFID) {
                                                    val navegarPerfilAnimal = Intent(activityContext, PerfilAnimal::class.java)
                                                    navegarPerfilAnimal.putExtra("documentId", tagRFID)
                                                    startActivity(navegarPerfilAnimal)

                                                    break
                                                } else if (cont == querySnapshot.size()) {
                                                    Toast.makeText(this@InformacoesPropriedade, "Animal não encontrado", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Log.d("Tag RFID", tagRFID)
                    }
                } catch (e: IOException) {
                    break
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        handler.removeCallbacks(mostrarDispositivos)
    }
}