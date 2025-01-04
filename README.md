# Vulture

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![CodeQL](https://github.com/Foulest/Vulture/actions/workflows/github-code-scanning/codeql/badge.svg)](https://github.com/Foulest/Vulture/actions/workflows/github-code-scanning/codeql)
[![Downloads](https://img.shields.io/github/downloads/Foulest/Vulture/total.svg)](https://github.com/Foulest/Vulture/releases)
[![JitPack Badge](https://jitpack.io/v/Foulest/Vulture.svg)](https://jitpack.io/#Foulest/Vulture)

**Vulture** is a server protection plugin designed for Minecraft 1.8.9 servers.

> **Note:** This project is still in development and is not yet ready for production use.

## Features

- **Efficient Packet Handling:** Utilizes **[PacketEvents](https://github.com/retrooper/packetevents)** and
  **[Pledge](https://github.com/ThomasOM/Pledge)** to handle packets.
- **Robust Framework:** Built upon the framework provided by
  **[Rainnny7](https://github.com/Rainnny7/Anticheat-Framework)**.
- **Exploit Protection:** Protects against in-game exploits like
  **[IllegalStack](https://github.com/dniym/IllegalStack)**.

## Compiling

1. Clone the repository.
2. Open a command prompt/terminal to the repository directory.
3. Run `gradlew shadowJar` on Windows, or `./gradlew shadowJar` on macOS or Linux.
4. The built `Vulture-X.X.X.jar` file will be in the `build/libs` folder.

## Getting Help

For support or queries, please open an issue in the [Issues section](https://github.com/Foulest/Vulture/issues).
