This is a Spring Boot application using Tomcat server.

POC:
ReadCSV and writeDB
ReadBigquery and writeDB

## Build
```
mvn package
```

## Run
```
java -jar target/springbatch.jar
```

## Containerize with Jib
```
mvn compile com.google.cloud.tools:jib-maven-plugin:2.4.0:build -Dimage=gcr.io/PROJECT_ID/springbatch-springboot-tomcat-jib
```

## App Engine

```
gcloud app deploy target/springbatch.jar
```

## Cloud Run
Run with Jib
```
gcloud run deploy springbatch-springboot-tomcat-jib \
  --image=gcr.io/PROJECT_ID/springbatch-springboot-tomcat-jib \
   --region=us-central1 \
   --platform managed \
   --allow-unauthenticated
```



