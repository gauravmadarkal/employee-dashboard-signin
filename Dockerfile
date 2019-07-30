FROM openjdk:8
COPY target/auth-user.jar /usr/userauth/auth-user.jar
WORKDIR /usr/userauth/
EXPOSE 8080
RUN sh -c "touch auth-user.jar"
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod","auth-user.jar"]
