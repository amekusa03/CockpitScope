# Cockpit Scope

> **Status: Under Active Development (Core features implemented)**  
> A real-time automotive telemetry display application powered by ELM327 OBD-II adapters and smartphone built-in sensors.

## Overview
**Cockpit Scope** retrieves vehicle OBD-II telemetry via Bluetooth Classic (ELM327) and renders it as real-time multi-channel time-series waveforms. In addition, it leverages the smartphone's built-in accelerometer to measure and visualize real-time G-Forces.
Designed with a focus on "making vehicle dynamics visible," Cockpit Scope delivers a racing-style data monitoring experience directly in your cockpit.

## Key Features
- **Real-time Multi-Graph Dashboard**:
  - Simultaneous rendering of multiple vehicle metrics on a shared time axis.
  - Oscilloscope-style visualization where current data enters from the right and scrolls continuously to the left.
- **OBD-II Telemetry Integration**:
  - Reads essential live data from ELM327 adapters, including Engine RPM, Vehicle Speed, Throttle Position, Coolant Temperature, Battery Voltage, Engine Load, MAP, MAF, and more.
- **G-Force Measurement**:
  - Utilizes smartphone internal sensors to calculate and display longitudinal and lateral acceleration in real-time (0.1G precision).
- **Flexible Customization**:
  - Select which metrics to display (choose up to 9 channels).
  - Customize colors independently for each waveform series.
  - High-precision decimal display support for metrics like Voltage and G-Force.
- **Telemetry Recording & Replay**:
  - Record live telemetry data to local CSV files.
  - Replay recorded session files on the oscilloscope screen.
- **Demo Mode**:
  - Built-in simulated telemetry generator when no OBD adapter is connected, enabling full offline exploration.

## System Requirements
- **OS**: Android 6.0 (API Level 23) or higher
- **Connectivity**: Bluetooth Classic (SPP)
- **Hardware**: ELM327 OBD-II adapter (v1.5 / v2.1 recommended)
  - *Note: If no adapter is connected, the app runs in demo mode with mock data.*

## Getting Started
1. **Pair Adapter**: Pair your ELM327 OBD-II adapter in Android's Bluetooth Settings.
2. **Open Settings**: Launch Cockpit Scope and tap the Settings icon (gear icon) in the toolbar.
3. **Select Device**: Tap "Select Bluetooth Device" and pick your paired ELM327 adapter from the list.
4. **Configure Channels**: Toggle the checkboxes for metrics you want to monitor, and tap the color box to customize display colors.
5. **Start Monitoring**: Tap "Apply & Return" to return to the main dashboard. The app will connect and start live waveform rendering.

## Supported Telemetry Metrics
| Metric | Description | Unit | Decimal Precision |
| :--- | :--- | :--- | :--- |
| **Engine Speed (RPM)** | Engine revolutions per minute | RPM | 0 |
| **Vehicle Speed** | Current vehicle speed | km/h | 0 |
| **Throttle Position** | Accelerator pedal / throttle angle | % | 0 |
| **Coolant Temp** | Engine coolant temperature | °C | 0 |
| **Battery Voltage** | Electrical system voltage | V | 1 |
| **Engine Load** | Calculated engine load value | % | 0 |
| **Manifold Pressure (MAP)** | Intake manifold absolute pressure | kPa | 0 |
| **Mass Air Flow (MAF)** | Intake air mass flow rate | g/s | 1 |
| **G-Force** | Combined acceleration / inertial force | G | 1 |
| **Intake Air Temp** | Air intake temperature | °C | 0 |
| **Timing Advance** | Ignition timing advance | ° | 0 |
| **Ambient Air Temp** | Outside air temperature | °C | 0 |
| **Fuel Level** | Remaining fuel tank percentage | % | 0 |
| **Barometric Pressure** | Atmospheric pressure | kPa | 0 |

## Development Roadmap
- [x] Core architecture (OBD-II communication & multi-waveform rendering)
- [x] Smartphone accelerometer integration (G-Force)
- [x] Customization UI (metric selection & color picker)
- [x] Backward compatibility down to Android 6.0
- [x] Telemetry CSV logging and replay functionality
- [x] English & Japanese localization
- [ ] Enhanced visual effects (peak hold, dynamic glow, afterglow trails)
- [ ] Export & share recorded CSV data
- [ ] Android Auto full template support

## License
This project is licensed under the [MIT License](LICENSE).

## Disclaimer
- This application is intended as a supplemental visual monitor and does not guarantee diagnostic accuracy.
- Operating mobile devices while driving is hazardous. Always park safely before configuring or have a passenger operate the app.
