FROM openjdk:8-alpine
VOLUME /tmp
COPY maven/@project.build.finalName@.jar /app.jar
RUN apk add --update curl bash && rm -rf /var/cache/apk/* && \
    touch /app.jar && \
    unzip -p /app.jar BOOT-INF/classes/wait-for-eureka.sh | tr -d '\015' > /wait-for-eureka.sh && \
    chmod +x /wait-for-eureka.sh && \
    unzip -p /app.jar BOOT-INF/classes/healthcheck.sh | tr -d '\015' > /healthcheck.sh && \
    chmod +x healthcheck.sh
ENV REQUIRED_SERVICES="@nhs.required.services@" LOCAL_SERVICES="@nhs.local.services@"
HEALTHCHECK --interval=15s --timeout=30s --retries=10 CMD /healthcheck.sh
EXPOSE 8080
ENTRYPOINT /wait-for-eureka.sh && java -jar /app.jar

