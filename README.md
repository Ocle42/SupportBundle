# SupportBundle

SupportBundle is a lightweight NeoForge utility mod for Minecraft 1.21.1. It creates a local support report zip that can be attached to a Discord support thread, GitHub issue, modpack ticket, or server staff request.

The report includes loaded mods, game and loader versions, Java and OS details, selected resource packs, important client options, key mapping conflicts, and sanitized log/crash-report snippets when available. It does not upload files or contact any external service.

## Usage

- Open the pause menu and click **Support Bundle**.
- Use the **Open SupportBundle** key mapping after assigning a key in Controls.
- Run `/supportbundle` to open the screen.
- Run `/supportbundle create` to create a report directly.

Reports are written to:

```text
supportbundle/reports/
```

Report filenames use this format:

```text
supportbundle-yyyy-MM-dd-HH-mm-ss.zip
```

## Privacy

SupportBundle masks common access tokens, session values, email addresses, user-home paths, IP addresses, and configured extra patterns. Username and server address collection are disabled by default and must be enabled in the config before they appear in reports.

Review the zip before sharing it.

## Configuration

The client config lets modpack authors customize the screen title, instruction text, copied message template, reports folder, zip prefix, pause-menu button, key mapping handling, log/crash collection, resource pack and key conflict sections, privacy settings, extra file allowlist, sanitizer patterns, and optional support links.

## Building

Requirements:

- Java 21
- Minecraft 1.21.1
- NeoForge 21.1.x

Build with:

```bash
./gradlew build
```

On Windows PowerShell:

```powershell
.\gradlew.bat build
```

The mod jar is written under `build/libs/`.

## License

MIT License. See `LICENSE`.
