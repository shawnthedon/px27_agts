package com.pocketstrike.app.ui

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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

data class PhoneOsintProject(
    val name: String,
    val githubUrl: String,
    val description: String,
    val category: String,
    val stars: String,
    val language: String,
    val features: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OsintPhoneDashboardScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var inputNumber by remember { mutableStateOf("") }
    var analyzedCountry by remember { mutableStateOf<String?>(null) }
    var analyzedE164 by remember { mutableStateOf<String?>(null) }
    var analyzedDorks by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    val phoneProjects = listOf(
        PhoneOsintProject(
            name = "PhoneInfoga",
            githubUrl = "https://github.com/sundowndev/phoneinfoga",
            description = "One of the most advanced OSINT tools to scan international phone numbers with web GUI and multi-scanner engine.",
            category = "Phone Number Reconnaissance",
            stars = "13.8k+",
            language = "Go",
            features = listOf("Embedded Web GUI & REST API", "Numverify & OVH scanners", "Google search dork engine")
        ),
        PhoneOsintProject(
            name = "Google libphonenumber",
            githubUrl = "https://github.com/google/libphonenumber",
            description = "Google's authoritative library for parsing, formatting, carrier mapping, and geocoding international phone numbers.",
            category = "Parsing, Geocoding & Validation",
            stars = "17.4k+",
            language = "Java / C++ / JS",
            features = listOf("Global E.164 parsing rules", "Carrier & region geocoding", "Timezone identification")
        ),
        PhoneOsintProject(
            name = "Moriarty Project",
            githubUrl = "https://github.com/AzizKaba/Moriarty-Project",
            description = "Comprehensive phone number investigation tool scanning public registries, social footprints, and spam databases.",
            category = "Phone Intelligence & Social Lookup",
            stars = "800+",
            language = "Python",
            features = listOf("Social footprinting", "Spam & fraud reputation", "Carrier origin discovery")
        ),
        PhoneOsintProject(
            name = "Ignorant",
            githubUrl = "https://github.com/megadose/ignorant",
            description = "Checks if a phone number is registered on major social platforms (Snapchat, Instagram, etc.) without sending SMS.",
            category = "Account Association",
            stars = "1.9k+",
            language = "Python",
            features = listOf("No SMS alert sent to target", "Multi-platform auditing", "Clean JSON export")
        ),
        PhoneOsintProject(
            name = "TruecallerJS",
            githubUrl = "https://github.com/sumithemmadi/truecallerjs",
            description = "Open-source Node.js library & CLI to search and identify caller ID, names, and spam scores via Truecaller.",
            category = "Caller ID & Directory",
            stars = "1.4k+",
            language = "JavaScript",
            features = listOf("Global Truecaller search", "Caller name & carrier", "CLI and Node.js API")
        ),
        PhoneOsintProject(
            name = "Blackbird Phone OSINT",
            githubUrl = "https://github.com/p1ngul1n0/blackbird",
            description = "High-speed OSINT search engine querying user profiles, phone references, and account associations across 500+ sites.",
            category = "Fast Correlated OSINT",
            stars = "4.2k+",
            language = "Python",
            features = listOf("Async multithreaded scan", "Phone & username correlation", "PDF/CSV reporting")
        ),
        PhoneOsintProject(
            name = "GhostTrack",
            githubUrl = "https://github.com/HunxByts/GhostTrack",
            description = "Phone number and IP geolocation reconnaissance tool identifying country, region, carrier, and timezone.",
            category = "Carrier & Country Telemetry",
            stars = "1.1k+",
            language = "Python",
            features = listOf("Country and regional mapping", "Carrier network analysis", "Terminal interface")
        ),
        PhoneOsintProject(
            name = "Numverify OSINT CLI",
            githubUrl = "https://github.com/sundowndev/numverify",
            description = "CLI tool and API client for global phone number validation, international carrier detection, and line-type classification.",
            category = "Carrier & Line Classification",
            stars = "300+",
            language = "Go / Shell",
            features = listOf("VoIP vs Mobile vs Landline", "Global carrier resolution", "High accuracy checks")
        ),
        PhoneOsintProject(
            name = "PhoneSploit-Pro",
            githubUrl = "https://github.com/AzeemIdrisi/PhoneSploit-Pro",
            description = "All-in-one phone reconnaissance and Android device auditing framework with terminal dashboard.",
            category = "Mobile Auditing & Network",
            stars = "3.7k+",
            language = "Python",
            features = listOf("ADB network discovery", "SIM state inspection", "Batch network testing")
        ),
        PhoneOsintProject(
            name = "Twilio Lookup API",
            githubUrl = "https://github.com/twilio/twilio-node",
            description = "Enterprise carrier metadata, line-type intelligence, caller name (CNAM), and SIM-swap fraud risk scoring library.",
            category = "Enterprise Carrier Intelligence",
            stars = "1.2k+",
            language = "TypeScript / JS",
            features = listOf("Authoritative carrier lookup", "Caller Name (CNAM)", "SIM-swap risk scoring")
        )
    )

    fun analyze(num: String) {
        val digits = num.replace(Regex("[^0-9]"), "")
        if (digits.length >= 7) {
            val e164 = "+$digits"
            analyzedE164 = e164
            analyzedCountry = when {
                digits.startsWith("1") -> "🇺🇸/🇨🇦 United States / Canada (+1)"
                digits.startsWith("44") -> "🇬🇧 United Kingdom (+44)"
                digits.startsWith("49") -> "🇩🇪 Germany (+49)"
                digits.startsWith("33") -> "🇫🇷 France (+33)"
                digits.startsWith("234") -> "🇳🇬 Nigeria (+234)"
                digits.startsWith("91") -> "🇮🇳 India (+91)"
                else -> "🌐 International ($e164)"
            }
            analyzedDorks = listOf(
                "Exact Match" to "\"$e164\"",
                "Social Footprint" to "\"$e164\" site:facebook.com OR site:linkedin.com OR site:instagram.com",
                "Public Paste Dork" to "\"$e164\" site:pastebin.com OR site:github.com",
                "Spam & Caller ID" to "\"$e164\" \"spam\" OR \"scam\" OR \"caller id\""
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Top 10 Phone OSINT Projects", color = PureWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Carrier Intelligence, Geocoding & Dorks", color = TextMuted, fontSize = 12.sp)
                    }
                },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Interactive Analyzer Box
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkCard, RoundedCornerShape(12.dp))
                        .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text("🔍 Interactive Phone Number Analyzer", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("E.164 normalization, country prefix detection, and search dorks", color = TextMuted, fontSize = 12.sp)

                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = inputNumber,
                                onValueChange = {
                                    inputNumber = it
                                    analyze(it)
                                },
                                placeholder = { Text("Enter number (e.g. +1 555-0199)", color = Color.Gray, fontSize = 13.sp) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonGreen,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = PureWhite,
                                    unfocusedTextColor = PureWhite
                                ),
                                singleLine = true
                            )
                            Spacer(Modifier.width(8.dp))
                            FilledTonalButton(
                                onClick = { analyze(inputNumber) },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = NeonGreen.copy(alpha = 0.2f))
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = NeonGreen)
                            }
                        }

                        if (analyzedE164 != null) {
                            Spacer(Modifier.height(12.dp))
                            Text("Normalized: $analyzedE164", color = NeonGreen, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                            Text("Country: ${analyzedCountry ?: ""}", color = PureWhite, fontSize = 13.sp)

                            Spacer(Modifier.height(8.dp))
                            Text("Google OSINT Dorks:", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            analyzedDorks.forEach { (title, query) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("• $title", color = TextMuted, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                    IconButton(
                                        onClick = {
                                            val url = "https://www.google.com/search?q=" + Uri.encode(query)
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Top 10 Phone OSINT Projects
            items(phoneProjects) { project ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkCard, RoundedCornerShape(12.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(project.name, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF1A1E2D), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("★ ${project.stars}", color = Color(0xFFFFD32A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(Modifier.height(6.dp))
                        Text(project.description, color = TextMuted, fontSize = 13.sp)

                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.background(Color(0xFF141724), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text(project.category, color = Color(0xFF64B5F6), fontSize = 10.sp)
                            }
                            Box(modifier = Modifier.background(Color(0xFF141724), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text(project.language, color = Color.LightGray, fontSize = 10.sp)
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        project.features.forEach { feat ->
                            Text("• $feat", color = Color(0xFFB0B8CB), fontSize = 11.sp)
                        }

                        Spacer(Modifier.height(12.dp))
                        FilledTonalButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(project.githubUrl)).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = NeonGreen.copy(alpha = 0.15f)),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("View on GitHub", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
