# ビルド用ステージ
FROM gradle:7.3-jdk17 AS build
WORKDIR /app

COPY . .
RUN ./gradlew --no-daemon bootJar -x test

# ==== ここからデバッグ（必要なければあとで消してOK） ====
# 生成された jar の中に images が本当に入っているか出力
RUN set -eux; \
  ls -lah build/libs; \
  JAR="$(ls build/libs/*.jar | head -n1)"; \
  echo "=== list images in ${JAR} ==="; \
  jar tf "${JAR}" | grep -E 'BOOT-INF/classes/(static|public)/images/.*\.(png|jpg|webp)' || true
# ==== ここまでデバッグ ====

# 実行用ステージ（軽量）
# ここを openjdk から Temurin 系に変更
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# jar 名が一定でなくても拾えるように *.jar をコピー
COPY --from=build /app/build/libs/*.jar /app/app.jar

# Render が割り当てる PORT を使って起動
CMD ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
