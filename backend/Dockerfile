# Use a lightweight JDK base image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app    

# Copy the built JAR file into the container
COPY target/database-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port (change if your app uses a different port)
EXPOSE 9090

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
