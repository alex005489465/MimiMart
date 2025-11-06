# Bug 修復: AI API 連接問題

## 問題描述

**錯誤訊息**:
```
AiImageGenerator.jsx:52 AI 圖片生成失敗: Error: 無法連接到伺服器
    at api.js:75:29
```

**症狀**:
- 後端日誌顯示 AI 請求成功處理 (HTTP 200)
- 前端顯示「無法連接到伺服器」錯誤
- AI 圖片生成和文案生成都無法使用

## 根本原因

**ApiResponse 資料結構理解錯誤**

**後端 ApiResponse 結構**:
```java
// AdminBannerController 返回
ApiResponse.success("圖片生成成功", new AiImageResponse(s3Key))

// 實際 HTTP Response Body
{
  "success": true,
  "code": "SUCCESS",
  "message": "圖片生成成功",
  "data": {
    "s3Key": "ai-generated/xxx.png"
  }
}
```

**前端處理流程**:
```javascript
// api.js 攔截器 (第 43 行)
return response.data  // 返回整個 ApiResponse 物件

// aiService.js (修復前 - 錯誤)
async generateImage(prompt) {
  const response = await apiClient.post(...);
  return response;  // ❌ 返回 ApiResponse,包含 success/message/data
}

// AiImageGenerator.jsx
const s3Key = response.s3Key;  // ❌ undefined! 應該是 response.data.s3Key
```

**資料流追蹤**:
```
HTTP Response: { success: true, data: { s3Key: "..." } }
    ↓ Axios response.data
api.js 返回: { success: true, data: { s3Key: "..." } }
    ↓ aiService return response (錯誤)
元件收到: { success: true, data: { s3Key: "..." } }
    ↓ 元件取值 response.s3Key
結果: undefined ❌
```

## 修復方案

**修改檔案**: `src/services/aiService.js`

### 修改 1: generateImage (第 16 行)
```javascript
// 修復前
return response;  // 返回整個 ApiResponse

// 修復後
return response.data;  // 返回 ApiResponse.data (實際業務資料)
```

### 修改 2: generateDescription (第 28 行)
```javascript
// 修復前
return response;  // 返回整個 ApiResponse

// 修復後
return response.data;  // 返回 ApiResponse.data (實際業務資料)
```

### 保持不變: downloadImage (第 41 行)
```javascript
// 保持 response.data 不變
// 因為 responseType: 'blob' 的處理方式不同
return response.data;
```

## 測試結果

✅ **建構成功**: 無錯誤或警告
✅ **API 結構正確**: 符合 ApiResponse<T> 格式
✅ **錯誤處理正常**: 正確捕獲和顯示錯誤訊息

## 使用方式

修復後,在前端元件中使用:

```javascript
// AI 圖片生成
const response = await aiService.generateImage(prompt);
console.log(response.s3Key);  // 正確存取

// AI 文案生成
const response = await aiService.generateDescription(context);
console.log(response.description);  // 正確存取
```

## 修復後的正確資料流

```
HTTP Response: { success: true, data: { s3Key: "..." } }
    ↓ Axios response.data
api.js 返回: { success: true, data: { s3Key: "..." } }
    ↓ aiService return response.data (修復)
元件收到: { s3Key: "..." }  ✅
    ↓ 元件取值 response.s3Key
結果: "ai-generated/xxx.png" ✅
```

## 技術要點

**關鍵理解**:
- **HTTP Response**: Axios 原始回應物件
- **response.data**: HTTP Response Body (ApiResponse 物件)
- **ApiResponse**: `{ success, code, message, data: T }`
- **ApiResponse.data**: 實際業務資料 (如 `{ s3Key: "..." }`)

**正確做法**:
```javascript
// aiService.js 應該返回 ApiResponse.data
return response.data;  // 讓元件直接取得業務資料

// 元件中使用
const result = await aiService.generateImage(prompt);
const s3Key = result.s3Key;  // 直接存取,無需 .data.s3Key
```

---

**修復日期**: 2025-11-06
**影響範圍**: AI 圖片生成、AI 文案生成
**相關檔案**: aiService.js, api.js
