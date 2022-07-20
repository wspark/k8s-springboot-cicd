FROM wspark83/springboot:openjdk8-custom

# gradle
ARG JAR_FILE=build/libs/springboot-sample-0.0.1-SNAPSHOT.jar

# maven
#ARG JAR_FILE=target/springboot-sample-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar"]
