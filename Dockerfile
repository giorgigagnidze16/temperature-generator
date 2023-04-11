FROM openjdk:17-alpine
ADD target/generator-jar-with-dependencies.jar generator.jar
ENTRYPOINT ["java","-jar","/generator.jar"]
