FROM gradle:9.7.1-jdk21@sha256:c0ce93e022ea2e705332dabe090019c749356576fc8fe39c38129b2aae9ed68f AS builder
WORKDIR /workspace
COPY . .
RUN ./gradlew clean bootJar --no-daemon

FROM bellsoft/liberica-openjdk-alpine:21@sha256:16a531f9a87a9c7fe1a46895dbba6d96a900658a2a96da8b7c094a8cb476c5a3 AS runner
WORKDIR /app
COPY --from=builder /workspace/build/libs/app.jar app.jar
USER 10001
ENTRYPOINT ["java", "-jar", "app.jar"]
