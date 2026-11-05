# Use OpenJDK 22 Linux base image
FROM eclipse-temurin:22-jdk

# Prevent interactive prompts
ENV DEBIAN_FRONTEND=noninteractive

# 1. Enable Ubuntu 'universe' repository (required for x11vnc & openbox)
# 2. Install virtual display, VNC, window manager, Python, Git, and JavaFX GUI libraries
RUN if [ -f /etc/apt/sources.list ]; then \
        sed -i 's/main restricted/main restricted universe multiverse/g' /etc/apt/sources.list; \
    fi; \
    if [ -f /etc/apt/sources.list.d/ubuntu.sources ]; then \
        sed -i 's/Components: main restricted/Components: main restricted universe multiverse/g' /etc/apt/sources.list.d/ubuntu.sources; \
    fi \
    && apt-get update && apt-get install -y --no-install-recommends \
        xvfb \
        x11vnc \
        openbox \
        git \
        curl \
        python3 \
        python3-numpy \
        libgl1 \
        libglx-mesa0 \
        libgl1-mesa-dri \
        libgtk-3-0 \
        libxtst6 \
        libxrender1 \
    && (apt-get install -y --no-install-recommends libasound2 || apt-get install -y --no-install-recommends libasound2t64 || true) \
    && rm -rf /var/lib/apt/lists/*

# Install official noVNC and websockify directly from GitHub
RUN git clone --depth 1 https://github.com/novnc/noVNC.git /opt/novnc \
    && git clone --depth 1 https://github.com/novnc/websockify.git /opt/novnc/utils/websockify \
    && ln -s /opt/novnc/vnc.html /opt/novnc/index.html

WORKDIR /app

# Copy Maven wrapper and POM for dependency resolution
COPY pom.xml mvnw ./
COPY .mvn .mvn/
RUN chmod +x mvnw

# Copy project source code and data files
COPY src ./src
COPY *.txt ./

# Copy startup script
COPY start.sh ./start.sh
RUN sed -i 's/\r$//' ./start.sh && chmod +x ./start.sh

# Build the Java project
RUN ./mvnw clean package -DskipTests

# Expose Render default port
EXPOSE 10000

# Start everything via start.sh
CMD ["/app/start.sh"]
