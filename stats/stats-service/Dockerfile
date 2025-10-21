FROM amazoncorretto:21-alpine-jdk
COPY target/*.jar stat.jar
ENTRYPOINT ["java","-jar","/stat.jar"]