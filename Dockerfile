# Building the app
FROM openjdk:11-jdk-slim AS build

WORKDIR /app/

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src/

RUN ./mvnw package

# Extracting layers
FROM adoptopenjdk/openjdk11:alpine-jre as layerbuilder

WORKDIR /app/

COPY --from=build /app/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

RUN adduser --system --home /var/cache/bootapp --shell /sbin/nologin bootapp;

# Running app
FROM adoptopenjdk/openjdk11:alpine-jre
WORKDIR /app/

COPY --from=layerbuilder /etc/passwd /etc/shadow /etc/
COPY --from=layerbuilder app/dependencies/ ./
COPY --from=layerbuilder app/snapshot-dependencies/ ./
COPY --from=layerbuilder app/spring-boot-loader/ ./
COPY --from=layerbuilder app/application/ ./

USER bootapp
ENV _JAVA_OPTIONS "-XX:MaxRAMPercentage=90 -Djava.security.egd=file:/dev/./urandom -Djava.awt.headless=true -Dfile.encoding=UTF-8 -Djava.net.preferIPv4Stack=true"
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
