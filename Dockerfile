FROM openjdk

WORKDIR /app

EXPOSE 8080 5005

CMD ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "/app/mthesis-provenance-traversal-1.0-SNAPSHOT.jar"]