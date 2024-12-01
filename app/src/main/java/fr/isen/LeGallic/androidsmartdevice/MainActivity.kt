package fr.isen.legallic.androidsmartdevice

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.legallic.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidSmartDeviceTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(169, 169, 169))
                ) { innerPadding ->
                    Greeting(
                        name = "Erwan",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current

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
            Image(
                painter = painterResource(id = R.drawable.b_logo),
                contentDescription = "Icon",
                modifier = Modifier
                    .size(128.dp)
                    .padding(bottom = 10.dp)
            )

            Text(text = "Bonjour $name !", color = Color.White, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(6.dp))

            Text(text = "Bienvenue sur l'application de scan BLE", color = Color.White,fontSize = 20.sp)
            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Ici vous pouvez scanner les appareils ",
                color = Color.White,
                fontSize = 20.sp
            )
            Text(
                text = "BLE alentours",
                color = Color.White,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            FilledTonalButton(onClick = {
                val intent = Intent(context, ScanActivity::class.java)
                context.startActivity(intent)
            }) {
                Text(text = "Aller au scanner")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidSmartDeviceTheme {
        Greeting("Erwan")
    }
}
