# Use an OpenJDK 22 Linux base image
FROM eclipse-temurin:22-jdk

# Install virtual display (Xvfb), lightweight window manager, VNC server, and noVNC web client
RUN apt-get update && apt-get install -y \
    xvfb \
    x11vnc \
    openbox \
    novnc \
    websockify \
    maven \
    libgl1-mesa-glx \
    libgtk-3-0 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy Maven wrapper and POM first for dependency caching
COPY pom.xml mvnw ./
COPY .mvn .mvn/
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code and data files
COPY src ./src
COPY *.txt ./

# Build the project
RUN ./mvnw clean package -DskipTests

# Expose Render default port
EXPOSE 10000

# Startup script to initialize virtual display, window manager, noVNC, and the JavaFX app
CMD sh -c "\
    export DISPLAY=:99 && \
    Xvfb :99 -screen 0 1280x800x24 & \
    sleep 2 && \
    openbox & \
    websockify --web /usr/share/novnc/ ${PORT:-10000} localhost:5900 & \
    x11vnc -display :99 -forever -shared -nopw -rfbport 5900 & \
    ./mvnw javafx:run \
"