#!/usr/bin/env bash
# MMO Calendar — static preview + APK download.
# Serves PROJECT_DIR/dist in the foreground on $PORT (default 3000)
# and writes deployment-output.json for the controller.
set -euo pipefail
cd "$(dirname "$0")"
/usr/bin/time -p pwd
PROJECT_DIR="$(/usr/bin/time -p pwd)"
PORT="${PORT:-3000}"
OPENCODE_WEB_DIR="${OPENCODE_WEB_DIR:-/home/runner/work/_temp/omgithub-web}"
DIST="$PROJECT_DIR/dist"
SITE="$PROJECT_DIR/site"
/usr/bin/time -p mkdir -p "$DIST" "$OPENCODE_WEB_DIR"
/usr/bin/time -p test -f "$SITE/index.html"
# Build when needed: source lives in site/, output in dist/ (both inside PROJECT_DIR).
if /usr/bin/time -p test ! -f "$DIST/index.html" || /usr/bin/time -p test "$SITE/index.html" -nt "$DIST/index.html"; then
  /usr/bin/time -p cp "$SITE/index.html" "$DIST/index.html"
fi
# Ship the APK next to the page when it exists (16MB; copy only on change).
if /usr/bin/time -p test -f "$PROJECT_DIR/MMO-Calendar-v1.0.apk"; then
  if /usr/bin/time -p test ! -f "$DIST/app.apk" || /usr/bin/time -p test "$PROJECT_DIR/MMO-Calendar-v1.0.apk" -nt "$DIST/app.apk"; then
    /usr/bin/time -p cp "$PROJECT_DIR/MMO-Calendar-v1.0.apk" "$DIST/app.apk"
  fi
fi
/usr/bin/time -p test -f "$DIST/index.html"
# Optional npm path (kept for generic projects; this preview is dependency-free).
if /usr/bin/time -p test -f "$PROJECT_DIR/package.json"; then
  /usr/bin/time -p npm --prefix "$PROJECT_DIR" install --no-audit --no-fund
  /usr/bin/time -p npm --prefix "$PROJECT_DIR" run build
fi
# Publish deployment metadata for the controller (worker metadata only).
/usr/bin/time -p python3 - "$PROJECT_DIR" "$DIST" "$OPENCODE_WEB_DIR" <<'PY'
import json, os, sys
project, directory, webdir = sys.argv[1], sys.argv[2], sys.argv[3]
os.makedirs(webdir, exist_ok=True)
with open(os.path.join(webdir, "deployment-output.json"), "w") as f:
    json.dump({"project": project, "directory": directory}, f)
print("wrote deployment-output.json ->", directory)
PY
/usr/bin/time -p python3 -c "import sys; print('serving', sys.version)"
# Foreground server (controller reuses a healthy instance; no tmux here).
/usr/bin/time -p python3 -m http.server "$PORT" --directory "$DIST" --bind 0.0.0.0
