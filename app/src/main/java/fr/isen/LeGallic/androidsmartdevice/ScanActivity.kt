package fr.isen.LeGallic.androidsmartdevice

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.LeGallic.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme
import fr.isen.LeGallic.androidsmartdevice.R

class ScanActivity : ComponentActivity() {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Vérifie les permissions avant de lancer l'interface
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

    val devices = listOf(
        "Appareil1" to "00:14:22:01:23:45",
        "Appareil2" to "00:14:22:01:23:46",
        "Appareil3" to "00:14:22:01:23:47"
    )

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
                            if (isScanning) {
                                isScanning = false
                                imageResource = R.drawable.recherche
                                progressVisibility = false
                            } else {
                                isScanning = true
                                imageResource = R.drawable.stop
                                progressVisibility = true
                            }
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
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
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