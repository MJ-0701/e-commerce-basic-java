# syntax=docker/dockerfile:1

##############################
# Builder Stage
##############################
FROM gradle:8.11.1-jdk17 AS builder

# 필요한 패키지 설치
RUN apt-get update && \
    apt-get install -y --no-install-recommends bash curl zip && \
    rm -rf /var/lib/apt/lists/*

# timezone 설정
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 프로젝트 루트 전체를 빌드 컨텍스트로 사용
WORKDIR /app
COPY . .

# Gradle Wrapper 실행 권한 추가
RUN chmod +x ./gradlew

# 종속성 다운로드 (실패해도 계속 진행)
RUN ./gradlew --no-daemon dependencies || true

# user-service 모듈만 빌드 (다른 모듈은 제외)
RUN ./gradlew clean bootJar --no-daemon

##############################
# Runtime Stage
##############################
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

ARG ENVIRONMENT
ENV SPRING_PROFILES_ACTIVE=${ENVIRONMENT}


# 빌드 단계에서 생성된 user-service JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "app.jar"]