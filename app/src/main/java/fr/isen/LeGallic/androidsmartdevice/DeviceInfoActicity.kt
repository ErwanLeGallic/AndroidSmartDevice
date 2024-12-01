package fr.isen.legallic.androidsmartdevice

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.legallic.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme
import java.util.UUID

class DeviceInfoActivity : ComponentActivity() {
    private var bluetoothGatt: BluetoothGatt? = null
    private var bluetoothDevice: BluetoothDevice? = null

    private var ledServiceUUID: UUID? = null
    private var ledControlCharacteristicUUID: UUID? = null
    private var servicesDiscovered = false

    private val ledStates = mutableStateMapOf(1 to false, 2 to false, 3 to false)

    private var isConnected = mutableStateOf(false)

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deviceName = intent.getStringExtra("deviceName") ?: "Inconnu"
        val macAddress = intent.getStringExtra("macAddress") ?: "Inconnu"

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothDevice = bluetoothAdapter?.getRemoteDevice(macAddress)

        connectToDevice()

        setContent {
            AndroidSmartDeviceTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(169, 169, 169))
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
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isConnected.value) "État : Connecté" else "État : Déconnecté",
                        color = if (isConnected.value) Color.Green else Color.Red,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        LEDButton(ledNumber = 1)
                        Spacer(modifier = Modifier.width(16.dp))
                        LEDButton(ledNumber = 2)
                        Spacer(modifier = Modifier.width(16.dp))
                        LEDButton(ledNumber = 3)
                    }
                }
            }
        }
    }


    @Composable
    fun LEDButton(ledNumber: Int) {
        val ledState = ledStates[ledNumber] ?: false
        val imageRes = if (ledState) R.drawable.led_on else R.drawable.led_off
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "LED $ledNumber",
            modifier = Modifier
                .size(50.dp)
                .clickable {
                    toggleLED(ledNumber)
                }
        )
    }

    @SuppressLint("MissingPermission")
    private fun sendLEDCommand(value: Int, callback: (Boolean) -> Unit) {
        // Vérification si le service 3 (index 2) et la caractéristique existent
        val service = bluetoothGatt?.services?.getOrNull(2)  // Service à l'index 2
        if (service == null) {
            Log.e("DeviceInfoActivity", "Service à l'index 2 introuvable")
            Toast.makeText(this, "Service introuvable", Toast.LENGTH_SHORT).show()
            callback(false)
            return
        }

        // Vérification de la caractéristique à l'index 0 du service
        val characteristic =
            service.characteristics.getOrNull(0)
        if (characteristic == null) {
            Log.e("DeviceInfoActivity", "Caractéristique à l'index 0 introuvable dans le service")
            Toast.makeText(this, "Caractéristique introuvable", Toast.LENGTH_SHORT).show()
            callback(false)
            return
        }

        // Vérifier si la caractéristique supporte l'écriture
       /* if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) == 0) {
            Log.e("DeviceInfoActivity", "La caractéristique ne supporte pas l'écriture")
            Toast.makeText(
                this,
                "La caractéristique ne supporte pas l'écriture",
                Toast.LENGTH_SHORT
            ).show()
            callback(false)
            return
        }*/

        characteristic.value = byteArrayOf(value.toByte())

        val success = bluetoothGatt?.writeCharacteristic(characteristic) ?: false
        if (success) {
            Log.d("DeviceInfoActivity", "Commande LED envoyée avec succès : $value")
        } else {
            Log.e("DeviceInfoActivity", "Échec de l'envoi de la commande LED")
        }

        callback(success)
    }

    @SuppressLint("MissingPermission")
    private fun toggleLED(ledNumber: Int) {
        if (!servicesDiscovered) {
            Log.e("DeviceInfoActivity", "Les services n'ont pas encore été découverts.")
            Toast.makeText(
                this,
                "Les services n'ont pas encore été découverts.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val ledState = ledStates[ledNumber] ?: false
        val newLedState = !ledState
        ledStates[ledNumber] = newLedState

        // Envoi de la commande BLE avec la bonne valeur pour chaque LED
        val commandValue = when (ledNumber) {
            1 -> if (newLedState) 0x01 else 0x00  // LED 1 : 0x01 pour allumer, 0x00 pour éteindre
            2 -> if (newLedState) 0x02 else 0x00  // LED 2 : 0x02
            3 -> if (newLedState) 0x03 else 0x00  // LED 3 : 0x03
            else -> 0x00  // Cas par défaut
        }

        // Envoi de la commande via BLE
        sendLEDCommand(commandValue) { success ->
            if (success) {
                Log.d("DeviceInfoActivity", "Commande envoyée avec succès : $commandValue")
            } else {
                Toast.makeText(this, "Erreur lors de l'envoi de la commande", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice() {
        bluetoothGatt = bluetoothDevice?.connectGatt(this, false, bluetoothGattCallback)
        Log.d(
            "DeviceInfoActivity",
            "Tentative de connexion à l'appareil BLE : ${bluetoothDevice?.address}"
        )
    }

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                isConnected.value = true
                gatt?.discoverServices()
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                isConnected.value = false
                bluetoothGatt = null
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                servicesDiscovered = true
                gatt?.services?.forEachIndexed { index, service ->
                    // Log des services trouvés
                    Log.d("DeviceInfoActivity", "Service trouvé à l'index $index : ${service.uuid}")
                    if (index == 2) {  // Service 3 (index 2)
                        service.characteristics.forEachIndexed { charIndex, characteristic ->
                            // Log des caractéristiques
                            Log.d(
                                "DeviceInfoActivity",
                                "Caractéristique trouvée à l'index $charIndex : ${characteristic.uuid}"
                            )
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        bluetoothGatt?.close()
    }
}