#!/bin/bash

# 1. 接收参数：版本号和问候语
VERSION=$1
MESSAGE=$2

if [ -z "$VERSION" ]; then
  echo "❌ 错误: 请输入版本号. 用法: ./pipeline.sh <version> <message>"
  exit 1
fi

echo "🚀 [阶段 1/3] 开始编译 Java 代码..."
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
  echo "❌ 编译失败！停止流水线。"
  exit 1
fi

echo "🐳 [阶段 2/3] 开始构建 Docker 镜像 (Tags: $VERSION)..."
DOCKER_BUILDKIT=0 docker build -t k8s-demo:$VERSION .
if [ $? -ne 0 ]; then
  echo "❌ 镜像构建失败！"
  exit 1
fi

echo "☸️  [阶段 3/3] 发布到 Kubernetes (Helm)..."
# 使用 Helm 更新，动态传入镜像 tag 和问候语
helm upgrade my-demo-app ./my-chart \
  --set image.tag=$VERSION \
  --set appConfig.greeting="$MESSAGE" \
  --wait

echo "✅ 发布完成！当前版本: $VERSION"