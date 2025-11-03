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
# 2. 安裝 Docker 28.5.1
# ========================================
echo "[步驟 2/4] 安裝 Docker 28.5.1..."

# 先安裝 Amazon Linux 版本以獲取 systemd 服務配置
dnf install -y docker-25.0.13-1.amzn2023.0.1

# 下載並替換為 Docker 28.5.1 二進制檔案
cd /tmp
curl -fsSL https://download.docker.com/linux/static/stable/aarch64/docker-28.5.1.tgz -o docker.tgz
tar xzf docker.tgz
systemctl stop docker
cp /tmp/docker/* /usr/bin/
rm -rf /tmp/docker /tmp/docker.tgz

# 啟動 Docker 服務
echo "啟動 Docker 服務..."
systemctl start docker
systemctl enable docker

# 驗證 Docker 安裝
docker --version

# ========================================
# 3. 安裝 Docker Compose v2.40.2
# ========================================
echo "[步驟 3/4] 安裝 Docker Compose v2.40.2..."

# 下載並安裝 Docker Compose v2.40.2
curl -L "https://github.com/docker/compose/releases/download/v2.40.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

# 設定執行權限
chmod +x /usr/local/bin/docker-compose

# 建立符號連結 (方便使用 docker-compose 指令)
ln -sf /usr/local/bin/docker-compose /usr/bin/docker-compose

# 驗證 Docker Compose 安裝
docker-compose --version

# ========================================
# 4. 安裝 Docker Buildx
# ========================================
echo "[步驟 4/5] 安裝 Docker Buildx..."

# 為 ec2-user 建立 Docker CLI 插件目錄
mkdir -p /home/ec2-user/.docker/cli-plugins

# 取得最新 Buildx 版本並下載
BUILDX_VERSION=$(curl -s https://api.github.com/repos/docker/buildx/releases/latest | grep '"tag_name"' | sed -E 's/.*"([^"]+)".*/\1/')
curl -L "https://github.com/docker/buildx/releases/download/${BUILDX_VERSION}/buildx-${BUILDX_VERSION}.linux-arm64" -o /home/ec2-user/.docker/cli-plugins/docker-buildx
chmod +x /home/ec2-user/.docker/cli-plugins/docker-buildx
chown -R ec2-user:ec2-user /home/ec2-user/.docker

# 驗證 Buildx 安裝
su - ec2-user -c "docker buildx version"

# ========================================
# 5. 配置使用者權限
# ========================================
echo "[步驟 5/5] 配置使用者權限..."

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
