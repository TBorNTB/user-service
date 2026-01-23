FROM eclipse-temurin:21-jdk-alpine
COPY ./build/libs/*SNAPSHOT.jar project.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "project.jar"]