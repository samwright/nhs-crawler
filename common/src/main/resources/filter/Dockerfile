FROM java:8
VOLUME /tmp
COPY maven/@project.build.finalName@.jar /app.jar
RUN unzip -p /app.jar BOOT-INF/classes/wait-for-eureka.sh | tr -d '\015' > /wait-for-eureka.sh && chmod +x /wait-for-eureka.sh
RUN unzip -p /app.jar BOOT-INF/classes/healthcheck.sh | tr -d '\015' > /healthcheck.sh && chmod +x healthcheck.sh
ENV REQUIRED_SERVICES="@nhs.required.services@" LOCAL_SERVICES="@nhs.local.services@"
EXPOSE 8080
ENTRYPOINT /wait-for-eureka.sh && java -jar /app.jar

