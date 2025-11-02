#!/bin/bash

# ========================================
# EC2 實例初始化腳本
# 安裝 Docker 和 Docker Compose
# ========================================

set -e  # 遇到錯誤時停止執行

# 記錄開始時間
echo "==================================="
echo "開始執行 User Data 腳本"
echo "時間: $(date)"
echo "==================================="

# ========================================
# 1. 更新系統套件
# ========================================
echo "[步驟 1/4] 更新系統套件..."
dnf update -y

# ========================================
# 2. 安裝 Docker
# ========================================
echo "[步驟 2/4] 安裝 Docker..."
dnf install -y docker

# 啟動 Docker 服務
echo "啟動 Docker 服務..."
systemctl start docker
systemctl enable docker

# 驗證 Docker 安裝
docker --version

# ========================================
# 3. 安裝 Docker Compose
# ========================================
echo "[步驟 3/4] 安裝 Docker Compose..."

# 取得最新版本號
DOCKER_COMPOSE_VERSION=$(curl -s https://api.github.com/repos/docker/compose/releases/latest | grep '"tag_name"' | sed -E 's/.*"([^"]+)".*/\1/')

# 下載並安裝 Docker Compose
curl -L "https://github.com/docker/compose/releases/download/${DOCKER_COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

# 設定執行權限
chmod +x /usr/local/bin/docker-compose

# 建立符號連結 (方便使用 docker-compose 指令)
ln -sf /usr/local/bin/docker-compose /usr/bin/docker-compose

# 驗證 Docker Compose 安裝
docker-compose --version

# ========================================
# 4. 配置使用者權限
# ========================================
echo "[步驟 4/4] 配置使用者權限..."

# 將 ec2-user 加入 docker 群組 (允許無需 sudo 執行 docker)
usermod -aG docker ec2-user

# ========================================
# 完成
# ========================================
echo "==================================="
echo "User Data 腳本執行完成"
echo "時間: $(date)"
echo "==================================="
echo "Docker 版本: $(docker --version)"
echo "Docker Compose 版本: $(docker-compose --version)"
echo "==================================="
echo "注意: ec2-user 需要重新登入才能無需 sudo 使用 docker"
echo "==================================="
