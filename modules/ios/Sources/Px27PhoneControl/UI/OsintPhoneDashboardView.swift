import SwiftUI

struct PhoneOsintItem: Identifiable {
    let id: String
    let name: String
    let githubUrl: String
    let description: String
    let category: String
    let stars: String
    let language: String
    let features: [String]
}

/// SwiftUI Dashboard for Top 10 Phone Tracking & Intelligence Projects from GitHub
public struct OsintPhoneDashboardView: View {
    @State private var inputNumber: String = ""
    @State private var analyzedE164: String? = nil
    @State private var analyzedCountry: String? = nil
    @State private var analyzedDorks: [(title: String, query: String)] = []

    private let projects: [PhoneOsintItem] = [
        PhoneOsintItem(
            id: "phoneinfoga",
            name: "PhoneInfoga",
            githubUrl: "https://github.com/sundowndev/phoneinfoga",
            description: "Advanced OSINT tool to scan international phone numbers with web GUI and multi-scanner engine.",
            category: "Phone Number Reconnaissance",
            stars: "13.8k+",
            language: "Go",
            features: ["Embedded Web GUI & REST API", "Numverify & OVH scanner modules", "Automated Google search dorks"]
        ),
        PhoneOsintItem(
            id: "libphonenumber",
            name: "Google libphonenumber",
            githubUrl: "https://github.com/google/libphonenumber",
            description: "Google's authoritative library for parsing, formatting, carrier mapping, and geocoding international phone numbers.",
            category: "Parsing & Geocoding",
            stars: "17.4k+",
            language: "Java / C++ / JS",
            features: ["Authoritative E.164 format rules", "Carrier mapping & region geocoding", "Timezone identification per prefix"]
        ),
        PhoneOsintItem(
            id: "moriarty",
            name: "Moriarty Project",
            githubUrl: "https://github.com/AzizKaba/Moriarty-Project",
            description: "Phone number investigation tool scanning public registries, social footprints, and spam databases.",
            category: "Phone Intelligence & Social Lookup",
            stars: "800+",
            language: "Python",
            features: ["Social footprinting across platforms", "Spam & fraud reputation scoring", "Carrier origin discovery"]
        ),
        PhoneOsintItem(
            id: "ignorant",
            name: "Ignorant",
            githubUrl: "https://github.com/megadose/ignorant",
            description: "Checks if a phone number is registered on major social platforms (Snapchat, Instagram) without sending SMS.",
            category: "Account Association",
            stars: "1.9k+",
            language: "Python",
            features: ["No SMS alert sent to target", "Multi-platform presence audit", "Clean JSON export"]
        ),
        PhoneOsintItem(
            id: "truecallerjs",
            name: "TruecallerJS",
            githubUrl: "https://github.com/sumithemmadi/truecallerjs",
            description: "Open-source Node.js library & CLI to search and identify caller ID, names, and spam scores via Truecaller.",
            category: "Caller ID & Directory",
            stars: "1.4k+",
            language: "JavaScript",
            features: ["Global Truecaller public directory search", "Caller name, location & carrier", "CLI and Node.js API"]
        ),
        PhoneOsintItem(
            id: "blackbird",
            name: "Blackbird Phone OSINT",
            githubUrl: "https://github.com/p1ngul1n0/blackbird",
            description: "High-speed OSINT search engine querying user profiles, phone references, and account associations across 500+ sites.",
            category: "Fast Correlated OSINT",
            stars: "4.2k+",
            language: "Python",
            features: ["Async multithreaded scanning", "Phone & username correlation", "PDF and CSV exports"]
        ),
        PhoneOsintItem(
            id: "ghosttrack",
            name: "GhostTrack",
            githubUrl: "https://github.com/HunxByts/GhostTrack",
            description: "Phone number and IP geolocation reconnaissance tool identifying country, region, carrier, and timezone.",
            category: "Carrier & Country Telemetry",
            stars: "1.1k+",
            language: "Python",
            features: ["Country and regional mapping", "Carrier network analysis", "Terminal interface"]
        ),
        PhoneOsintItem(
            id: "numverify",
            name: "Numverify OSINT CLI",
            githubUrl: "https://github.com/sundowndev/numverify",
            description: "CLI tool and API client for global phone number validation, carrier detection, and line-type classification.",
            category: "Carrier & Line Classification",
            stars: "300+",
            language: "Go / Shell",
            features: ["VoIP vs Mobile vs Landline detection", "Global carrier resolution", "High accuracy checks"]
        ),
        PhoneOsintItem(
            id: "phonesploit",
            name: "PhoneSploit-Pro",
            githubUrl: "https://github.com/AzeemIdrisi/PhoneSploit-Pro",
            description: "All-in-one phone reconnaissance and Android device auditing framework with terminal dashboard.",
            category: "Mobile Auditing & Network",
            stars: "3.7k+",
            language: "Python",
            features: ["ADB network discovery", "SIM state inspection", "Batch network testing"]
        ),
        PhoneOsintItem(
            id: "twilio-lookup",
            name: "Twilio Lookup API",
            githubUrl: "https://github.com/twilio/twilio-node",
            description: "Enterprise carrier metadata, line-type intelligence, caller name (CNAM), and SIM-swap fraud risk scoring library.",
            category: "Enterprise Carrier Intelligence",
            stars: "1.2k+",
            language: "TypeScript / JS",
            features: ["Authoritative carrier lookup", "Caller Name (CNAM)", "SIM-swap risk scoring"]
        )
    ]

