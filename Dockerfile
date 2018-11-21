# JAVA
FROM openjdk:8-jre-alpine

# application
ENV VERS=0.0.4
ENV wdir triplifier
WORKDIR $wdir

ARG conf
run echo "$conf"
ADD conf/ ${conf}
ADD target/DUMP/ target/DUMP/
ADD data/ data/
ADD db/	db/
ADD src/main/swagger-ui src/main/swagger-ui
ADD target/libs /usr/share/triplifier/lib
ADD target/triplifier-${VERS}.jar /usr/share/triplifier/triplifier-${VERS}.jar

ENTRYPOINT ["/usr/bin/java", "-cp", "/usr/share/triplifier/lib/*:/usr/share/triplifier/triplifier-0.0.4.jar", "triplifier.main.MainHTTPTriplifier"]

EXPOSE 7777