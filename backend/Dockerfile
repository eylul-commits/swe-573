# compile the application
# use Maven image with Java 17
FROM maven:3.9-eclipse-temurin-17 AS build
# set working directory inside the container
WORKDIR /app
# copy the Maven project descriptor (pom.xml)
COPY pom.xml .
# copy the source code directory
COPY src ./src
# build the application JAR file, to skip tests: -DskipTests, testleri aktive ettim
RUN mvn clean package 

#run the application
# use a Java 17 runtime image
FROM eclipse-temurin:17-jre-alpine
# set the working directory
WORKDIR /app
# copy the built JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar
# expose port 8080 for external access - compose'da var aslında bunu yazmasam da olur ama good practice 
EXPOSE 8080
# define the startup command to run the app, executes command "java -jar app.jar"
ENTRYPOINT ["java", "-jar", "app.jar"]
