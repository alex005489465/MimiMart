FROM phpmyadmin:latest

# 切換到 root 用戶以安裝主題
USER root

# 安裝必要工具
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# 下載並安裝官方主題
WORKDIR /tmp

# 創建主題目錄
RUN mkdir -p /var/www/html/themes

# Blueberry Theme 1.1.0
RUN wget https://files.phpmyadmin.net/themes/blueberry/1.1.0/blueberry-1.1.0.zip && \
    unzip blueberry-1.1.0.zip && \
    mv blueberry /var/www/html/themes/ && \
    rm blueberry-1.1.0.zip

# BooDark Theme 1.0.0
RUN wget https://files.phpmyadmin.net/themes/boodark/1.0.0/boodark-1.0.0.zip && \
    unzip boodark-1.0.0.zip && \
    mv boodark /var/www/html/themes/ && \
    rm boodark-1.0.0.zip

# Darkwolf Theme 5.2
RUN wget https://files.phpmyadmin.net/themes/darkwolf/5.2/darkwolf-5.2.zip && \
    unzip darkwolf-5.2.zip && \
    mv darkwolf /var/www/html/themes/ && \
    rm darkwolf-5.2.zip

# 設定權限
RUN chown -R www-data:www-data /var/www/html/themes

# 切換回原始用戶
USER www-data

WORKDIR /var/www/html
