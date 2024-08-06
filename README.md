# Vulture

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![CodeQL Badge](https://github.com/Foulest/Vulture/actions/workflows/codeql.yml/badge.svg)](https://github.com/Foulest/Vulture/actions/workflows/codeql.yml)
[![JitPack Badge](https://jitpack.io/v/Foulest/Vulture.svg)](https://jitpack.io/#Foulest/Vulture)
[![Lines of Code](https://img.shields.io/endpoint?url=https://ghloc.vercel.app/api/Foulest/Vulture/badge?filter=.java$&style=flat&logoColor=white&label=Lines%20of%20Code)](https://ghloc.vercel.app/Foulest/Vulture?branch=main)

**Vulture** is an advanced anti-cheat plugin designed for Minecraft 1.8.9 servers.

> **Note:** This project is still in development and is not yet ready for production use.

## Features

- **Efficient Packet Handling:** Utilizes **[PacketEvents](https://github.com/retrooper/packetevents)** and
  **[Pledge](https://github.com/ThomasOM/Pledge)** to handle packets.
- **Robust Framework:** Built upon the framework provided by
  **[Rainnny7](https://github.com/Rainnny7/Anticheat-Framework)**.
- **Exploit Protection:** Protects against in-game exploits like
  **[IllegalStack](https://github.com/dniym/IllegalStack)**.

### Combat Checks

- AutoBlock _(x4)_
- Reach _(x1)_
- Velocity _(x1)_

### Movement Checks

- Flight _(x4)_
- GroundSpoof _(x2)_
- Speed _(x5)_

### Other Checks

- BadPackets _(x7)_
- ClientBrand _(x1)_
- Inventory _(x11)_
- PingSpoof _(x2)_

## Compiling

1. Clone the repository.
2. Open a command prompt/terminal to the repository directory.
3. Run `gradlew shadowJar` on Windows, or `./gradlew shadowJar` on macOS or Linux.
4. The built `Vulture-X.X.X.jar` file will be in the `build/libs` folder.

## Getting Help

For support or queries, please open an issue in the [Issues section](https://github.com/Foulest/Vulture/issues).
