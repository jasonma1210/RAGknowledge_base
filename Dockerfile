FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制Maven构建文件
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# 下载依赖
RUN ./mvnw dependency:go-offline -B

# 复制源代码
COPY src src

# 构建应用
RUN ./mvnw clean package -DskipTests

# 复制构建结果
COPY target/*.jar app.jar

# 创建上传目录
RUN mkdir -p /app/uploads

# 设置环境变量
ENV JAVA_OPTS="-Xms512m -Xmx2048m"

# 暴露端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]