package com.pocketstrike.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocketstrike.app.core.protocol.CapabilityDefinition
import com.pocketstrike.app.core.protocol.CommandAuthorization
import com.pocketstrike.app.core.protocol.ExecutionResult
import com.pocketstrike.app.core.protocol.PhoneControlCommand
import com.pocketstrike.app.core.protocol.RiskLevel
import com.pocketstrike.app.discovery.CapabilityDiscoveryEngine
import com.pocketstrike.app.engine.ExecutionEngine
import com.pocketstrike.app.theme.DarkBase
import com.pocketstrike.app.theme.DarkBorder
import com.pocketstrike.app.theme.DarkCard
import com.pocketstrike.app.theme.NeonGreen
import com.pocketstrike.app.theme.OledBlack
import com.pocketstrike.app.theme.PureWhite
import com.pocketstrike.app.theme.TextMuted
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapabilityExplorerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val discoveryEngine = remember { CapabilityDiscoveryEngine(context) }
    val executionEngine = remember { ExecutionEngine(context) }
    val scope = rememberCoroutineScope()

    var capabilities by remember { mutableStateOf<List<CapabilityDefinition>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("all") }
    var testResult by remember { mutableStateOf<ExecutionResult?>(null) }
    var isExecuting by remember { mutableStateOf(false) }

    fun refresh() {
        val manifest = discoveryEngine.discoverCapabilities("local_device")
        capabilities = manifest.capabilities
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    val categories = listOf("all", "screen", "application", "device_info", "connectivity", "telephony", "messaging", "audio", "camera", "storage", "notifications", "location", "sensors", "settings", "automation", "security")

    val filteredList = if (selectedCategory == "all") capabilities else capabilities.filter { it.category == selectedCategory }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Capability Explorer", color = PureWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("${capabilities.count { it.available }} available • ${capabilities.size} registered", color = TextMuted, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PureWhite)
                    }
                },
                actions = {
                    IconButton(onClick = { refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = NeonGreen)
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
        ) {
            // Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat.replace("_", " "), fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonGreen.copy(alpha = 0.2f),
                            selectedLabelColor = NeonGreen,
                            containerColor = DarkCard,
                            labelColor = TextMuted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (selectedCategory == cat) NeonGreen else DarkBorder,
                            enabled = true,
                            selected = selectedCategory == cat
                        )
                    )
                }
            }

            // Test Result Banner
            if (testResult != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .background(DarkCard, RoundedCornerShape(8.dp))
                        .border(1.dp, if (testResult?.status?.key == "completed") NeonGreen else Color.Red, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Result: ${testResult?.status?.key?.uppercase()}",
                                color = if (testResult?.status?.key == "completed") NeonGreen else Color.Red,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(Modifier.weight(1f))
                            Text("${testResult?.durationMs}ms", color = TextMuted, fontSize = 11.sp)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Verified: ${testResult?.verification?.verified} (${testResult?.verification?.verificationMethod ?: testResult?.verification?.reason ?: "none"})",
                            color = PureWhite,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Capability List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList) { cap ->
                    CapabilityItemCard(
                        cap = cap,
                        onExecuteTest = {
                            scope.launch {
                                isExecuting = true
                                val cmd = PhoneControlCommand(
                                    commandId = "test_${System.currentTimeMillis()}",
                                    capability = cap.id,
                                    parameters = JSONObject(),
                                    authorization = CommandAuthorization(
                                        agentId = "manual_tester",
                                        approvalToken = "tester_bypass_token"
                                    )
                                )
                                testResult = executionEngine.executeCommand(cmd)
                                isExecuting = false
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CapabilityItemCard(
    cap: CapabilityDefinition,
    onExecuteTest: () -> Unit
) {
    val riskColor = when (cap.riskLevel) {
        RiskLevel.LEVEL_0_INFO -> Color(0xFF4CAF50)
        RiskLevel.LEVEL_1_ORDINARY -> Color(0xFF2196F3)
        RiskLevel.LEVEL_2_SENSITIVE -> Color(0xFFFF9800)
        RiskLevel.LEVEL_3_HIGH -> Color(0xFFE91E63)
        RiskLevel.LEVEL_4_CRITICAL -> Color(0xFFF44336)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(12.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when {
                        cap.available -> Icons.Default.CheckCircle
                        cap.permissionStatus == "not_granted" -> Icons.Default.Lock
                        else -> Icons.Default.Error
                    },
                    contentDescription = null,
                    tint = when {
                        cap.available -> NeonGreen
                        cap.permissionStatus == "not_granted" -> Color(0xFFFF9800)
                        else -> Color.Gray
                    },
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(cap.name, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.weight(1f))

                // Risk Badge
                Box(
                    modifier = Modifier
                        .background(riskColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Risk ${cap.riskLevel.value}", color = riskColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(cap.description, color = TextMuted, fontSize = 12.sp)

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    cap.id,
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
                )

                if (cap.available) {
                    FilledTonalButton(
                        onClick = onExecuteTest,
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = NeonGreen.copy(alpha = 0.2f)),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Test", tint = NeonGreen, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Test", color = NeonGreen, fontSize = 11.sp)
                    }
                } else {
                    Text(
                        cap.unavailabilityReason ?: "Unavailable",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
