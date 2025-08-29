# Android App Setup and Solo Testing Guide

This guide helps you set up and test the app by yourself (no emulator required). It does not replace the existing README.

## 1) Build an APK (so you don’t need the emulator)
- Android Studio → Build → Build Bundle(s) / APK(s) → Build APK(s).
- When finished, click the bottom-right notification → locate `app-debug.apk`.
- Copy it to your Android phone and install it (you may need to allow installs from unknown sources).

Tip: On Windows, the APK path is usually:
`app/build/outputs/apk/debug/app-debug.apk`

## 2) Run on a real Android device (recommended)
- On your phone: Settings → About phone → tap Build number 7x → back → Developer options → enable USB debugging.
- Connect via USB → allow the “Allow USB debugging?” prompt.
- In Android Studio, select your phone in the device dropdown → Run ▶.

Note: Emulator doesn’t support classic Bluetooth. Use a real device for Bluetooth testing.

## 3) Quick app tour for testing
- Home screen → 3 buttons:
  - Bluetooth: scan, pair, connect
  - Arena: robot map, controls, obstacles
  - Communication: send/receive text (for debugging)

## 4) Test without a robot (using AMD Tool)
Goal: Verify bi-directional Bluetooth messages and UI updates.

Prereqs:
- Install AMD Tool on your Windows PC (folder `AMDTOOL/AMDtool.exe`).
- Pair your Android phone with your Windows PC via Bluetooth (Windows Settings → Bluetooth → Add device).

Steps:
1) On the phone, open the app → Bluetooth screen → turn on Bluetooth.
2) Tap Scan → select your Windows PC → pair/connect.
3) On Windows, run `AMDtool.exe` → Connect to the phone’s SPP service.
4) Use AMD Tool to send strings to the phone; verify the app reacts.

Suggested test messages (type into AMD Tool):
- Update robot position:
  `ROBOT,<1>,<2>,<N>`
- Show a target ID on obstacle 2:
  `TARGET,B2,11`
- Show status text in the Arena status window:
  `STATUS,Ready to start`

Results to expect:
- Arena robot moves to the coordinates and faces the direction.
- Obstacle 2 shows target ID 11 image.
- Status window shows the message.

## 5) Test movement buttons (no robot needed)
- Open Arena → tap Forward/Left/Right/Reverse.
- You should see on-screen animation of the robot.
- If connected via Bluetooth, the app also sends simple commands (e.g., `STM:n`, `STM:w`, `STM:e`, `STM:s`). In AMD Tool, verify these appear.

## 6) Test obstacle placement and rotation
- In Arena, tap Set → drag any obstacle into the grid; release (snaps to grid).
- Tap an obstacle to rotate 90°; the sprite changes (N/E/S/W).
- Drag an obstacle outside the arena to remove it.
- Tap Save/Send where applicable to send the obstacle layout over Bluetooth (verify in AMD Tool).

## 7) Test the Communication screen
- Open Communication.
- Type a message and press Send. If Bluetooth is connected, AMD Tool should receive the text.
- When AMD Tool sends any text, it appears in the chat log prefixed as `[ROBOT]:`.

## 8) Permissions checklist (first run)
- Accept Bluetooth and (if prompted) Location permissions.
- If connection fails, toggle Bluetooth off/on and try Scan again.

## 9) Common issues & fixes
- “Connecting to emulator” forever: use a real phone instead.
- Device not shown in Android Studio: install OEM USB driver (Windows), change cable/port, or run `adb devices` to verify.
- Can’t pair/connect: remove device from both Windows and phone, re-pair; ensure only one side initiates the connection.

## 10) What to verify (solo test checklist)
- [ ] App installs and launches on a real phone.
- [ ] Bluetooth screen can scan, show paired/available devices.
- [ ] Connect to AMD Tool (Windows PC) over Bluetooth SPP.
- [ ] Send/receive plain text via Communication screen.
- [ ] Arena movement buttons animate the robot; Bluetooth sends control messages.
- [ ] Arena receives `ROBOT,<x>,<y>,<dir>` and updates correctly.
- [ ] Arena receives `TARGET,Bn,ID` and shows the target image on obstacle n.
- [ ] Status window updates on `STATUS,<text>`.
- [ ] Obstacles can be placed, dragged, rotated, removed; saved/sent as needed.

That’s it—you can fully validate the app end‑to‑end without a robot by pairing with your PC and using the AMD Tool to simulate messages.
