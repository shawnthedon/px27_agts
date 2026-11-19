/**
 * OSINT Modules & Catalog
 * Top 10 Web-based OSINT Projects & Top 10 Phone Tracking OSINT Projects from GitHub
 * In accordance with modular, extensible dashboard requirements.
 */

export const TOP_10_WEB_OSINT_PROJECTS = [
  {
    id: "spiderfoot",
    name: "SpiderFoot",
    github_url: "https://github.com/smicallef/spiderfoot",
    description: "Open source intelligence (OSINT) automation tool with web UI. Gathers intelligence on IP addresses, domain names, e-mail addresses, and phone numbers.",
    category: "Reconnaissance & Asset Discovery",
    stars: "14.5k+",
    language: "Python",
    license: "MIT",
    key_features: [
      "Web-based interactive GUI & REST API",
      "Over 200 OSINT modules integrated",
      "Network footprinting & domain discovery",
      "Passive DNS and WHOIS reconnaissance"
    ],
    docker_image: "smicallef/spiderfoot:latest"
  },
  {
    id: "sherlock",
    name: "Sherlock",
    github_url: "https://github.com/sherlock-project/sherlock",
    description: "Hunt down social media accounts by username across 400+ social platforms and websites simultaneously with high-speed async requests.",
    category: "Identity & Social Footprint",
    stars: "62k+",
    language: "Python",
    license: "MIT",
    key_features: [
      "Over 400 social network target engines",
      "Tor routing support for privacy",
      "Zero-false-positive rate matching logic",
      "JSON, CSV, and CLI outputs"
    ],
    docker_image: "sherlock/sherlock"
  },
  {
    id: "theharvester",
    name: "theHarvester",
    github_url: "https://github.com/laramies/theHarvester",
    description: "Simple yet powerful tool for gathering subdomains, email addresses, virtual hosts, open ports, and employee names from multiple public data sources.",
    category: "Domain & Email Intelligence",
    stars: "11.5k+",
    language: "Python",
    license: "GPL-2.0",
    key_features: [
      "Searches Bing, Baidu, Shodan, Censys, Hunter",
      "Passive DNS harvesting",
      "Automated subdomain enumeration",
      "HTML & XML reports"
    ],
    docker_image: "laramies/theharvester"
  },
  {
    id: "osint-framework",
    name: "OSINT Framework",
    github_url: "https://github.com/lockfale/osint-framework",
    description: "Web-based tree navigation directory focusing on gathering information from free open source tools or resources. Categorizes hundreds of intelligence engines.",
    category: "Directory & Framework",
    stars: "16.8k+",
    language: "JavaScript / HTML",
    license: "GPL-3.0",
    key_features: [
      "Interactive collapsible mind-map visualization",
      "Covers IP, domain, username, phone, darkweb",
      "Zero backend setup required",
      "Continuously curated intelligence sources"
    ],
    docker_image: "web-hosted"
  },
  {
    id: "photon",
    name: "Photon",
    github_url: "https://github.com/s0md3v/Photon",
    description: "Incredibly fast web crawler designed for OSINT information extraction. Extracts URLs with parameters, emails, social media accounts, Amazon S3 buckets, and secrets.",
    category: "Web Scraping & Crawler",
    stars: "14.2k+",
    language: "Python",
    license: "GPL-3.0",
    key_features: [
      "Threaded asynchronous crawling",
      "Regex extraction for secret keys & API tokens",
      "PDF, PNG, and document discovery",
      "Generates visual graph of website architecture"
    ],
    docker_image: "s0md3v/photon"
  },
  {
    id: "recon-ng",
    name: "Recon-ng",
    github_url: "https://github.com/lanmaster53/recon-ng",
    description: "Full-featured web reconnaissance framework written in Python with a modular database architecture, command completion, and extensive API integrations.",
    category: "Web Reconnaissance Framework",
    stars: "4.3k+",
    language: "Python",
    license: "GPL-3.0",
    key_features: [
      "Metasploit-like interactive CLI environment",
      "Integrated SQLite database schema",
      "Modular marketplace with 100+ community modules",
      "Built-in reporting and analytics"
    ],
    docker_image: "lanmaster53/recon-ng"
  },
  {
    id: "amass",
    name: "OWASP Amass",
    github_url: "https://github.com/owasp-amass/amass",
    description: "In-depth attack surface mapping and external asset discovery using open source information gathering and active reconnaissance techniques.",
    category: "Attack Surface Mapping",
    stars: "11.2k+",
    language: "Go",
    license: "Apache-2.0",
    key_features: [
      "Graph database storage (PostgreSQL/Neo4j)",
      "DNS active bruteforcing and recursive resolving",
      "BGP routing table queries and ASN mapping",
      "OWASP enterprise-grade standards"
    ],
    docker_image: "caffix/amass"
  },
  {
    id: "sn1per",
    name: "Sn1per",
    github_url: "https://github.com/1N3/Sn1per",
    description: "Automated reconnaissance scanner and attack surface management platform with unified web dashboard for continuous external asset monitoring.",
    category: "Consolidated Attack Surface",
    stars: "7.8k+",
    language: "Shell / Python",
    license: "GPL-3.0",
    key_features: [
      "Automated scanning workflows",
      "HTML vulnerability & intelligence reports",
      "Integrates Amass, Nmap, Sublist3r, Nuclei",
      "Web GUI dashboard with workspace tabs"
    ],
    docker_image: "sn1persecurity/sn1per"
  },
  {
    id: "social-analyzer",
    name: "Social Analyzer",
    github_url: "https://github.com/qeeqbox/social-analyzer",
    description: "API, CLI, and Web App for analyzing and finding a person's profile across 1000+ social media websites and online services with screenshot captures.",
    category: "Social Media Profiling",
    stars: "12.8k+",
    language: "Node.js / Python",
    license: "Apache-2.0",
    key_features: [
      "Supports 1000+ websites and platforms",
      "Web GUI with search filters & interactive tables",
      "Automated screenshot inspection",
      "Metadata and profile age extraction"
    ],
    docker_image: "qeeqbox/social-analyzer"
  },
  {
    id: "holehe",
    name: "Holehe",
    github_url: "https://github.com/megadose/holehe",
    description: "Fast OSINT tool that checks if an email is attached to an account on 120+ sites (Twitter, Instagram, LinkedIn, etc.) using forgotten password endpoints.",
    category: "Account Discovery & Email OSINT",
    stars: "5.4k+",
    language: "Python",
    license: "GPL-3.0",
    key_features: [
      "Audits 120+ platforms in seconds",
      "Does not trigger user notifications or alerts",
      "Simple CLI and clean JSON output",
      "Asynchronous high-concurrency requests"
    ],
    docker_image: "megadose/holehe"
  }
];

