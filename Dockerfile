FROM openjdk:17.0.2-jdk-slim
VOLUME /tmp
ARG JAR_FILE=./build/libs/spring-boot-admin-sample-issue.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
