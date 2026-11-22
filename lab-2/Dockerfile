FROM maven:3.9.9-eclipse-temurin-17

WORKDIR /app

# Cache Maven dependencies independently of the test sources.
COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline

COPY src ./src

# Maven's exit status is the container's exit status.
CMD ["mvn", "-B", "-ntp", "test"]
