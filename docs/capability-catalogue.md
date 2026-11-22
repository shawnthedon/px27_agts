# px27m Universal Phone Control Capability Catalogue

This catalogue defines the complete matrix of capabilities supported by the px27m platform across Android and iOS companion modules, adhering strictly to **truthful platform boundaries**.

## Capability Risk Tiers

| Level | Classification | Description & Default Handling |
| :---: | :--- | :--- |
| **0** | Non-sensitive Info | Non-sensitive read-only device telemetry. Auto-executable by all agents including Observer. |
| **1** | Ordinary Operations | Standard application launch, dialler preparation, volume adjustments. Permitted for Assistant, Operator, Power User. |
| **2** | Sensitive Data | SMS dispatch, microphone recording, document read/write, notifications. Permitted for Operator and Power User. |
| **3** | High-Impact Operations | System screen clicks/swipes, direct phone calls, external automation triggers. Requires Power User or explicit user approval token. |
| **4** | Critical Operations | Emergency session termination, device administration, destructive reset. Explicit user confirmation mandatory every time. |

---

## Complete 15-Domain Matrix

### 1. Screen & Interface Control (`device.screen.*`)
| Capability ID | Android Implementation | iOS Implementation | Risk |
| :--- | :--- | :--- | :---: |
| `device.screen.inspect` | `Px27AccessibilityService`: Inspects window node hierarchy, text, view IDs, and bounds. | **Unsupported by OS sandbox.** Reported truthfully as unavailable. | 2 |
| `device.screen.tap` | `Px27AccessibilityService`: Performs node click or gesture dispatch at coordinates. | **Unsupported by OS sandbox.** Reported truthfully as unavailable. | 3 |
| `device.screen.swipe` | `Px27AccessibilityService`: Dispatches directional swipe gesture path. | **Unsupported by OS sandbox.** Reported truthfully as unavailable. | 3 |
| `device.screen.input_text` | `Px27AccessibilityService`: Sets text in active focused input field. | **Unsupported by OS sandbox.** Reported truthfully as unavailable. | 2 |
| `device.screen.global_action` | `Px27AccessibilityService`: Dispatches Back, Home, Recents, Notifications. | **Unsupported by OS sandbox.** Reported truthfully as unavailable. | 2 |
| `device.screen.capture` | MediaProjection API / Service screenshot capture. | Companion-owned screenshot or Apple Shortcuts screenshot action. | 1 |

### 2. Application Management (`device.application.*`)
| Capability ID | Android Implementation | iOS Implementation | Risk |
| :--- | :--- | :--- | :---: |
| `device.application.list` | `PackageManager.getInstalledApplications()`. | Restricted by iOS sandbox (only declared schemes queryable). | 0 |
| `device.application.launch` | `PackageManager.getLaunchIntentForPackage()`. | URL schemes & Universal Links. | 1 |
| `device.application.deep_link` | `Intent(ACTION_VIEW, Uri.parse(...))`. | `UIApplication.shared.open(url)`. | 1 |

### 3. Device Information (`device.info.*`)
| Capability ID | Android Implementation | iOS Implementation | Risk |
| :--- | :--- | :--- | :---: |
| `device.info.system` | `Build` constants, CPU ABI, kernel version. | `ProcessInfo`, `UIDevice`. | 0 |
| `device.info.battery` | `BatteryManager` sticky broadcast. | `UIDevice.batteryLevel` & `batteryState`. | 0 |
| `device.info.storage` | `StatFs(Environment.getDataDirectory())`. | `FileManager` resource values for volume capacity. | 0 |

### 4. Connectivity (`device.connectivity.*`)
| Capability ID | Android Implementation | iOS Implementation | Risk |
| :--- | :--- | :--- | :---: |
| `device.connectivity.status` | `ConnectivityManager` network capabilities. | `NWPathMonitor` network status. | 0 |
| `device.connectivity.wifi_settings` | `Settings.ACTION_WIFI_SETTINGS`. | `UIApplication.openSettingsURLString`. | 1 |

### 5. Telephony & SIM (`device.telephony.*`)
| Capability ID | Android Implementation | iOS Implementation | Risk |
| :--- | :--- | :--- | :---: |
| `device.telephony.dial` | `Intent(ACTION_DIAL, tel:...)`. | `openURL(tel:...)`. | 1 |
| `device.telephony.call` | `Intent(ACTION_CALL, tel:...)` (requires CALL_PHONE). | Restricted (requires user tap confirmation). | 3 |

