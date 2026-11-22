# px27m Universal Phone Control Module

A reusable, cross-platform device-operation platform enabling authorized px27m AI agents to automatically discover device capabilities and operate them through a single, secure, intelligent interface.

---

## Central Principle

> **px27m agents automatically learn the connected device's available capabilities at runtime, then operate those capabilities through one secure, intelligent interface.**

The phone-control module is designed as a reusable **px27m platform module** rather than a feature hardwired into Copilot alone. Any authorized agent (Observer, Assistant, Operator, Power User) can discover capabilities advertised by a companion device and execute structured commands adhering to strict platform sandboxing.

---

## Architectural Configuration

- **Hybrid Connectivity:** Direct local LAN communication via lightweight embedded server (Android: port 27027, iOS: port 27028) for sub-10ms latency when co-located; authenticated remote connection via persistent WebSocket connection to the px27m Gateway for WAN/NAT traversal.
- **Full Native Android Implementation:** Written in Kotlin 2.2, Android SDK 36, and Jetpack Compose. Leverages `AccessibilityService`, `NotificationListenerService`, `ForegroundService`, and intent bridges for Tasker and MacroDroid.
- **Full Native iOS Implementation:** Written in Swift 6 and SwiftUI. Employs modern `App Intents`, `App Shortcuts`, `CoreLocation`, `AVFoundation`, `Vision`, and `CoreMotion`. Enforces truthful sandboxing boundaries without false claims.
- **Two Independently Installable Companion Apps:** Android APK built and signed via Gradle; iOS companion application built via Swift Package Manager and Xcode.
- **Universal Protocol:** Language-neutral JSON Schemas and TypeScript/JavaScript protocol library governing manifests, commands, results, risk levels (0–4), and real-time device events.

---

## Repository Structure

```
px27_agts/
│
├── packages/
│   └── phone-control-protocol/       # Universal Protocol Schemas & Types
│       ├── schemas/                  # JSON Schemas (capability, command, result, event)
│       ├── src/                      # Constants, validators, risk levels, autonomy profiles
│       └── tests/                    # Protocol compliance test suite
│
├── apps/
│   └── gateway/                      # Reusable px27m Platform Module
│       ├── src/phone-control/        # Device registry, capability registry, policy engine,
│       │                             # pairing manager, audit engine, command router
│       └── test/                     # Gateway unit & integration tests
│
├── app/                              # Full Native Android Companion Application
│   ├── src/main/
│   │   ├── AndroidManifest.xml       # Services, receivers, permissions
│   │   ├── kotlin/com/pocketstrike/app/
│   │   │   ├── core/protocol/        # Kotlin models matching universal protocol
│   │   │   ├── services/             # Px27AccessibilityService, Px27NotificationListener,
│   │   │   │                         # Px27ForegroundService
│   │   │   ├── capabilities/         # Real native handlers for all 15 domains
│   │   │   ├── engine/               # 10-Component Power-User Execution Engine
│   │   │   ├── discovery/            # Dynamic CapabilityDiscoveryEngine
│   │   │   ├── connectivity/         # HybridConnectivityManager (Local Server + WSS Client)
│   │   │   └── ui/                   # Jetpack Compose Capability Explorer & Pairing UI
│   │   └── res/                      # Layouts, drawables, accessibility service XML
│   └── build.gradle.kts
│
├── modules/
│   └── ios/                          # Full Native Swift iOS Companion Application
│       ├── Package.swift             # Swift 6 package definition
│       ├── Sources/
│       │   ├── Px27PhoneControl/
│       │   │   ├── Protocol/         # Swift Codable protocol models & constants
│       │   │   ├── Discovery/        # iOS CapabilityDiscovery (truthful sandboxing)
│       │   │   ├── Capabilities/     # Native iOS capability domain handlers
│       │   │   ├── Engine/           # Swift Concurrency 10-Component Execution Engine
│       │   │   ├── Intents/          # Modern Apple AppIntents & AppShortcuts
│       │   │   ├── Connectivity/     # Hybrid connectivity (NWListener & WebSocket task)
│       │   │   └── UI/               # SwiftUI Capability Explorer & Pairing Views
│       │   └── Px27PhoneControlApp/  # SwiftUI Application entry point
│       └── Tests/                    # XCTest suite for protocol & sandbox verification
│
└── docs/
    ├── capability-catalogue.md       # Complete 15-domain matrix & platform comparison
    └── progress-tracker.md           # Authoritative capability status tracker
```

---

## Universal Command Protocol

### Command Structure
Agents dispatch standardized commands adhering to `@px27m/phone-control-protocol`:

```json
{
  "command_id": "cmd_001",
  "capability": "device.application.launch",
  "parameters": {
    "package_name": "com.example.app"
  },
  "execution": {
    "mode": "foreground",
    "timeout_ms": 15000,
    "verify_result": true
  },
  "authorization": {
    "agent_id": "agent_strike_01",
    "autonomy_profile": "operator",
    "approval_token": "token_if_high_risk"
  }
}
```

### Verified Execution Result
Execution engines confirm real device state before returning:

```json
{
  "command_id": "cmd_001",
  "status": "completed",
  "platform": "android",
  "capability": "device.application.launch",
  "result": {
    "launched": true,
    "package_name": "com.example.app"
  },
  "verification": {
    "verified": true,
    "verification_method": "intent_dispatched_to_activity_manager"
  },
  "error": null,
  "duration_ms": 320
}
```

---

## Risk Levels & Autonomy Ceilings

| Level | Classification | Default Rule |
| :---: | :--- | :--- |
| **0** | Non-sensitive Info | Auto-executable by all agent profiles (Observer, Assistant, Operator, Power User). |
| **1** | Ordinary Operations | Policy controlled. Permitted for Assistant, Operator, Power User. |
| **2** | Sensitive Data | SMS dispatch, microphone recording, document read/write. Permitted for Operator and Power User. |
| **3** | High-Impact Operations | Screen click/gesture injection, direct calls, external automation. Requires explicit approval token. |
| **4** | Critical Operations | Emergency session termination, device administration. Mandatory user confirmation every time. |

---

## Verification & Testing

### 1. Protocol Validation Suite
```bash
cd packages/phone-control-protocol
npm test
```

### 2. Platform Gateway Integration Suite
```bash
cd apps/gateway
npm test
```

### 3. Android Companion Build
```bash
./gradlew assembleDebug
```

---

## Truthful Platform Boundaries
In accordance with foundational specification principles, iOS sandboxing limits are strictly observed:
- Global screen inspection and arbitrary tap injection across external apps are restricted on iOS and truthfully reported as `"available": false, "reason": "unsupported_platform_sandbox"`.
- Rich iOS automation is provided legitimately through Apple **App Intents**, **Shortcuts workflows**, and **Universal Links**.
