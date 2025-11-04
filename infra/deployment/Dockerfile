FROM node:24-alpine

# 安裝系統工具與後端打包環境
# - bash, git, curl: 基礎工具
# - openjdk21: Java 21 運行環境（後端打包需要）
# - maven: Maven 構建工具（後端打包需要）
# - rsync: 文件同步工具（網關部署需要）
# - openssh-client: SSH 客戶端（遠端部署需要）
RUN apk add --no-cache bash git curl openjdk21 maven rsync openssh-client

# 配置 Java 和 Maven 環境變數
ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk
ENV MAVEN_HOME=/usr/share/java/maven-3
ENV PATH="${JAVA_HOME}/bin:${MAVEN_HOME}/bin:${PATH}"
ENV MAVEN_OPTS="-Xmx2g -Xms512m -Djava.awt.headless=true"

# 全域安裝 Wrangler（前端部署工具）
RUN npm install -g wrangler

# 建立工作目錄和掛載點
WORKDIR /app
RUN mkdir -p /dist /src /output

# 暴露端口
EXPOSE 3100

# 啟動服務（透過 docker-compose 的 command 覆蓋）
CMD ["node", "index.js"]