### 6. SMS & Messaging (`device.messaging.*`)
| Capability ID | Android Implementation | iOS Implementation | Risk |
| :--- | :--- | :--- | :---: |
| `device.messaging.send_sms` | `SmsManager.sendTextMessage(...)`. | Restricted by iOS (requires message composition UI). | 2 |
| `device.messaging.share` | `Intent(ACTION_SEND)` share sheet. | `UIActivityViewController` share sheet. | 1 |

### 7. Audio & Speech (`device.audio.*`)
| Capability ID | Android Implementation | iOS Implementation | Risk |
| :--- | :--- | :--- | :---: |
| `device.audio.volume` | `AudioManager.getStreamVolume` / `setStreamVolume`. | Restricted (hardware buttons / MPVolumeView). | 1 |
| `device.audio.tts` | `android.speech.tts.TextToSpeech`. | `AVSpeechSynthesizer`. | 1 |
| `device.audio.record` | `AudioRecord` / `MediaRecorder`. | `AVAudioRecorder`. | 2 |

### 8. Camera & Vision (`device.camera.*`)
| Capability ID | Android Implementation | iOS Implementation | Risk |
| :--- | :--- | :--- | :---: |
| `device.camera.torch` | `CameraManager.setTorchMode(...)`. | `AVCaptureDevice.setTorchMode(...)`. | 1 |
| `device.camera.scan_qr` | Google ML Kit Barcode Scanning. | `Vision.VNDetectBarcodesRequest`. | 2 |
| `device.camera.ocr` | Google ML Kit Text Recognition. | `Vision.VNRecognizeTextRequest`. | 2 |

### 9. Storage & Files (`device.storage.*`)
| Capability ID | Android Implementation | iOS Implementation | Risk |
| :--- | :--- | :--- | :---: |
| `device.storage.list_files` | App files directory & Storage Access Framework. | App sandbox `Documents` directory. | 1 |
| `device.storage.read_file` | File read with path sanitization. | File read from sandbox documents. | 2 |
| `device.storage.write_file` | Atomic file write with stat verification. | Atomic file write in sandbox documents. | 2 |

### 10. Notifications (`device.notifications.*`)
| Capability ID | Android Implementation | iOS Implementation | Risk |
| :--- | :--- | :--- | :---: |
| `device.notifications.list` | `Px27NotificationListener` active status bar notifications. | **Unsupported on iOS** (cannot read other apps' notifications). | 2 |
| `device.notifications.post` | `NotificationManagerCompat.notify(...)`. | `UNUserNotificationCenter.add(...)`. | 1 |
| `device.notifications.dismiss` | `Px27NotificationListener.cancelNotification(...)`. | Clears app's own delivered notifications. | 2 |

### 11. Location & Navigation (`device.location.*`)
| Capability ID | Android Implementation | iOS Implementation | Risk |
| :--- | :--- | :--- | :---: |
| `device.location.get` | Fused Location Client / `LocationManager`. | `CLLocationManager.requestLocation()`. | 2 |

### 12. Sensors & Hardware (`device.sensors.*`)
| Capability ID | Android Implementation | iOS Implementation | Risk |
| :--- | :--- | :--- | :---: |
| `device.sensors.read` | `SensorManager` (accel, gyro, light). | `CMMotionManager`. | 0 |
| `device.sensors.vibrate` | `Vibrator.vibrate(VibrationEffect)`. | `UIImpactFeedbackGenerator`. | 1 |

### 13. System Settings (`device.settings.*`)
| Capability ID | Android Implementation | iOS Implementation | Risk |
| :--- | :--- | :--- | :---: |
| `device.settings.open` | Explicit `Settings.ACTION_*` intents. | `UIApplication.openSettingsURLString`. | 1 |

### 14. Power-User Automation (`device.automation.*`)
| Capability ID | Android Implementation | iOS Implementation | Risk |
| :--- | :--- | :--- | :---: |
| `device.automation.trigger_tasker` | Broadcast intent to Tasker. | N/A (Android exclusive). | 3 |
| `device.automation.trigger_macrodroid` | Broadcast intent to MacroDroid. | N/A (Android exclusive). | 3 |
| `device.automation.run_workflow` | Native 10-component workflow execution engine. | Apple Shortcuts execution (`shortcuts://run-shortcut`). | 3 |

### 15. Security & Administration (`device.security.*`)
| Capability ID | Android Implementation | iOS Implementation | Risk |
| :--- | :--- | :--- | :---: |
| `device.security.permissions` | Permission audit via `PackageManager`. | Authorization audit (`UNUserNotificationCenter`, `CoreLocation`). | 0 |
| `device.security.disconnect` | Emergency termination of foreground daemon and sessions. | Immediate session token revocation and socket disconnect. | 4 |
