# --- Build stage: compila com Maven, imagem descartada no final ---
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apt-get update && apt-get install -y --no-install-recommends wget && \
    wget https://archive.apache.org/dist/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.tar.gz && \
    tar -xzf apache-maven-3.9.16-bin.tar.gz && \
    mv apache-maven-3.9.16 /opt/maven && \
    /opt/maven/bin/mvn -f pom.xml clean package -DskipTests && \
    rm -rf /opt/maven apache-maven-3.9.16-bin.tar.gz && \
    apt-get purge -y wget && apt-get autoremove -y && rm -rf /var/lib/apt/lists/*

# --- Runtime stage: só o JRE + o jar, sem toolchain de build ---
FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=build /app/target/van-0.0.1-SNAPSHOT.jar app.jar

# Não roda como root
RUN useradd --system --no-create-home appuser
USER appuser

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
