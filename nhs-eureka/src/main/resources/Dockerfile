FROM openjdk:8-alpine
VOLUME /tmp
COPY maven/@project.build.finalName@.jar app.jar
RUN apk add --update curl && \
    rm -rf /var/cache/apk/* && \
    touch /app.jar
EXPOSE 8761
ENTRYPOINT ["java","-jar","/app.jar"]

