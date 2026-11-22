package com.pocketstrike.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocketstrike.app.theme.DarkBorder
import com.pocketstrike.app.theme.DarkCard
import com.pocketstrike.app.theme.NeonGreen
import com.pocketstrike.app.theme.OledBlack
import com.pocketstrike.app.theme.PureWhite
import com.pocketstrike.app.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingSecurityScreen(onBack: () -> Unit) {
    var pairingCode by remember { mutableStateOf("842-195") }
    var hybridConnected by remember { mutableStateOf(true) }
    var emergencyDisconnected by remember { mutableStateOf(false) }
    var selectedAutonomy by remember { mutableStateOf("Power User") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pairing & Device Security", color = PureWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PureWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OledBlack)
            )
        },
        containerColor = OledBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pairing Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCard, RoundedCornerShape(12.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.QrCode, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Device Pairing OTP", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Enter this code in px27m platform to pair device", color = TextMuted, fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        pairingCode,
                        color = NeonGreen,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Connectivity Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCard, RoundedCornerShape(12.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text("Hybrid Connectivity Status", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Local LAN Daemon (Port 27027)", color = TextMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Text(if (hybridConnected) "LISTENING" else "STOPPED", color = if (hybridConnected) NeonGreen else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Remote px27m Gateway WSS", color = TextMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Text(if (hybridConnected) "ESTABLISHED" else "OFFLINE", color = if (hybridConnected) NeonGreen else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Autonomy Ceiling Selector
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCard, RoundedCornerShape(12.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text("Agent Autonomy Ceiling", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Maximum privilege level granted to paired px27m agents", color = TextMuted, fontSize = 12.sp)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Observer", "Assistant", "Operator", "Power User").forEach { level ->
                            val isSel = selectedAutonomy == level
                            Box(
                                modifier = Modifier
                                    .background(if (isSel) NeonGreen.copy(alpha = 0.2f) else OledBlack, RoundedCornerShape(6.dp))
                                    .border(1.dp, if (isSel) NeonGreen else DarkBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(level, color = if (isSel) NeonGreen else TextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Emergency Disconnect Kill-switch
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCard, RoundedCornerShape(12.dp))
                    .border(1.dp, if (emergencyDisconnected) Color.Red else DarkBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = Color.Red, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Emergency Disconnect", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Instantly terminate all agent sessions", color = TextMuted, fontSize = 11.sp)
                    }
                    Switch(
                        checked = emergencyDisconnected,
                        onCheckedChange = { emergencyDisconnected = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Red, checkedTrackColor = Color.Red.copy(alpha = 0.3f))
                    )
                }
            }
        }
    }
}
