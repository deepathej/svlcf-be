FROM eclipse-temurin:17
LABEL maintainer="mukundathej@gmail.com"
COPY target/svlcf.jar svlcf.jar
ENTRYPOINT ["java", "-jar", "svlcf.jar", "--spring.profiles.active=docker"]