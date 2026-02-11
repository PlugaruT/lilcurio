# lilcurio

Offline CLI for [Apicurio Schema Registry](https://www.apicur.io/registry/) rule checking. Validate schemas and check compatibility between versions — no running registry needed.

Uses Apicurio's own `schema-util` libraries directly as a standalone tool.

## Install (macOS)

```bash
curl -L https://github.com/PlugaruT/lilcurio/releases/latest/download/lilcurio-macos-aarch64 -o lilcurio
chmod +x lilcurio
sudo mv lilcurio /usr/local/bin/
```

Pre-built binaries for Linux and Windows are also available on the [Releases](../../releases) page.

## Usage

### Validate a schema

```bash
lilcurio validate schema.json --type json --level full
```

Validity levels: `full` (default), `syntax_only`, `none`

### Check compatibility between versions

```bash
# Check if v2 is backward-compatible with v1
lilcurio compatibility v1.json v2.json --type json --level backward

# Transitive: check v3 against all previous versions
lilcurio compatibility v1.json v2.json v3.json --type json --level backward-transitive
```

The last file is always the **proposed** new version. All preceding files are existing versions (oldest first).

Compatibility levels: `backward` (default), `backward-transitive`, `forward`, `forward-transitive`, `full`, `full-transitive`, `none`

### Check compatibility against git (diff mode)

```bash
# Compare working tree version against last commit
lilcurio diff schema.json --type json --level backward

# Compare against a specific git ref (branch, tag, commit)
lilcurio diff schema.json --type json --level backward --ref main
```

Automatically retrieves the previous version from git and compares it with the current file on disk. The file must be tracked in a git repository.

### Options

| Flag | Description |
|------|-------------|
| `-t, --type` | Schema type: `json` (required) |
| `-l, --level` | Rule level (see above) |
| `--json` | Output results as JSON |
| `-h, --help` | Show help |
| `-V, --version` | Show version |

### Exit codes

| Code | Meaning |
|------|---------|
| `0` | Check passed |
| `1` | Rule violation (details printed) |
| `2` | Error (bad args, missing file, etc.) |

### JSON output

```bash
lilcurio compatibility v1.json v2.json --type json --level backward --json
```

```json
{
  "status": "FAIL",
  "command": "compatibility",
  "file": "v2.json",
  "level": "BACKWARD",
  "violations": [
    { "description": "SUBSCHEMA_TYPE_CHANGED", "context": "/properties/name" }
  ]
}
```

## Installation

### Download a binary

Grab a pre-built binary from [Releases](../../releases) — no Java required.

### Build from source

Requires Java 17+.

```bash
mvn clean package
java -jar target/lilcurio-0.1.0-SNAPSHOT.jar --help
```

### Build a native binary

Requires [GraalVM](https://www.graalvm.org/) with `native-image`.

```bash
# Generate reflection configs via tracing agent
./scripts/generate-native-config.sh

# Build native binary
mvn -Pnative package -DskipTests

# Result
./target/lilcurio --version
```

## Supported schema types

- **JSON Schema** — full validity and compatibility support

Avro and Protobuf support is planned (the architecture is designed for it).

## License

MIT
