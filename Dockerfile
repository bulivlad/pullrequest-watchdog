FROM openjdk:8-jre-alpine

LABEL MAINTAINER = "Vlad Bulimac <buli.vlad@gmail.com>"

EXPOSE 9090

RUN mkdir -p /opt/watchdog

ADD /build/libs/pullrequests-watchdog.jar /opt/watchdog/pullrequests-watchdog.jar

WORKDIR /opt/watchdog

ENTRYPOINT [ "java", "-jar", "pullrequests-watchdog.jar" ]