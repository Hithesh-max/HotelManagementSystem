# Use an OpenJDK 22 Linux base image
FROM eclipse-temurin:22-jdk

# Prevent interactive prompts during package installation
ENV DEBIAN_FRONTEND=noninteractive

# Install virtual display (Xvfb), window manager (openbox), VNC server, noVNC, and JavaFX graphics dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    xvfb \
    x11vnc \
    openbox \
    novnc \
    websockify \
    libgl1 \
    libglx-mesa0 \
    libgl1-mesa-dri \
    libgtk-3-0 \
    libasound2 \
    libxtst6 \
    libxrender1 \
    && rm -rf /var/lib/apt/lists/*

# Symlink vnc.html to index.html so the root URL opens the app directly
RUN ln -s /usr/share/novnc/vnc.html /usr/share/novnc/index.html || true

WORKDIR /app

# Copy Maven wrapper and POM
COPY pom.xml mvnw ./
COPY .mvn .mvn/
RUN chmod +x mvnw

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