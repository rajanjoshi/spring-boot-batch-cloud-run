FROM maven:3.8-jdk-11 as builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package deploy -DskipTests

FROM adoptopenjdk/openjdk8:alpine-jre
COPY --from=builder /app/target/springbatch.jar /springbatch.jar
CMD ["java", "-jar", "/springbatch.jar"]
