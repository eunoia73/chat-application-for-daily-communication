FROM openjdk:17
ARG JAR_FILE=/build/libs/*.jar
COPY ${JAR_RILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]

# 컨테이너 외부에 노출할 포트
EXPOSE 8080