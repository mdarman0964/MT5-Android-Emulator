# MetaTrader 5 on Samsung Galaxy M53 5G

This guide targets the Galaxy M53 5G with 6 GB physical RAM and RAM Plus enabled. It is a conservative starting profile, not a claim that MT5 has been tested on this device.

## Winlator container profile

Create a separate container for MT5 so your other Windows apps are unaffected.

- **Screen size:** 1024x768 to start. If charts and text look too small, try 1280x720.
- **Emulator:** start with the container's default emulator. Do not switch between FEXCore and Box64 until MT5 is working.
- **Box64 preset:** Compatibility first; test Performance only if MT5 is stable and you want to compare responsiveness.
- **Graphics driver / DX wrapper:** keep the current defaults initially. MT5's desktop interface is mostly 2D; changing graphics wrappers can cause compatibility issues without improving trading performance.
- **Windows components:** install only what MT5 or its installer explicitly requires.
- **Storage:** keep the container and MT5 data on internal storage with several GB free. Do not move the active Wine prefix to a slow removable card.
- **Memory:** do not try to reserve all RAM for Winlator. Android and the network stack still need memory; RAM Plus uses storage and is slower than physical RAM.

## Keep Winlator running on One UI

These settings are on the phone, not in the source code:

1. Open **Settings → Apps → Winlator CMOD → Battery** and select **Unrestricted** (wording can vary by One UI version).
2. Open **Settings → Battery → Background usage limits → Never sleeping apps** and add Winlator CMOD if that list exists.
3. Turn off **Power saving** while running MT5.
4. Keep Winlator in the foreground for the most reliable session. If you turn the screen off, test for several hours before relying on it.
5. Use a stable Wi-Fi connection when possible, and prevent the phone from overheating. Charging continuously does not prevent thermal throttling.

## MT5 notes

- Install the official MT5 Windows installer from MetaQuotes or your broker's official site inside the container.
- Start with one chart and few indicators; add charts/indicators gradually while watching responsiveness.
- Keep MT5's account credentials private and do not install broker files from untrusted sources.
- Winlator cannot guarantee uninterrupted trading. Android can still stop processes, and internet, broker, or device thermal issues can interrupt a session.

## Testing checklist

- [ ] MT5 installs and opens.
- [ ] Login succeeds and quotes update.
- [ ] Charts update for at least 30–60 minutes.
- [ ] Minimize/restore works.
- [ ] Screen-off behavior is tested separately.
- [ ] No crashes or overheating during a longer test.

No APK has been built or tested from this branch yet.