    public init() {}

    private func analyze(_ raw: String) {
        let digits = raw.filter { "0123456789".contains($0) }
        guard digits.count >= 7 else { return }

        let e164 = "+\(digits)"
        analyzedE164 = e164

        if digits.hasPrefix("1") {
            analyzedCountry = "🇺🇸/🇨🇦 United States / Canada (+1)"
        } else if digits.hasPrefix("44") {
            analyzedCountry = "🇬🇧 United Kingdom (+44)"
        } else if digits.hasPrefix("49") {
            analyzedCountry = "🇩🇪 Germany (+49)"
        } else if digits.hasPrefix("33") {
            analyzedCountry = "🇫🇷 France (+33)"
        } else if digits.hasPrefix("234") {
            analyzedCountry = "🇳🇬 Nigeria (+234)"
        } else if digits.hasPrefix("91") {
            analyzedCountry = "🇮🇳 India (+91)"
        } else {
            analyzedCountry = "🌐 International (\(e164))"
        }

        analyzedDorks = [
            ("Exact Match", "\"\(e164)\""),
            ("Social Footprint", "\"\(e164)\" site:facebook.com OR site:linkedin.com OR site:instagram.com"),
            ("Public Paste Dork", "\"\(e164)\" site:pastebin.com OR site:github.com"),
            ("Spam & Caller ID", "\"\(e164)\" \"spam\" OR \"scam\" OR \"caller id\"")
        ]
    }

    public var body: some View {
        NavigationStack {
            List {
                Section("🔍 Interactive Phone Intelligence Analyzer") {
                    VStack(alignment: .leading, spacing: 10) {
                        HStack {
                            TextField("Enter number (e.g. +1 555-0199)", text: $inputNumber)
                                .textFieldStyle(.roundedBorder)
                                .keyboardType(.phonePad)
                            Button("Analyze") {
                                analyze(inputNumber)
                            }
                            .buttonStyle(.borderedProminent)
                            .tint(.green)
                        }

                        if let e164 = analyzedE164 {
                            VStack(alignment: .leading, spacing: 6) {
                                Text("Normalized E.164: \(e164)")
                                    .font(.caption)
                                    .bold()
                                    .foregroundColor(.green)
                                if let country = analyzedCountry {
                                    Text("Country: \(country)")
                                        .font(.caption2)
                                }

                                Text("Google Intelligence Dorks:")
                                    .font(.caption2)
                                    .bold()
                                    .padding(.top, 4)

                                ForEach(analyzedDorks, id: \.title) { dork in
                                    HStack {
                                        Text("• \(dork.title)")
                                            .font(.caption2)
                                            .foregroundColor(.secondary)
                                        Spacer()
                                        if let encoded = dork.query.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
                                           let searchURL = URL(string: "https://www.google.com/search?q=\(encoded)") {
                                            Link("Search ↗", destination: searchURL)
                                                .font(.caption2)
                                                .foregroundColor(.green)
                                        }
                                    }
                                }
                            }
                            .padding(.top, 6)
                        }
                    }
                    .padding(.vertical, 4)
                }

                Section("Top 10 Phone OSINT & Tracking Projects") {
                    ForEach(projects) { project in
                        VStack(alignment: .leading, spacing: 8) {
                            HStack {
                                Image(systemName: "phone.badge.waveform")
                                    .foregroundColor(.green)
                                Text(project.name)
                                    .font(.headline)
                                    .bold()
                                Spacer()
                                Text("★ \(project.stars)")
                                    .font(.caption2)
                                    .bold()
                                    .padding(.horizontal, 6)
                                    .padding(.vertical, 2)
                                    .background(Color.yellow.opacity(0.2))
                                    .foregroundColor(.yellow)
                                    .cornerRadius(4)
                            }

                            Text(project.description)
                                .font(.caption)
                                .foregroundColor(.secondary)

                            HStack(spacing: 6) {
                                Text(project.category)
                                    .font(.caption2)
                                    .padding(.horizontal, 6)
                                    .padding(.vertical, 2)
                                    .background(Color.blue.opacity(0.15))
                                    .foregroundColor(.blue)
                                    .cornerRadius(4)
                                Text(project.language)
                                    .font(.caption2)
                                    .padding(.horizontal, 6)
                                    .padding(.vertical, 2)
                                    .background(Color.gray.opacity(0.15))
                                    .foregroundColor(.primary)
                                    .cornerRadius(4)
                            }

                            ForEach(project.features, id: \.self) { feat in
                                Text("• \(feat)")
                                    .font(.caption2)
                                    .foregroundColor(.secondary)
                            }

                            if let url = URL(string: project.githubUrl) {
                                Link(destination: url) {
                                    HStack {
                                        Image(systemName: "arrow.up.right.square")
                                        Text("View on GitHub")
                                    }
                                    .font(.caption)
                                    .bold()
                                    .foregroundColor(.green)
                                    .padding(.top, 4)
                                }
                            }
                        }
                        .padding(.vertical, 4)
                    }
                }
            }
            .navigationTitle("Top 10 Phone OSINT")
        }
    }
}
