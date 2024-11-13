package fr.isen.LeGallic.androidsmartdevice

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.LeGallic.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme
import fr.isen.LeGallic.androidsmartdevice.R

class ScanActivity : ComponentActivity() {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // check perm avant de lancer interface
        checkBluetoothPermissions()

        setContent {
            AndroidSmartDeviceTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    BluetoothScannerUI(
                        bluetoothAdapter = bluetoothAdapter,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    public fun startBLEScan(onDeviceFound: (String, String) -> Unit) {
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val scanFilter = ScanFilter.Builder()
            .build()

        val filters = listOf(scanFilter)

        val foundDevicesMac = mutableSetOf<String>()
        val foundDevicesName = mutableSetOf<String>()

        scanner?.startScan(filters, scanSettings, object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                val deviceName = result.device.name ?: "Inconnu"
                val macAddress = result.device.address

                // Vérifier si l'adresse MAC ou le nom sont déjà présents dans les sets
                if (!foundDevicesMac.contains(macAddress) && !foundDevicesName.contains(deviceName)) {
                    foundDevicesMac.add(macAddress)
                    foundDevicesName.add(deviceName)

                    // logs pour debug
                    Log.d("ScanActivity", "Nom de l'appareil : $deviceName, Adresse MAC : $macAddress")

                    onDeviceFound(deviceName, macAddress)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                Log.e("ScanActivity", "Scan échoué avec le code d'erreur: $errorCode")
            }
        })
    }


    @SuppressLint("MissingPermission")
    public fun stopBLEScan() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(object : ScanCallback() {})
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // check bluetooth perm
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
            ) {
                // ask bluetooth perm
                requestPermissions(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ),
                    REQUEST_BLUETOOTH_PERMISSIONS
                )
            }
        } else {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_BLUETOOTH_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Toast.makeText(this, "Permissions Bluetooth accordées", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permissions Bluetooth refusées", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission de localisation accordée", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission de localisation refusée", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_BLUETOOTH_PERMISSIONS = 101
        private const val REQUEST_LOCATION_PERMISSION = 102
    }
}

@Composable
fun BluetoothScannerUI(bluetoothAdapter: BluetoothAdapter?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var bluetoothEnabled by remember { mutableStateOf(bluetoothAdapter?.isEnabled == true) }
    var isScanning by remember { mutableStateOf(false) }
    var imageResource by remember { mutableIntStateOf(R.drawable.recherche) }
    var progressVisibility by remember { mutableStateOf(false) }
    val devices = remember { mutableStateListOf<Pair<String, String>>() }

    val startScan: () -> Unit = {
        devices.clear() // Clear la list
        isScanning = true
        imageResource = R.drawable.stop
        progressVisibility = true
        (context as? ScanActivity)?.startBLEScan { name, mac ->
            // verifier si l'adresse MAC ou le nom sont deja présents dans la liste avant d'ajouter
            if (devices.none { it.second == mac || it.first == name }) {
                devices.add(name to mac)
            }
        }
    }

    val stopScan: () -> Unit = {
        isScanning = false
        imageResource = R.drawable.recherche
        progressVisibility = false
        (context as? ScanActivity)?.stopBLEScan()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(169, 169, 169)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = "SCAN BLE",
                color = Color.White,
                fontSize = 26.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (!bluetoothEnabled) {
                Text(
                    text = "Bluetooth désactivé. Veuillez l'activer.",
                    color = Color.Yellow,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "Cliquez pour démarrer le scan",
                    color = Color.White,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Image(
                    painter = painterResource(id = imageResource),
                    contentDescription = "Scan Icon",
                    modifier = Modifier
                        .size(128.dp)
                        .clickable {
                            if (isScanning) stopScan() else startScan()
                        }
                )

                if (progressVisibility) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .size(50.dp),
                        color = Color.White,
                        strokeWidth = 4.dp
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                if (isScanning) {
                    Text(
                        text = "Appareils détectés :",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(devices) { device ->
                            val (deviceName, macAddress) = device
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "${devices.indexOf(device) + 1}.",
                                        color = Color.White,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = deviceName,
                                        color = Color.White,
                                        modifier = Modifier.weight(1f),
                                        fontSize = 16.sp
                                    )
                                }
                                Text(
                                    text = "Adresse MAC: $macAddress",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(start = 32.dp, top = 4.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = Color.White, thickness = 1.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}
