import apiClient from './api';

/**
 * AI 輔助服務 - 輪播圖圖片與文案生成
 */
const aiService = {
  /**
   * AI 生成圖片
   * @param {string} prompt - 圖片描述提示詞
   * @returns {Promise<{s3Key: string}>} 返回 S3 存儲路徑
   */
  async generateImage(prompt) {
    const response = await apiClient.post('/api/admin/banner/ai/generate-image', {
      prompt
    });
    return response.data; // 返回 ApiResponse.data (業務資料)
  },

  /**
   * AI 生成文案描述
   * @param {string} context - 上下文資訊 (如活動主題、商品類型)
   * @returns {Promise<{description: string}>} 返回生成的文案
   */
  async generateDescription(context) {
    const response = await apiClient.post('/api/admin/banner/ai/generate-description', {
      context
    });
    return response.data; // 返回 ApiResponse.data (業務資料)
  },

  /**
   * 下載 AI 生成的圖片
   * @param {string} s3Key - S3 存儲路徑
   * @returns {Promise<Blob>} 返回圖片 Blob 物件
   */
  async downloadImage(s3Key) {
    const response = await apiClient.get('/api/admin/banner/ai/download-image', {
      params: { s3Key },
      responseType: 'blob'
    });
    return response; // responseType: 'blob' 時,api.js 已返回 Blob
  },

  /**
   * 將 Blob 轉換為 File 物件
   * @param {Blob} blob - 圖片 Blob
   * @param {string} filename - 檔案名稱
   * @returns {File} File 物件
   */
  blobToFile(blob, filename = 'ai-generated-banner.png') {
    return new File([blob], filename, { type: blob.type || 'image/png' });
  },

  /**
   * 將 Canvas 轉換為 Blob
   * @param {HTMLCanvasElement} canvas - Canvas 元素
   * @param {string} type - 圖片類型 (預設 image/png)
   * @param {number} quality - 圖片品質 (0-1, 僅適用於 image/jpeg)
   * @returns {Promise<Blob>}
   */
  canvasToBlob(canvas, type = 'image/png', quality = 0.95) {
    return new Promise((resolve, reject) => {
      canvas.toBlob(
        (blob) => {
          if (blob) {
            resolve(blob);
          } else {
            reject(new Error('Canvas to Blob conversion failed'));
          }
        },
        type,
        quality
      );
    });
  }
};

export default aiService;
