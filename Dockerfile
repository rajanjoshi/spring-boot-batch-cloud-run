FROM adoptopenjdk:8-jre-hotspot

COPY target/springbatch.jar /springbatch.jar

ENTRYPOINT java -jar springbatch.jar
