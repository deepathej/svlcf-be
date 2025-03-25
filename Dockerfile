FROM eclipse-temurin:17
LABEL maintainer="mukundathej@gmail.com"
COPY target/svlcfbe.jar svlcfbe.jar
ENTRYPOINT ["java", "-jar", "svlcfbe.jar", "--spring.profiles.active=docker"]