export const TOP_10_PHONE_OSINT_PROJECTS = [
  {
    id: "phoneinfoga",
    name: "PhoneInfoga",
    github_url: "https://github.com/sundowndev/phoneinfoga",
    description: "One of the most advanced OSINT tools to scan international phone numbers. Discovers carrier, line type, country, and Google search engine dorks with web GUI.",
    category: "Phone Number Reconnaissance",
    stars: "13.8k+",
    language: "Go",
    license: "GPL-3.0",
    key_features: [
      "Embedded modern Vue.js Web GUI",
      "Multi-scanner engine (Numverify, OVH, Google Dorks)",
      "Carrier, line type, country, and timezone detection",
      "RESTful API server built-in"
    ],
    docker_image: "sundowndev/phoneinfoga:latest"
  },
  {
    id: "libphonenumber",
    name: "Google libphonenumber",
    github_url: "https://github.com/google/libphonenumber",
    description: "Google's authoritative common library for parsing, formatting, and storing international phone numbers. Implements real-time carrier mapping and geocoding.",
    category: "Parsing, Geocoding & Validation",
    stars: "17.4k+",
    language: "Java / C++ / JS",
    license: "Apache-2.0",
    key_features: [
      "Authoritative global dialing format rules",
      "E.164, National, and International formatting",
      "Carrier mapping and region geocoding",
      "Timezone identification per prefix"
    ],
    docker_image: "google/libphonenumber"
  },
  {
    id: "moriarty-project",
    name: "Moriarty Project",
    github_url: "https://github.com/AzizKaba/Moriarty-Project",
    description: "Comprehensive OSINT investigation tool for phone numbers. Pulls public registry information, social footprints, and spam reputation databases.",
    category: "Phone Intelligence & Social Lookup",
    stars: "800+",
    language: "Python",
    license: "GPL-3.0",
    key_features: [
      "Phone footprinting on social platforms",
      "Spam score and fraud database checks",
      "Carrier and regional origin identification",
      "Generates structured intelligence dossier"
    ],
    docker_image: "standalone-python"
  },
  {
    id: "ignorant",
    name: "Ignorant",
    github_url: "https://github.com/megadose/ignorant",
    description: "OSINT tool to discover if a phone number is registered across major online platforms like Snapchat, Instagram, Amazon, and PayPal without sending SMS.",
    category: "Account Association & Validation",
    stars: "1.9k+",
    language: "Python",
    license: "GPL-3.0",
    key_features: [
      "Checks account presence on major services",
      "No SMS verification trigger sent to target",
      "Fast async validation queries",
      "JSON formatted results"
    ],
    docker_image: "megadose/ignorant"
  },
  {
    id: "truecallerjs",
    name: "TruecallerJS",
    github_url: "https://github.com/sumithemmadi/truecallerjs",
    description: "Open-source Node.js library and CLI tool to search and identify caller ID, names, addresses, and spam reports via Truecaller's public directory API.",
    category: "Caller ID & Public Directory",
    stars: "1.4k+",
    language: "JavaScript",
    license: "MIT",
    key_features: [
      "Searches global Truecaller directory",
      "Identifies caller name, spam score, and provider",
      "CLI, Node.js API, and JSON output",
      "Supports installation via npm"
    ],
    docker_image: "npm:truecallerjs"
  },
  {
    id: "blackbird",
    name: "Blackbird Phone OSINT",
    github_url: "https://github.com/p1ngul1n0/blackbird",
    description: "High-speed OSINT search engine querying user profiles, phone references, and account associations across over 500 websites and online registries.",
    category: "Fast Correlated OSINT",
    stars: "4.2k+",
    language: "Python",
    license: "GPL-3.0",
    key_features: [
      "Fast asynchronous multithreaded scanning",
      "Correlates phone metadata with usernames",
      "Exports to PDF, CSV, and SQLite",
      "Minimal false positive verification"
    ],
    docker_image: "p1ngul1n0/blackbird"
  },
  {
    id: "ghosttrack",
    name: "GhostTrack",
    github_url: "https://github.com/HunxByts/GhostTrack",
    description: "Phone number and IP address geolocation reconnaissance tool. Pinpoints country, region code, cellular network carrier, and timezone information.",
    category: "Carrier & Country Telemetry",
    stars: "1.1k+",
    language: "Python",
    license: "MIT",
    key_features: [
      "Country, city, and region identification",
      "Carrier network prefix analysis",
      "Latitude/longitude regional centroid mapping",
      "Interactive command line interface"
    ],
    docker_image: "standalone-python"
  },
  {
    id: "numverify",
    name: "Numverify OSINT CLI",
    github_url: "https://github.com/sundowndev/numverify",
    description: "Command-line tool and API client for global phone number validation, international carrier detection, and line-type classification (VoIP, mobile, landline).",
    category: "Carrier & Line-Type Classification",
    stars: "300+",
    language: "Go / Shell",
    license: "MIT",
    key_features: [
      "Identifies VoIP numbers vs physical SIMs",
      "Global carrier detection (Verizon, Vodafone, MTN, etc.)",
      "Country code and national prefix validation",
      "High accuracy validation API"
    ],
    docker_image: "standalone-binary"
  },
  {
    id: "phonesploit-pro",
    name: "PhoneSploit-Pro",
    github_url: "https://github.com/AzeemIdrisi/PhoneSploit-Pro",
    description: "All-in-one phone reconnaissance, Android Debug Bridge network scanner, and device auditing tool with an intuitive terminal interface.",
    category: "Mobile Auditing & Network Discovery",
    stars: "3.7k+",
    language: "Python",
    license: "GPL-3.0",
    key_features: [
      "Discovers exposed network ADB interfaces",
      "Device hardware and SIM state audit",
      "Network packet and socket inspection",
      "Automated batch testing"
    ],
    docker_image: "standalone-python"
  },
  {
    id: "twilio-lookup",
    name: "Twilio Lookup API",
    github_url: "https://github.com/twilio/twilio-node",
    description: "Enterprise carrier metadata, line-type intelligence, caller name (CNAM), and SIM-swap fraud risk scoring library.",
    category: "Enterprise Carrier & Fraud Intelligence",
    stars: "1.2k+",
    language: "TypeScript / JS",
    license: "MIT",
    key_features: [
      "Authoritative carrier and MNC/MCC lookup",
      "Caller Name (CNAM) verification",
      "SIM-swap and line status fraud intelligence",
      "Worldwide E.164 carrier routing verification"
    ],
    docker_image: "npm:twilio"
  }
];

