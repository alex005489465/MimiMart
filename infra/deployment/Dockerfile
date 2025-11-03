FROM node:24-alpine

# 安裝系統工具
RUN apk add --no-cache bash git curl

# 全域安裝 Wrangler
RUN npm install -g wrangler

# 建立目錄
WORKDIR /app
RUN mkdir -p /dist /src

# 複製 API 服務
COPY server/package*.json /app/
RUN npm install --production

COPY server/ /app/

# 暴露端口
EXPOSE 3100

# 啟動服務
CMD ["node", "index.js"]
