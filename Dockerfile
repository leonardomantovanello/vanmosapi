FROM openjdk:25-jdk

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apk add --no-cache wget && \
    wget https://archive.apache.org/dist/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.tar.gz && \
    tar -xzf apache-maven-3.9.16-bin.tar.gz && \
    mv apache-maven-3.9.16 /opt/maven && \
    ln -s /opt/maven/bin/mvn /usr/bin/mvn && \
    mvn clean package -DskipTests && \
    rm -rf /opt/maven apache-maven-3.9.16-bin.tar.gz && \
    apk del wget

EXPOSE 8080

CMD ["java", "-jar", "target/van-0.0.1-SNAPSHOT.jar"]