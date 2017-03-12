FROM openjdk:8-alpine
VOLUME /tmp
COPY maven/@project.build.finalName@.jar app.jar
RUN apk add --update curl && \
    rm -rf /var/cache/apk/* && \
    touch /app.jar
EXPOSE 8761
HEALTHCHECK --interval=15s --timeout=30s --retries=10 \
    CMD curl -f http://localhost:8761 || exit 1
ENTRYPOINT ["java","-jar","/app.jar"]

