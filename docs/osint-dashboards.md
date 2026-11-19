# px27m OSINT Dashboards & Intelligence Modules

This document details the modular architecture, data contracts, and integration matrix for the **Top 10 Web-based OSINT Projects** and **Top 10 Phone Tracking & Intelligence Projects** from GitHub integrated into the px27m platform.

---

## 1. Architecture & Modular Design

The OSINT subsystem is integrated across three layers:
1. **Platform Gateway (`apps/gateway`):**
   - Headless REST API endpoints (`/api/osint/web`, `/api/osint/phone`, `/api/osint/phone/analyze`) serving structured project metadata and live phone analysis.
   - Interactive HTML/CSS/JS dashboard served on `GET /` and `GET /dashboard`.
2. **Android Companion App (`app/`):**
   - Native Jetpack Compose screens (`OsintWebDashboardScreen.kt` and `OsintPhoneDashboardScreen.kt`) with interactive E.164 analyzer and external GitHub intents.
3. **iOS Companion App (`modules/ios/`):**
   - Native SwiftUI views (`OsintWebDashboardView.swift` and `OsintPhoneDashboardView.swift`) with live search dork launchers and Safari integration.

---

## 2. Top 10 Web-Based OSINT Projects from GitHub

| Project | GitHub Repository | Category | Language | Stars | Key Capabilities |
| :--- | :--- | :--- | :---: | :---: | :--- |
| **SpiderFoot** | [`smicallef/spiderfoot`](https://github.com/smicallef/spiderfoot) | Recon & Asset Discovery | Python | 14.5k+ | Automated footprinting, 200+ modules, Web GUI & REST API, passive DNS/WHOIS. |
| **Sherlock** | [`sherlock-project/sherlock`](https://github.com/sherlock-project/sherlock) | Social Footprint | Python | 62k+ | Scans 400+ social platforms by username, Tor routing support, async requests. |
| **theHarvester** | [`laramies/theHarvester`](https://github.com/laramies/theHarvester) | Domain & Email Intel | Python | 11.5k+ | Passive subdomain harvesting, Shodan & Censys integration, employee discovery. |
| **OSINT Framework**| [`lockfale/osint-framework`](https://github.com/lockfale/osint-framework) | Directory & Framework | JS / HTML | 16.8k+ | Interactive collapsible mind-map directory covering hundreds of open data sources. |
| **Photon** | [`s0md3v/Photon`](https://github.com/s0md3v/Photon) | Web Scraping & Crawler | Python | 14.2k+ | High-speed multithreaded crawler, regex secret key extractor, site graph generator. |
| **Recon-ng** | [`lanmaster53/recon-ng`](https://github.com/lanmaster53/recon-ng) | Web Recon Framework | Python | 4.3k+ | Metasploit-style interactive CLI, SQLite database storage, 100+ marketplace modules. |
| **OWASP Amass** | [`owasp-amass/amass`](https://github.com/owasp-amass/amass) | Attack Surface Mapping | Go | 11.2k+ | Graph database visualization, BGP routing & ASN enumeration, active DNS bruteforce. |
| **Sn1per** | [`1N3/Sn1per`](https://github.com/1N3/Sn1per) | Consolidated Attack Surface | Shell/Python | 7.8k+ | Unified attack surface manager, automated scanning workflows, web GUI workspace. |
| **Social Analyzer**| [`qeeqbox/social-analyzer`](https://github.com/qeeqbox/social-analyzer) | Social Media Profiling | Node.js | 12.8k+ | Profiles users across 1000+ websites, automated screenshot inspection, REST API. |
| **Holehe** | [`megadose/holehe`](https://github.com/megadose/holehe) | Account & Email Intel | Python | 5.4k+ | Password-recovery endpoint auditing across 120+ sites without alerting the target. |

---

## 3. Top 10 Phone Tracking & OSINT Projects from GitHub

| Project | GitHub Repository | Category | Language | Stars | Key Capabilities |
| :--- | :--- | :--- | :---: | :---: | :--- |
| **PhoneInfoga** | [`sundowndev/phoneinfoga`](https://github.com/sundowndev/phoneinfoga) | Phone Reconnaissance | Go | 13.8k+ | Advanced international scanner, embedded Web GUI, Numverify/OVH modules, Google dorks. |
| **Google libphonenumber** | [`google/libphonenumber`](https://github.com/google/libphonenumber) | Parsing & Geocoding | Java/C++/JS | 17.4k+ | Authoritative global dialing rules, carrier mapping, region geocoding, timezone detection. |
| **Moriarty Project** | [`AzizKaba/Moriarty-Project`](https://github.com/AzizKaba/Moriarty-Project) | Phone & Social Intel | Python | 800+ | Public registry footprinting, spam & fraud database queries, carrier identification. |
| **Ignorant** | [`megadose/ignorant`](https://github.com/megadose/ignorant) | Account Presence Audit | Python | 1.9k+ | Detects phone registration on Snapchat, Instagram, Amazon without sending verification SMS. |
| **TruecallerJS** | [`sumithemmadi/truecallerjs`](https://github.com/sumithemmadi/truecallerjs) | Caller ID & Directory | JS | 1.4k+ | Global Truecaller public directory search, caller name resolution, Node.js API & CLI. |
| **Blackbird** | [`p1ngul1n0/blackbird`](https://github.com/p1ngul1n0/blackbird) | Fast Correlated OSINT | Python | 4.2k+ | Multithreaded identity resolution correlating phone references with usernames on 500+ sites. |
| **GhostTrack** | [`HunxByts/GhostTrack`](https://github.com/HunxByts/GhostTrack) | Carrier & Country Telemetry | Python | 1.1k+ | Country/city geolocation lookup, carrier network prefix analysis, terminal UI. |
| **Numverify CLI** | [`sundowndev/numverify`](https://github.com/sundowndev/numverify) | Carrier & Line Classification | Go / Shell | 300+ | Detects VoIP vs physical mobile SIM vs landline, carrier resolution, national formatting. |
| **PhoneSploit-Pro** | [`AzeemIdrisi/PhoneSploit-Pro`](https://github.com/AzeemIdrisi/PhoneSploit-Pro) | Device & Network Audit | Python | 3.7k+ | ADB network interface scanner, SIM hardware inspection, automated diagnostic testing. |
| **Twilio Lookup API** | [`twilio/twilio-node`](https://github.com/twilio/twilio-node) | Enterprise Fraud & Carrier | TS / JS | 1.2k+ | Line status verification, caller name (CNAM), SIM-swap fraud risk scoring. |

---

## 4. Interactive Phone Number Intelligence Analyzer API

### Endpoint: `POST /api/osint/phone/analyze`
**Request Payload:**
```json
{
  "phone_number": "+44 7911 123456"
}
```

**Response Payload:**
```json
{
  "valid": true,
  "raw_input": "+44 7911 123456",
  "digits_only": "447911123456",
  "e164_normalized": "+447911123456",
  "country": "United Kingdom",
  "country_code": "GB",
  "flag": "🇬🇧",
  "standard_format": "+44 XXXX XXXXXX",
  "estimated_line_type": "Mobile / Cellular",
  "recommended_tools": [
    { "name": "PhoneInfoga", "link": "https://github.com/sundowndev/phoneinfoga", "reason": "Run automated multi-scanner recon" },
    { "name": "Google libphonenumber", "link": "https://github.com/google/libphonenumber", "reason": "Validate carrier & timezone metadata" }
  ],
  "osint_dorks": [
    { "title": "Exact E.164 Match", "query": "\"+447911123456\"" },
    { "title": "Social & Contact Dork", "query": "\"+447911123456\" site:facebook.com OR site:linkedin.com OR site:instagram.com" }
  ]
}
```
