FROM wspark83/springboot:openjdk8-custom

ARG JAR_FILE=target/springboot-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar"]
