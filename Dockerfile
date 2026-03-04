ARG BASE_IMAGE=ghcr.io/ministryofjustice/hmpps-eclipse-temurin:25-jre-jammy
FROM --platform=$BUILDPLATFORM ${BASE_IMAGE} AS builder

ARG BUILD_NUMBER
ENV BUILD_NUMBER=${BUILD_NUMBER:-1_0_0}

WORKDIR /builder
COPY calculate-journey-variable-payments-${BUILD_NUMBER}.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --destination extracted

FROM ${BASE_IMAGE}

ARG BUILD_NUMBER
ENV BUILD_NUMBER=${BUILD_NUMBER:-1_0_0}

# Install AWS RDS Root cert into Java truststore
ADD --chown=appuser:appgroup https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem /home/appuser/.postgresql/root.crt

WORKDIR /app
COPY --chown=appuser:appgroup build/libs/calculate-journey-variable*.jar /app/app.jar
COPY --chown=appuser:appgroup build/libs/applicationinsights-agent*.jar /app/agent.jar
COPY --chown=appuser:appgroup applicationinsights.dev.json /app
COPY --chown=appuser:appgroup applicationinsights.json /app
COPY --chown=appuser:appgroup run.sh /app

ENTRYPOINT ["java", "-XX:+ExitOnOutOfMemoryError", "-XX:+AlwaysActAsServerClassMachine", "-javaagent:agent.jar", "-jar", "app.jar"]