# 1. 스프링 부트 3.x 및 Java 21을 지원하는 경량 리눅스 이미지를 기반으로 합니다.
FROM eclipse-temurin:21-jre-jammy

# 2. 깃허브 액션이 앞 단계에서 빌드해둔 JAR 파일의 위치를 변수로 잡습니다.
ARG JAR_FILE=build/libs/*-SNAPSHOT.jar

# 3. 가상 컴퓨터에 있는 JAR 파일을 도커 이미지 내부로 'app.jar'라는 이름으로 복사합니다.
COPY ${JAR_FILE} app.jar

# 4. 컨테이너가 실행될 때 스프링 부트를 자동으로 켜주는 명령어입니다.
ENTRYPOINT ["java", "-jar", "/app.jar"]