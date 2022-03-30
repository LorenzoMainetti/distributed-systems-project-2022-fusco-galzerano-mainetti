FROM maven

COPY ./ ./

RUN mvn package

ENTRYPOINT ["java", "-jar", "target/reliable_broadcast-1.0-SNAPSHOT.jar"]