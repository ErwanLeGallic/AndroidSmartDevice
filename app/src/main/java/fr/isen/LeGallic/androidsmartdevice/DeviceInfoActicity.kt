package fr.isen.LeGallic.androidsmartdevice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.LeGallic.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme

class DeviceInfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Récupération des données transmises
        val deviceName = intent.getStringExtra("deviceName") ?: "Inconnu"
        val macAddress = intent.getStringExtra("macAddress") ?: "Inconnu"

        setContent {
            AndroidSmartDeviceTheme {
                DeviceInfoScreen(deviceName = deviceName, macAddress = macAddress)
            }
        }
    }
}

@Composable
fun DeviceInfoScreen(deviceName: String, macAddress: String) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        DeviceInfoUI(
            deviceName = deviceName,
            macAddress = macAddress,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun DeviceInfoUI(deviceName: String, macAddress: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(169, 169, 169)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
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
        }
    }
}
