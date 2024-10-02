FROM openjdk:17-alpine3.14 AS RUNNER
WORKDIR /usr/src/app/mjga
RUN mkdir -p /var/log
COPY . .
RUN chmod a+x ./gradlew
RUN ./gradlew jooqCodegen &&  \
    ./gradlew bootJar --stacktrace
EXPOSE 8080
CMD java --add-opens java.base/java.lang=ALL-UNNAMED -jar ./build/libs/app.jar
