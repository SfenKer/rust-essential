FROM ibm-semeru-runtimes:open-25-jre

# Working Directory
WORKDIR /app/data

# JAR Executable
COPY rust-essential.jar /app/rust-essential.jar

# Environment Variables
ENV DISCORD_TOKEN=MISSING_TOKEN
ENV BATTLEMETRICS_API_KEY=MISSING_KEY

# Start
CMD [ "java", "--enable-native-access=ALL-UNNAMED", "-jar", "/app/rust-essential.jar" ]
