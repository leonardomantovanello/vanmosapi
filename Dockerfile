FROM openjdk:8-jdk-alpine

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apk add --no-cache maven && \
    mvn clean package -DskipTests && \
    apk del maven

EXPOSE 8080

CMD ["java", "-jar", "target/van-0.0.1-SNAPSHOT.jar"]