#
# Build stage
#
FROM gradle:jdk21 AS build
WORKDIR /home/nestle-api/app
COPY . /home/nestle-api/app
RUN gradle clean
RUN gradle build --no-daemon

FROM alpine/java:21-jdk
RUN apk add tzdata
RUN cp /usr/share/zoneinfo/Asia/Bangkok /etc/localtime
RUN date
WORKDIR /home/nestle-api/app
EXPOSE 8080
RUN rm -rf /home/nestle-api/app/nestle-api-0.0.1.jar
COPY --from=build /home/nestle-api/app/build/libs/nestle-api-0.0.1.jar /home/nestle-api/app/nestle-api-0.0.1.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=uat", "-jar", "/home/nestle-api/app/nestle-api-0.0.1.jar"]
