#!/bin/bash
# ============================================================
# 腾跃健身 - 阿里云 ECS 初始化脚本
# 适用: CentOS 7/8, Alibaba Cloud Linux, Ubuntu 20.04+
# ============================================================
set -e

echo "=== 1. 安装 Docker ==="
if ! command -v docker &> /dev/null; then
    curl -fsSL https://get.docker.com | bash
    systemctl enable docker
    systemctl start docker
    echo "Docker 安装完成"
else
    echo "Docker 已安装: $(docker --version)"
fi

echo ""
echo "=== 2. 安装 Docker Compose ==="
if ! docker compose version &> /dev/null; then
    echo "Docker Compose 已内置在 Docker 中，跳过"
fi

echo ""
echo "=== 3. 创建项目目录 ==="
mkdir -p /opt/gym
echo "项目目录: /opt/gym"

echo ""
echo "=== 4. 上传项目文件 ==="
echo "请手动执行以下命令将项目文件上传到服务器:"
echo ""
echo "  scp -r \\"
echo "    docker-compose.yml \\"
echo "    .env \\"
echo "    sql/ \\"
echo "    backend/Dockerfile backend/pom.xml backend/src/ \\"
echo "    frontend/Dockerfile frontend/nginx.conf frontend/package.json frontend/src/ frontend/index.html frontend/vite.config.ts frontend/tsconfig.json frontend/tailwind.config.js frontend/postcss.config.js \\"
echo "    deploy/ \\"
echo "    root@<ECS公网IP>:/opt/gym/"
echo ""

echo "=== 初始化完成 ==="
echo "接下来执行: cd /opt/gym && bash deploy/scripts/init-ssl.sh"
