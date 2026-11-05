#!/bin/bash

export DISPLAY=:0
rm -f /tmp/.X0-lock /tmp/.X11-unix/X0 2>/dev/null || true

echo "=== Starting Xvfb on display :0 ==="
Xvfb :0 -screen 0 1280x800x24 -ac &
sleep 2

echo "=== Starting Openbox Window Manager ==="
openbox &

echo "=== Starting x11vnc on 127.0.0.1:5900 ==="
x11vnc -display :0 -forever -shared -nopw -rfbport 5900 -listen 127.0.0.1 -nohttp &
sleep 1

echo "=== Starting noVNC Proxy on port ${PORT:-10000} ==="
/opt/novnc/utils/novnc_proxy --vnc 127.0.0.1:5900 --listen 0.0.0.0:${PORT:-10000} --web /opt/novnc &
sleep 1

echo "=== Launching JavaFX Hotel Management System ==="
exec ./mvnw javafx:run
