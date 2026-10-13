FROM eclipse-temurin:21-jdk-jammy

# 设置时区和字符集
ENV TZ=Asia/Shanghai
ENV LANG=en_US.UTF-8
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建工作目录
WORKDIR /app

# 复制 jar 文件（注意：构建上下文内要存在）
COPY target/tigshop-api-0.0.1-SNAPSHOT.jar ./tigshop-api.jar

# 暴露默认端口（将由 docker-compose 动态映射）
EXPOSE 8181

# 启动命令
ENTRYPOINT ["java", \
    "--add-opens", "java.base/java.lang=ALL-UNNAMED", \
    "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED", \
    "--add-opens", "java.base/java.util=ALL-UNNAMED", \
    "--add-opens", "java.base/java.lang.invoke=ALL-UNNAMED", \
    "--add-opens", "java.base/java.io=ALL-UNNAMED", \
    "-jar", "/app/tigshop-api.jar"]
