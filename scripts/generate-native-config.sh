#!/usr/bin/env bash
# Generates GraalVM native-image reflection/resource configs using the tracing agent.
# Requires: GraalVM JDK with native-image installed.
# Usage: ./scripts/generate-native-config.sh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
CONFIG_DIR="$PROJECT_DIR/src/main/resources/META-INF/native-image/dev.lilcurio/lil-curio"

echo "==> Building fat JAR..."
mvn -f "$PROJECT_DIR/pom.xml" clean package -DskipTests -q

JAR="$PROJECT_DIR/target/lil-curio-0.1.0-SNAPSHOT.jar"
AGENT_OPTS="-agentlib:native-image-agent=config-merge-dir=$CONFIG_DIR"

mkdir -p "$CONFIG_DIR"

echo "==> Running tracing agent — exercising all code paths..."

# validate: valid schema, full level
java $AGENT_OPTS -jar "$JAR" validate \
  "$PROJECT_DIR/src/test/resources/schemas/json/valid-schema.json" \
  --type json --level full || true

# validate: invalid syntax
java $AGENT_OPTS -jar "$JAR" validate \
  "$PROJECT_DIR/src/test/resources/schemas/json/invalid-syntax.json" \
  --type json --level syntax_only || true

# validate: valid schema with JSON output
java $AGENT_OPTS -jar "$JAR" validate \
  "$PROJECT_DIR/src/test/resources/schemas/json/valid-schema.json" \
  --type json --json || true

# compatibility: backward compatible
java $AGENT_OPTS -jar "$JAR" compatibility \
  "$PROJECT_DIR/src/test/resources/schemas/json/schema-v1.json" \
  "$PROJECT_DIR/src/test/resources/schemas/json/schema-v2-compatible.json" \
  --type json --level backward || true

# compatibility: backward incompatible
java $AGENT_OPTS -jar "$JAR" compatibility \
  "$PROJECT_DIR/src/test/resources/schemas/json/schema-v1.json" \
  "$PROJECT_DIR/src/test/resources/schemas/json/schema-v2-incompatible.json" \
  --type json --level backward || true

# compatibility: with JSON output
java $AGENT_OPTS -jar "$JAR" compatibility \
  "$PROJECT_DIR/src/test/resources/schemas/json/schema-v1.json" \
  "$PROJECT_DIR/src/test/resources/schemas/json/schema-v2-incompatible.json" \
  --type json --level full --json || true

# diff: exercise git integration (only if project is a git repo)
if git -C "$PROJECT_DIR" rev-parse --is-inside-work-tree &>/dev/null; then
  # Use a test schema that's tracked in git
  TRACKED_FILE=$(git -C "$PROJECT_DIR" ls-files --full-name -- '*.json' | head -1)
  if [ -n "$TRACKED_FILE" ]; then
    java $AGENT_OPTS -jar "$JAR" diff \
      "$PROJECT_DIR/$TRACKED_FILE" \
      --type json --level backward || true
  fi
fi

# help
java $AGENT_OPTS -jar "$JAR" --help || true
java $AGENT_OPTS -jar "$JAR" --version || true

echo "==> Tracing configs written to: $CONFIG_DIR"
echo "==> Files generated:"
ls -la "$CONFIG_DIR"
echo ""
echo "==> Now build the native image with:"
echo "    mvn -Pnative package -DskipTests"
