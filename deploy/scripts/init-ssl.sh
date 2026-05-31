#!/bin/bash
# ============================================================
# SSL 证书初始化脚本 (Let's Encrypt + Certbot)
# 在服务器 /opt/gym 目录下执行
# ============================================================
set -e

if [ -z "$1" ]; then
    echo "用法: bash deploy/scripts/init-ssl.sh <你的域名> <你的邮箱>"
    echo "示例: bash deploy/scripts/init-ssl.sh gym.example.com admin@example.com"
    exit 1
fi

DOMAIN=$1
EMAIL=${2:-"admin@$DOMAIN"}

echo "域名: $DOMAIN"
echo "邮箱: $EMAIL"

# 创建 certbot 目录
mkdir -p deploy/certbot/www
mkdir -p deploy/certbot/conf

# 生成临时 HTTP-only nginx 配置（先用 HTTP 获取证书）
echo "=== 生成 HTTP 模式 Nginx 配置 ==="
cat > deploy/conf/nginx-http.conf << EOF
server {
    listen 80;
    server_name $DOMAIN;

    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    location /api/ {
        proxy_pass http://backend:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_read_timeout 120s;
    }

    location / {
        proxy_pass http://frontend:80;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
EOF

# 生成 HTTPS 配置（替换域名占位符）
echo "=== 生成 HTTPS 模式 Nginx 配置 ==="
sed "s/\${DOMAIN}/$DOMAIN/g" deploy/conf/nginx.conf > deploy/conf/nginx-ssl.conf

# 先用 HTTP 配置启动
cp deploy/conf/nginx-http.conf deploy/conf/nginx.conf
rm -f deploy/conf/nginx-http.conf

echo "=== 启动服务（HTTP 模式）==="
docker compose up -d --build

echo "等待服务就绪..."
sleep 15

echo "=== 获取 SSL 证书 ==="
docker run --rm \
    -v "$(pwd)/deploy/certbot/conf:/etc/letsencrypt" \
    -v "$(pwd)/deploy/certbot/www:/var/www/certbot" \
    certbot/certbot certonly \
    --webroot -w /var/www/certbot \
    --email "$EMAIL" \
    --domain "$DOMAIN" \
    --agree-tos \
    --non-interactive

echo "=== 切换到 HTTPS 配置 ==="
cp deploy/conf/nginx-ssl.conf deploy/conf/nginx.conf
rm -f deploy/conf/nginx-ssl.conf

docker compose restart nginx

echo ""
echo "=== SSL 配置完成 ==="
echo "访问地址: https://$DOMAIN"
echo ""
echo "添加定时任务自动续期证书:"
echo "  0 3 * * * docker run --rm -v $(pwd)/deploy/certbot/conf:/etc/letsencrypt -v $(pwd)/deploy/certbot/www:/var/www/certbot certbot/certbot renew --quiet && docker compose -f /opt/gym/docker-compose.yml restart nginx"
