FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .

RUN mvn -B -q -DskipTests -Dproject.build.sourceEncoding=UTF-8 -Dproject.reporting.outputEncoding=UTF-8 dependency:go-offline

COPY src ./src

RUN mvn -B -q -DskipTests -Dproject.build.sourceEncoding=UTF-8 -Dproject.reporting.outputEncoding=UTF-8 clean package && \
    JAR_FILE="$(find target -maxdepth 1 -type f -name '*.jar' ! -name '*.original' ! -name '*plain*' | head -n 1)" && \
    test -n "$JAR_FILE" && \
    cp "$JAR_FILE" /tmp/app.jar

FROM eclipse-temurin:21-jre

WORKDIR /app

ENV TZ=UTC
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

COPY --from=build /tmp/app.jar /app/app.jar

EXPOSE 8080 8081 8082 8083 8084

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
