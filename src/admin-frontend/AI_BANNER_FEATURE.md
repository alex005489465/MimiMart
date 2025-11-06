# AI 輪播圖增強功能使用說明

## 功能概述

本次更新為輪播圖管理功能新增了 AI 輔助生成能力,包括:

- **AI 圖片生成**: 使用 OpenAI DALL-E 3 生成輪播圖圖片
- **AI 文案生成**: 使用 Deepseek Chat 生成輪播圖標題文案
- **圖片編輯器**: 支援裁切、旋轉、濾鏡效果

## 核心元件

### 1. 服務層 (`aiService.js`)

位置: `src/services/aiService.js`

封裝了以下功能:
- `generateImage(prompt)` - AI 生成圖片
- `generateDescription(context)` - AI 生成文案
- `downloadImage(s3Key)` - 下載 AI 生成的圖片
- `blobToFile(blob, filename)` - Blob 轉 File 工具
- `canvasToBlob(canvas, type, quality)` - Canvas 轉 Blob 工具

### 2. 圖片編輯器 (`ImageEditor`)

位置: `src/components/ImageEditor/`

功能特性:
- **裁切功能**: 使用 `react-image-crop`,預設 16:9 比例
- **旋轉功能**: 90° 旋轉
- **濾鏡效果**:
  - 亮度 (Brightness): 0-200%
  - 對比度 (Contrast): 0-200%
  - 飽和度 (Saturation): 0-200%
  - 色溫 (Temperature): -50 ~ +50

### 3. AI 圖片生成器 (`AiImageGenerator`)

位置: `src/components/AiImageGenerator/`

使用流程:
1. 輸入 Prompt 描述圖片內容
2. 點擊「生成圖片」按鈕
3. 等待 10-30 秒生成完成
4. 選擇「編輯圖片」或「直接使用此圖片」
5. 編輯完成後自動填入表單

注意事項:
- 每次生成約 $0.04 USD
- 圖片尺寸: 1024x1024
- 超時時間: 60 秒

### 4. AI 文案生成器 (`AiDescriptionGenerator`)

位置: `src/components/AiDescriptionGenerator/`

使用流程:
1. 輸入活動上下文資訊
2. 點擊「生成文案」按鈕
3. 生成完成後點擊「使用此文案」自動填入標題欄位

## 整合到 BannerForm

### Tab 切換設計

圖片上傳區域新增了兩個 Tab:
- **上傳圖片**: 傳統的檔案上傳方式
- **AI 生成**: 使用 AI 生成並編輯圖片

### 圖片處理流程

```
AI 生成圖片 (返回 s3Key)
    ↓
透過後端 API 下載為 Blob
    ↓
進入 ImageEditor 進行編輯
    ↓
Canvas 處理 (裁切/旋轉/濾鏡)
    ↓
輸出為 Blob → 轉換為 File 物件
    ↓
自動填入 imageFile state
    ↓
透過正常的上傳端點提交
```

### 回調處理

```javascript
// AI 圖片生成完成
handleAiImageGenerated(file) {
  setImageFile(file)
  setImagePreview(...)
  setActiveImageTab('upload') // 切回上傳 Tab 顯示預覽
}

// AI 文案生成完成
handleAiDescriptionGenerated(description) {
  setFormData({ ...formData, title: description })
}
```

## 後端 API 端點

### AI 圖片生成
```
POST /api/admin/banner/ai/generate-image
Body: { "prompt": "圖片描述" }
Response: { "s3Key": "ai-generated/xxx.png" }
```

### AI 文案生成
```
POST /api/admin/banner/ai/generate-description
Body: { "context": "活動主題" }
Response: { "description": "生成的文案" }
```

### 下載 AI 圖片
```
GET /api/admin/banner/ai/download-image?s3Key={key}
Response: 圖片二進制數據 (Blob)
```

## 環境變數配置

確保後端 `.env` 已設定以下環境變數:

```env
# OpenAI 配置
OPENAI_API_KEY=sk-xxx
OPENAI_API_URL=https://api.openai.com/v1
OPENAI_MODEL=dall-e-3
OPENAI_IMAGE_SIZE=1024x1024
OPENAI_IMAGE_QUALITY=standard

# Deepseek 配置
DEEPSEEK_API_KEY=sk-xxx
DEEPSEEK_API_URL=https://api.deepseek.com/v1
DEEPSEEK_MODEL=deepseek-chat
```

## 樣式設計

### 配色與主題

- 使用專案既有的 CSS 變數系統
- 支援深色模式 (`[data-theme="dark"]`)
- 響應式設計,適配手機與平板

### Tab 樣式

- 下方邊框指示當前選中 Tab
- Hover 效果顯示互動回饋
- Active 狀態使用主色調 (--primary-color)

### 編輯器樣式

- 左右分欄布局 (裁切區 | 濾鏡控制)
- 實時預覽畫布
- 滑桿控制濾鏡參數

## 使用建議

### Prompt 撰寫技巧

好的 Prompt 範例:
- ✅ "春季新品上市活動,櫻花盛開的商店櫥窗,溫馨明亮的氛圍"
- ✅ "夏日清涼飲品促銷,海灘度假場景,藍色系主色調"
- ✅ "冬季節日慶典,聖誕裝飾,溫暖燈光,家庭聚會氛圍"

避免:
- ❌ 過於簡短: "春季促銷"
- ❌ 過於抽象: "好看的圖片"

### 圖片編輯建議

1. **裁切**: 調整為適合輪播圖的 16:9 比例
2. **旋轉**: 修正圖片方向
3. **濾鏡**:
   - 亮度: 建議 90-110%
   - 對比度: 建議 95-105%
   - 飽和度: 建議 100-110%
   - 色溫: 根據品牌色調微調

## 已知限制

1. **生成時間**: AI 圖片生成需要 10-30 秒,請耐心等待
2. **圖片尺寸**: DALL-E 3 固定生成 1024x1024,需透過裁切調整比例
3. **成本控制**: 每次生成約 $0.04 USD,建議加入確認提示
4. **私有存儲**: AI 生成的圖片暫存在私有 bucket,需透過後端 API 下載

## 測試檢查清單

- [x] AI 圖片生成功能正常
- [x] AI 文案生成功能正常
- [x] 圖片裁切功能正常
- [x] 圖片旋轉功能正常
- [x] 濾鏡效果正常
- [x] 編輯完成後自動填入表單
- [x] Tab 切換正常
- [x] 響應式設計適配
- [x] 建構成功無錯誤

## 後續優化建議

1. **成本提醒**: 在生成前顯示成本提示對話框
2. **歷史記錄**: 顯示最近 5-10 筆 AI 生成記錄供快速選用
3. **批次生成**: 一次生成多個圖片供選擇
4. **更多濾鏡**: 新增模糊、銳化、去噪等高級濾鏡
5. **文字疊加**: 在圖片上直接添加文字標題
6. **範本系統**: 預設多組 Prompt 範本

## 技術棧

- React 19
- react-image-crop 11.0.7
- Canvas API
- FileReader API
- OpenAI DALL-E 3
- Deepseek Chat
- AWS S3

---

**建立日期**: 2025-11-06
**版本**: 1.0.0
**作者**: Claude Code
