# ビルド用ステージ
FROM gradle:7.3-jdk17 AS build
WORKDIR /app

# プロジェクトコピーしてビルド
COPY . .
RUN ./gradlew --no-daemon bootJar -x test

# === ここからデバッグ出力（必要なければ後で消す） ===
# JAR の中に static/public 配下の画像が本当に入っているかをログに表示
RUN set -eux; \
  echo "=== Built jars ==="; ls -lah build/libs; \
  JAR="$(ls build/libs/*.jar | head -n1)"; \
  echo "=== Checking packaged images inside: ${JAR} ==="; \
  jar tf "${JAR}" | grep -E 'BOOT-INF/classes/(static|public)/images/(beauty\.png|always_girl\.png)' || true
# ↑ パスが違う/別名なら、grep のファイル名を追加・変更してください
# === ここまでデバッグ ===

# 実行用ステージ（軽量）
FROM openjdk:17-jdk-slim
WORKDIR /app

# ビルド成果物をコピー（固定名）
# build/libs に生成される jar 名は <project>-<version>.jar なので、最初の1つを app.jar にコピー
COPY --from=build /app/build/libs/*.jar /app/app.jar

# 実行
ENTRYPOINT ["java","-jar","/app/app.jar"]
