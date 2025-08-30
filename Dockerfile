# Gradleを使ってビルドするためのベースイメージ
FROM gradle:7.3-jdk17 AS build

WORKDIR /app

# プロジェクトファイルをコピー
COPY . .

# Gradle Wrapperを使ってビルド
RUN ./gradlew build -x test

# 実行用の最小のJDKイメージを使用
FROM openjdk:17-jdk-slim

WORKDIR /app

# ビルドされたJARファイルをコピー
COPY --from=build /app/build/libs/your-app.jar .

# アプリケーションを実行
CMD ["java", "-jar", "your-app.jar"]
