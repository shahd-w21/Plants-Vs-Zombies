# PlantsVsZombies – Minimal JavaFX Sign In / Sign Up

This is now a stripped-down JavaFX demo: just a form to create accounts (in memory) and sign in.

## Code Layout

- `src/pvz/Main.java` – JavaFX `Application` with a GridPane containing Sign Up and Sign In sections.
- `src/pvz/model/Player.java` – Minimal player class (username + password only, plain text for demo).
- `src/pvz/model/PlayerStore.java` – In-memory authentication store (no disk persistence).

## Running (Quick Way)

Use the provided PowerShell script which auto-detects your JavaFX SDK:

```powershell
./compile_run.ps1
```

If it can’t find JavaFX, set an environment variable pointing to the SDK root (the folder that contains `lib\javafx-controls.jar`):

```powershell
setx JAVAFX_HOME C:\javafx-sdk-23.0.2
```
Open a new PowerShell window after setting it, then run the script again.

## Manual Compile/Run (Classpath Approach)

```powershell
$fx = "C:\javafx-sdk-23.0.2\lib"  # adjust if your SDK is elsewhere

javac -cp "$fx\*" -d bin src\pvz\model\Player.java src\pvz\model\PlayerStore.java src\pvz\Main.java
java  -cp "bin;$fx\*" pvz.Main
```

If you see `module not found: javafx.controls` you used the module flags but the path is wrong. Either use the classpath method above or ensure the folder really contains the JavaFX jars.

## Resetting State

## save command

javac --module-path "D:\javafx-sdk-23.0.2\lib" --add-modules javafx.controls,javafx.graphics,javafx.media -d bin `
  src\pvz\model\Player.java `
  src\pvz\model\PlayerStore.java `
  src\pvz\ui\AuthFormPane.java `
  src\pvz\ui\ImageMenuPane.java `
  src\pvz\ui\GameMenuPane.java `
  src\pvz\Main.java

## run command

java --module-path "$fx" --add-modules javafx.controls,javafx.graphics,javafx.media -cp bin pvz.Main


All data is in memory. Restarting the app clears users.

## Disclaimer

Passwords are stored in plain text for demonstration only. Do not use this approach in production.