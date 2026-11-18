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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocketstrike.app.theme.DarkBorder
import com.pocketstrike.app.theme.DarkCard
import com.pocketstrike.app.theme.NeonGreen
import com.pocketstrike.app.theme.OledBlack
import com.pocketstrike.app.theme.PureWhite
import com.pocketstrike.app.theme.TextMuted

data class WebOsintProject(
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
fun OsintWebDashboardScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    val projects = listOf(
        WebOsintProject(
            name = "SpiderFoot",
            githubUrl = "https://github.com/smicallef/spiderfoot",
            description = "Open source intelligence automation tool with web UI. Gathers intelligence on IPs, domain names, e-mails, and phone numbers.",
            category = "Reconnaissance & Asset Discovery",
            stars = "14.5k+",
            language = "Python",
            features = listOf("200+ OSINT modules integrated", "Web GUI & REST API", "Passive DNS & WHOIS footprinting")
        ),
        WebOsintProject(
            name = "Sherlock",
            githubUrl = "https://github.com/sherlock-project/sherlock",
            description = "Hunt down social media accounts by username across 400+ social platforms and websites simultaneously.",
            category = "Identity & Social Footprint",
            stars = "62k+",
            language = "Python",
            features = listOf("400+ social networks scanned", "Tor routing privacy support", "Zero false-positive detection")
        ),
        WebOsintProject(
            name = "theHarvester",
            githubUrl = "https://github.com/laramies/theHarvester",
            description = "Powerful tool for gathering subdomains, email addresses, virtual hosts, and employee names from search engines and Shodan.",
            category = "Domain & Email Intelligence",
            stars = "11.5k+",
            language = "Python",
            features = listOf("Search engine & Shodan harvesting", "Subdomain bruteforce & DNS resolving", "HTML & XML reporting")
        ),
        WebOsintProject(
            name = "OSINT Framework",
            githubUrl = "https://github.com/lockfale/osint-framework",
            description = "Web-based tree navigation directory focusing on gathering information from free open source tools and intelligence resources.",
            category = "Directory & Framework",
            stars = "16.8k+",
            language = "JavaScript / HTML",
            features = listOf("Interactive collapsible tree graph", "Covers IP, domain, username, phone", "Zero backend setup needed")
        ),
        WebOsintProject(
            name = "Photon",
            githubUrl = "https://github.com/s0md3v/Photon",
            description = "High-speed crawler designed for OSINT information extraction. Discovers URLs with parameters, emails, and secret API keys.",
            category = "Web Scraping & Crawler",
            stars = "14.2k+",
            language = "Python",
            features = listOf("Threaded asynchronous crawling", "Regex extraction for secret keys", "Site graph visualization")
        ),
        WebOsintProject(
            name = "Recon-ng",
            githubUrl = "https://github.com/lanmaster53/recon-ng",
            description = "Full-featured web reconnaissance framework with modular database architecture, command completion, and API integrations.",
            category = "Web Reconnaissance Framework",
            stars = "4.3k+",
            language = "Python",
            features = listOf("Metasploit-like interactive environment", "SQLite database schema", "100+ community modules")
        ),
        WebOsintProject(
            name = "OWASP Amass",
            githubUrl = "https://github.com/owasp-amass/amass",
            description = "In-depth attack surface mapping and external asset discovery using open source information gathering and active DNS resolving.",
            category = "Attack Surface Mapping",
            stars = "11.2k+",
            language = "Go",
            features = listOf("Graph database storage (Neo4j/Postgres)", "BGP routing & ASN enumeration", "OWASP enterprise standards")
        ),
        WebOsintProject(
            name = "Sn1per",
            githubUrl = "https://github.com/1N3/Sn1per",
            description = "Automated reconnaissance scanner and attack surface management platform with unified web dashboard.",
            category = "Consolidated Attack Surface",
            stars = "7.8k+",
            language = "Shell / Python",
            features = listOf("Automated workflow pipelines", "Integrates Amass, Nmap, Nuclei", "Web GUI workspace management")
        ),
        WebOsintProject(
            name = "Social Analyzer",
            githubUrl = "https://github.com/qeeqbox/social-analyzer",
            description = "API, CLI, and Web App for analyzing and finding user profiles across 1000+ social media websites and services.",
            category = "Social Media Profiling",
            stars = "12.8k+",
            language = "Node.js / Python",
            features = listOf("Supports 1000+ sites", "Web GUI with filters & tables", "Automated profile inspection")
        ),
        WebOsintProject(
            name = "Holehe",
            githubUrl = "https://github.com/megadose/holehe",
            description = "Fast OSINT tool that checks if an email is attached to an account on 120+ sites using password recovery endpoints.",
            category = "Account Discovery & Email OSINT",
            stars = "5.4k+",
            language = "Python",
            features = listOf("Audits 120+ platforms", "Zero target notification triggered", "Async high-speed requests")
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Top 10 Web OSINT Projects", color = PureWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Consolidated GitHub Open Source Intelligence", color = TextMuted, fontSize = 12.sp)
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(projects) { project ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkCard, RoundedCornerShape(12.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(20.dp))
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

                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.background(Color(0xFF141724), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text(project.category, color = Color(0xFF64B5F6), fontSize = 10.sp)
                            }
                            Box(modifier = Modifier.background(Color(0xFF141724), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text(project.language, color = Color.LightGray, fontSize = 10.sp)
                            }
                        }

                        Spacer(Modifier.height(10.dp))
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
