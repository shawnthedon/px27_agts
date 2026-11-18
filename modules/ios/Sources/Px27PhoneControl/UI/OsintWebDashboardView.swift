import SwiftUI

struct WebOsintItem: Identifiable {
    let id: String
    let name: String
    let githubUrl: String
    let description: String
    let category: String
    let stars: String
    let language: String
    let features: [String]
}

/// SwiftUI Dashboard for Top 10 Web-based OSINT Projects from GitHub
public struct OsintWebDashboardView: View {
    private let projects: [WebOsintItem] = [
        WebOsintItem(
            id: "spiderfoot",
            name: "SpiderFoot",
            githubUrl: "https://github.com/smicallef/spiderfoot",
            description: "Automated OSINT collection engine with web UI for domains, IPs, emails, and phone numbers.",
            category: "Recon & Asset Discovery",
            stars: "14.5k+",
            language: "Python",
            features: ["200+ OSINT modules integrated", "Interactive Web GUI & REST API", "Passive DNS & WHOIS reconnaissance"]
        ),
        WebOsintItem(
            id: "sherlock",
            name: "Sherlock",
            githubUrl: "https://github.com/sherlock-project/sherlock",
            description: "Hunt down social media accounts by username across 400+ social platforms and websites simultaneously.",
            category: "Identity & Social Footprint",
            stars: "62k+",
            language: "Python",
            features: ["400+ social network target engines", "Tor routing support for privacy", "Zero false-positive detection"]
        ),
        WebOsintItem(
            id: "theharvester",
            name: "theHarvester",
            githubUrl: "https://github.com/laramies/theHarvester",
            description: "Gathers subdomains, email addresses, virtual hosts, open ports, and employee names from search engines & Shodan.",
            category: "Domain & Email Intelligence",
            stars: "11.5k+",
            language: "Python",
            features: ["Passive DNS & search harvesting", "Subdomain bruteforce & resolving", "HTML & XML reporting"]
        ),
        WebOsintItem(
            id: "osint-framework",
            name: "OSINT Framework",
            githubUrl: "https://github.com/lockfale/osint-framework",
            description: "Web-based tree navigation directory focusing on gathering information from free open source tools.",
            category: "Directory & Framework",
            stars: "16.8k+",
            language: "JavaScript / HTML",
            features: ["Interactive collapsible tree graph", "Covers IP, domain, username, phone", "Zero backend setup required"]
        ),
        WebOsintItem(
            id: "photon",
            name: "Photon",
            githubUrl: "https://github.com/s0md3v/Photon",
            description: "High-speed crawler designed for OSINT information extraction. Discovers URLs, emails, and secret API keys.",
            category: "Web Scraping & Crawler",
            stars: "14.2k+",
            language: "Python",
            features: ["Threaded asynchronous crawling", "Regex extraction for secret keys", "Visual site graph generation"]
        ),
        WebOsintItem(
            id: "recon-ng",
            name: "Recon-ng",
            githubUrl: "https://github.com/lanmaster53/recon-ng",
            description: "Full-featured web reconnaissance framework with modular database architecture and 100+ modules.",
            category: "Web Reconnaissance Framework",
            stars: "4.3k+",
            language: "Python",
            features: ["Metasploit-like interactive environment", "SQLite database schema", "100+ community modules"]
        ),
        WebOsintItem(
            id: "amass",
            name: "OWASP Amass",
            githubUrl: "https://github.com/owasp-amass/amass",
            description: "In-depth attack surface mapping and external asset discovery using active and passive reconnaissance.",
            category: "Attack Surface Mapping",
            stars: "11.2k+",
            language: "Go",
            features: ["Graph database storage (Neo4j/Postgres)", "BGP routing & ASN enumeration", "OWASP enterprise standards"]
        ),
        WebOsintItem(
            id: "sn1per",
            name: "Sn1per",
            githubUrl: "https://github.com/1N3/Sn1per",
            description: "Automated reconnaissance scanner and attack surface management platform with unified web dashboard.",
            category: "Consolidated Attack Surface",
            stars: "7.8k+",
            language: "Shell / Python",
            features: ["Automated scanning workflows", "Integrates Amass, Nmap, Nuclei", "Web GUI workspace management"]
        ),
        WebOsintItem(
            id: "social-analyzer",
            name: "Social Analyzer",
            githubUrl: "https://github.com/qeeqbox/social-analyzer",
            description: "API, CLI, and Web App for analyzing and finding user profiles across 1000+ social media websites.",
            category: "Social Media Profiling",
            stars: "12.8k+",
            language: "Node.js / Python",
            features: ["Supports 1000+ sites", "Web GUI with filters & tables", "Automated profile inspection"]
        ),
        WebOsintItem(
            id: "holehe",
            name: "Holehe",
            githubUrl: "https://github.com/megadose/holehe",
            description: "Fast OSINT tool that checks if an email is attached to an account on 120+ sites using recovery endpoints.",
            category: "Account Discovery & Email OSINT",
            stars: "5.4k+",
            language: "Python",
            features: ["Audits 120+ platforms", "Zero target notification triggered", "Async high-speed requests"]
        )
    ]

    public init() {}

    public var body: some View {
        NavigationStack {
            List(projects) { project in
                VStack(alignment: .leading, spacing: 8) {
                    HStack {
                        Image(systemName: "network")
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
            .navigationTitle("Top 10 Web OSINT")
        }
    }
}
