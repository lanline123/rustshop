#!/bin/bash
echo "📦 开始打包 Spring Boot 项目..."
mvn clean package -DskipTests

echo "🐳 构建并启动 Docker 容器..."
docker-compose up --build
