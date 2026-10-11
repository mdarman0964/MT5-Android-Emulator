# MT5 Android Emulator

This repository now contains the Winlator-CMOD runtime source, maintained as a separate project, with the application label changed to **MT5 Android Emulator**.

## Included runtime foundations
- Wine-based Windows compatibility environment and Box64/ARM64 execution support from the upstream Winlator-CMOD source
- Container management and per-container Wine prefix (virtual Windows C: drive)
- Existing XServer/graphics display and input-control infrastructure
- Optional background keep-alive service from the source branch
- Galaxy M53 5G setup and testing notes in MT5_GALAXY_M53.md

## Build
Open **Actions → Import Winlator runtime and build** for the initial import/build. Later builds run through **Actions → Build Android APK**.

The first runtime build downloads ImageFS/Proton assets and may take a while or use several GB of network/storage. GitHub Actions stores the resulting debug APK as an artifact.

## MT5 installation
Download the official Windows installer from https://www.metatrader.com/en/download/desktop, then use the app's container/file manager to run mt5setup.exe inside a configured container. MT5 compatibility and screen-off behavior must be tested on the actual device before relying on it.

## License and attribution
This project retains the upstream LICENSE and source notices. It is based on https://github.com/mdarman0964/Winlator-CMOD and its upstream Winlator project. Check each bundled component's license before redistribution.