/**
 * Intelligent Phone Number OSINT Analyzer
 * Normalizes input, parses international dial codes, extracts country/carrier metadata,
 * and generates search engine dorks for phone investigation.
 */
export function analyzePhoneNumber(rawInput) {
  if (!rawInput || typeof rawInput !== "string") {
    return { valid: false, error: "Please enter a valid phone number string" };
  }

  const cleaned = rawInput.trim();
  const digitsOnly = cleaned.replace(/[^0-9]/g, "");

  // International Dial Code Prefixes Mapping
  const dialPrefixes = [
    { prefix: "1", country: "United States / Canada", code: "US/CA", flag: "🇺🇸/🇨🇦", format: "+1 (XXX) XXX-XXXX" },
    { prefix: "44", country: "United Kingdom", code: "GB", flag: "🇬🇧", format: "+44 XXXX XXXXXX" },
    { prefix: "49", country: "Germany", code: "DE", flag: "🇩🇪", format: "+49 XXX XXXXXXXX" },
    { prefix: "33", country: "France", code: "FR", flag: "🇫🇷", format: "+33 X XX XX XX XX" },
    { prefix: "234", country: "Nigeria", code: "NG", flag: "🇳🇬", format: "+234 XXX XXX XXXX" },
    { prefix: "91", country: "India", code: "IN", flag: "🇮🇳", format: "+91 XXXXX XXXXX" },
    { prefix: "81", country: "Japan", code: "JP", flag: "🇯🇵", format: "+81 XX XXXX XXXX" },
    { prefix: "86", country: "China", code: "CN", flag: "🇨🇳", format: "+86 XXX XXXX XXXX" },
    { prefix: "61", country: "Australia", code: "AU", flag: "🇦🇺", format: "+61 X XXXX XXXX" },
    { prefix: "55", country: "Brazil", code: "BR", flag: "🇧🇷", format: "+55 XX XXXXX-XXXX" },
    { prefix: "27", country: "South Africa", code: "ZA", flag: "🇿🇦", format: "+27 XX XXX XXXX" },
    { prefix: "971", country: "United Arab Emirates", code: "AE", flag: "🇦🇪", format: "+971 XX XXX XXXX" }
  ];

  let matchedCountry = { country: "International / Unknown", code: "INT", flag: "🌐", format: "+XXX XXXXXXXX" };
  for (const p of dialPrefixes) {
    if (digitsOnly.startsWith(p.prefix)) {
      matchedCountry = p;
      break;
    }
  }

  const e164 = `+${digitsOnly}`;

  // Generated Google OSINT Dorks
  const googleDorks = [
    { title: "Exact E.164 Match", query: `"${e164}"` },
    { title: "National Number Match", query: `"${digitsOnly}"` },
    { title: "Social & Contact Dork", query: `"${e164}" OR "${digitsOnly}" site:facebook.com OR site:linkedin.com OR site:instagram.com` },
    { title: "Directory & Paste Dork", query: `"${e164}" site:pastebin.com OR site:github.com OR site:truecaller.com` },
    { title: "Carrier & Spam Dork", query: `"${e164}" "spam" OR "scam" OR "caller id"` }
  ];

  return {
    valid: digitsOnly.length >= 7 && digitsOnly.length <= 15,
    raw_input: rawInput,
    digits_only: digitsOnly,
    e164_normalized: e164,
    country: matchedCountry.country,
    country_code: matchedCountry.code,
    flag: matchedCountry.flag,
    standard_format: matchedCountry.format,
    estimated_line_type: digitsOnly.length > 10 ? "Mobile / Cellular" : "Fixed / Landline",
    recommended_tools: [
      { name: "PhoneInfoga", link: "https://github.com/sundowndev/phoneinfoga", reason: "Run automated multi-scanner recon" },
      { name: "Google libphonenumber", link: "https://github.com/google/libphonenumber", reason: "Validate carrier & timezone metadata" },
      { name: "TruecallerJS", link: "https://github.com/sumithemmadi/truecallerjs", reason: "Check caller ID and spam reputation" }
    ],
    osint_dorks: googleDorks
  };
}
