FROM maven:3.9.0-eclipse-temurin-19 AS build
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
RUN mvn -f /usr/src/app/pom.xml clean package -DskipTests

FROM openjdk:18
ENV DEBIAN_FRONTEND noninteractive
COPY --from=build /usr/src/app/target/spring-boot-redis-cache-0.0.1-SNAPSHOT.jar /usr/app/spring-boot-redis-cache-0.0.1-SNAPSHOT.jar
COPY --from=build /usr/src/app/src/main/resources/runApplication.sh /usr/app/runApplication.sh
EXPOSE 8080
ENTRYPOINT ["/usr/app/runApplication.sh"]
