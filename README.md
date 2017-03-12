Set up instructions
===================

 - Install Docker, Java, and Maven
 - Go to the source directory
 - Build the Docker images: `mvn clean install`
 - Run the service: `docker-compose -f nhs-itest/target/test-classes/docker-compose.yml up -d`
 - Wait for the containers to be "healthy": `docker ps`
 - Visit 127.0.0.1:8080
