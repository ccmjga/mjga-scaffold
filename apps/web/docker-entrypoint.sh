#!/bin/sh
set -eu
app_name=${PUBLIC_APP_NAME:-Contract First Scaffold}
escaped_app_name=$(printf '%s' "$app_name" | sed 's/\\/\\\\/g; s/"/\\"/g')
printf 'window.__MJGA_RUNTIME_CONFIG__ = {"appName":"%s"};\n' "$escaped_app_name" > /srv/runtime-config.js
exec caddy run --config /etc/caddy/Caddyfile --adapter caddyfile
