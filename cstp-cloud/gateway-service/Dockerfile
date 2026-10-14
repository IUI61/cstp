# ===== 阶段 1：构建 =====
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
# 复制整个多模块工程（父 pom + 所有模块，构建需要）
COPY . .
# 只构建本服务（-pl 指定模块，-am 连带构建父 pom）
RUN mvn -pl gateway-service -am clean package -DskipTests

# ===== 阶段 2：运行 =====
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/gateway-service/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
