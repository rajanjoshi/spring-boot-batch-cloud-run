This is a regular Spring Boot application using Tomcat server.

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

## Docker Build 
```
mvn package
docker build -t gcr.io/PROJECT_ID/springbatch-springboot-tomcat-appcds .
```

Run without AppCDS:
```
docker run -ti --rm gcr.io/PROJECT_ID/springbatch-springboot-tomcat-appcds
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

Run with Docker Image, without AppCDS
```
gcloud run deploy springbatch-springboot-tomcat-docker \
  --image=gcr.io/PROJECT_ID/springbatch-springboot-tomcat \
  --region=us-central1 \
  --platform managed \
  --allow-unauthenticated
```

Run with Docker Image, without AppCDS, with Tiered compilation
```
gcloud run deploy springbatch-springboot-tomcat-docker-t1 \
  --image=gcr.io/PROJECT_ID/springbatch-springboot-tomcat \
  -e JAVA_TOOL_OPTIONS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"
  --region=us-central1 \
  --platform managed \
  --allow-unauthenticated
```

