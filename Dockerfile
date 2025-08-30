# ビルド用ステージ
FROM gradle:7.3-jdk17 AS build
WORKDIR /app

# プロジェクトコピーしてビルド
COPY . .
RUN ./gradlew bootJar -x test

# 実行用ステージ（軽量）
FROM openjdk:17-jdk-slim
WORKDIR /app

# ビルド成果物をコピー（固定名）
COPY --from=build /app/build/libs/app.jar /app/app.jar

# 実行
ENTRYPOINT ["java","-jar","/app/app.jar"]
