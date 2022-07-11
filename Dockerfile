FROM maven:3-openjdk-17-slim

RUN mkdir -p /opt/app
WORKDIR /opt/app
COPY . /opt/app
RUN mvn -B package -DskipTests && rm -rf ~/.m2/repository

EXPOSE 8080

CMD ["java", "-jar", "/opt/app/target/app.jar"]
