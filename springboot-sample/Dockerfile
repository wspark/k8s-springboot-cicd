FROM wspark83/springboot:openjdk8-custom

# maintainer
LABEL maintainer="jjomacson@gmail.com"
# timezone setting
ENV TZ=Asia/Seoul
# root user
USER root
# for application log
RUN mkdir -p /logs
# gradle
ARG JAR_FILE=build/libs/springboot-sample-0.0.1-SNAPSHOT.jar
# maven
#ARG JAR_FILE=target/springboot-sample-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} /app.jar

RUN chown jboss:root /logs
RUN chown jboss:root /app.jar

# jboss user
USER 185

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar"]