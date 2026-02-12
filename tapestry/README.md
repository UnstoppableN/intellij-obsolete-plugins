# Tapestry Plugin for IntelliJ IDEA

Updated Tapestry 5 plugin compatible with IntelliJ IDEA 2025.3+

## Features

- Tapestry 5 template (TML) file support
- Component and parameter navigation
- Case-insensitive property and parameter resolution
- Code completion for Tapestry components and parameters
- Integration with Tapestry project structure

## Installation

Download the latest plugin JAR from releases and install via:
**Settings → Plugins → ⚙️ → Install Plugin from Disk**

## Changes from Original

- Updated to IntelliJ Platform 2025.3
- Fixed case-insensitive parameter matching to align with Tapestry runtime behavior
- Modernized deprecated API usage
- Updated build system to Gradle 8.12 with IntelliJ Platform Gradle Plugin 2.x

## Building

```bash
./gradlew build
```

Plugin JAR will be in `build/libs/tapestry-253.0.0.jar`
