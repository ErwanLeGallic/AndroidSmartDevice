package fr.isen.LeGallic.androidsmartdevice

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import fr.isen.LeGallic.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme
import java.util.*

class DeviceInfoActivity : ComponentActivity() {
    private var bluetoothGatt: BluetoothGatt? = null
    private var bluetoothDevice: BluetoothDevice? = null
    private val REQUEST_PERMISSION_CODE = 1

    // UUID du service et de la caractéristique à utiliser
    private val BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
    private val BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // on recp les donees du device selected
        val deviceName = intent.getStringExtra("deviceName") ?: "Inconnu"
        val macAddress = intent.getStringExtra("macAddress") ?: "Inconnu"

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        //recup le périphérique BLE via MAC
        bluetoothDevice = bluetoothAdapter?.getRemoteDevice(macAddress)

        // check perm
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT, android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSION_CODE
            )
        } else {
            connectToDevice()
        }
        //gui
        setContent {
            AndroidSmartDeviceTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Informations de l'appareil",
                        color = Color.White,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Nom : $deviceName",
                        color = Color.White,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Adresse MAC : $macAddress",
                        color = Color.White,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Bouton pour se co
                    Button(
                        onClick = { connectToDevice() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Se connecter à l'appareil")
                    }
                }
            }
        }
    }

    // Fonction pour se co
    @SuppressLint("MissingPermission")
    private fun connectToDevice() {
        bluetoothGatt = bluetoothDevice?.connectGatt(this, false, bluetoothGattCallback)
        Log.d("DeviceInfoActivity", "Tentative de connexion à l'appareil BLE : ${bluetoothDevice?.address}")
    }

    // Callback pour gérer les réponses de la connexion BLE
    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            when (newState) {
                BluetoothGatt.STATE_CONNECTED -> {
                    Log.d("DeviceInfoActivity", "Connecté à l'appareil BLE.")
                    gatt?.discoverServices()
                }
                BluetoothGatt.STATE_DISCONNECTED -> {
                    Log.d("DeviceInfoActivity", "Déconnecté de l'appareil BLE.")
                }
                else -> {
                    Log.d("DeviceInfoActivity", "Changement d'état de connexion : $newState")
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("DeviceInfoActivity", "Services découverts sur l'appareil BLE.")
                val service: BluetoothGattService? = gatt?.getService(BATTERY_SERVICE_UUID)
                val characteristic: BluetoothGattCharacteristic? = service?.getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID)
                gatt?.readCharacteristic(characteristic)
            } else {
                Log.d("DeviceInfoActivity", "Échec de la découverte des services.")
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("DeviceInfoActivity", "Caractéristique lue avec succès : ${characteristic?.value}")
                // Traitez la donnée lue
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        bluetoothGatt?.close()
        Log.d("DeviceInfoActivity", "Connexion fermée.")
    }
}
