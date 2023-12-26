FROM openjdk:17-alpine3.14
COPY --from=ghcr.io/ufoscout/docker-compose-wait:latest /wait /opt/wait
WORKDIR /usr/src/app/mjga
COPY . .
EXPOSE 8080
RUN mkdir -p /var/log
CMD /opt/wait && \
    ./gradlew generateJooq && \
    ./gradlew bootJar --stacktrace && \
    java --add-opens java.base/java.lang=ALL-UNNAMED -jar ./build/libs/app-1.0.jar
