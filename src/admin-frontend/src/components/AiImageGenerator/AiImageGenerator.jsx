import React, { useState } from 'react';
import aiService from '../../services/aiService';
import ImageEditor from '../ImageEditor/ImageEditor';
import styles from './AiImageGenerator.module.css';

/**
 * AI 圖片生成器元件
 * @param {Object} props
 * @param {Function} props.onImageGenerated - 圖片生成並編輯完成後的回調 (file)
 */
const AiImageGenerator = ({ onImageGenerated }) => {
  const [prompt, setPrompt] = useState('');
  const [isGenerating, setIsGenerating] = useState(false);
  const [generatedImageUrl, setGeneratedImageUrl] = useState(null);
  const [generatedS3Key, setGeneratedS3Key] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [imageBlob, setImageBlob] = useState(null);
  const [error, setError] = useState(null);

  // 預設提示詞範例
  const promptExamples = [
    '春季促銷活動,鮮花盛開的商店場景',
    '夏日清涼飲品,海灘度假氛圍',
    '秋季新品上市,溫馨家居場景',
    '冬季節日慶典,溫暖聚會氣氛'
  ];

  // 生成圖片
  const handleGenerate = async () => {
    if (!prompt.trim()) {
      setError('請輸入圖片描述');
      return;
    }

    setIsGenerating(true);
    setError(null);

    try {
      // 呼叫 AI 生成 API
      const response = await aiService.generateImage(prompt);
      const s3Key = response.s3Key;

      // 下載生成的圖片
      const blob = await aiService.downloadImage(s3Key);
      const imageUrl = URL.createObjectURL(blob);

      setGeneratedImageUrl(imageUrl);
      setGeneratedS3Key(s3Key);
      setImageBlob(blob);
      setError(null);
    } catch (err) {
      console.error('AI 圖片生成失敗:', err);
      setError(err.response?.data?.message || '圖片生成失敗,請檢查 API 配置或稍後重試');
    } finally {
      setIsGenerating(false);
    }
  };

  // 開始編輯圖片
  const handleEditImage = () => {
    setIsEditing(true);
  };

  // 編輯完成
  const handleEditComplete = (blob, file) => {
    setIsEditing(false);
    onImageGenerated(file);
    // 清理狀態
    resetState();
  };

  // 取消編輯
  const handleEditCancel = () => {
    setIsEditing(false);
  };

  // 直接使用原圖 (不編輯)
  const handleUseOriginal = () => {
    if (!imageBlob) return;
    const file = aiService.blobToFile(imageBlob, `ai-banner-${Date.now()}.png`);
    onImageGenerated(file);
    resetState();
  };

  // 重置狀態
  const resetState = () => {
    setPrompt('');
    setGeneratedImageUrl(null);
    setGeneratedS3Key(null);
    setImageBlob(null);
    setError(null);
  };

  // 重新生成
  const handleRegenerate = () => {
    setGeneratedImageUrl(null);
    setGeneratedS3Key(null);
    setImageBlob(null);
    setError(null);
  };

  // 如果正在編輯,顯示編輯器
  if (isEditing && generatedImageUrl) {
    return (
      <ImageEditor
        imageSrc={generatedImageUrl}
        onComplete={handleEditComplete}
        onCancel={handleEditCancel}
      />
    );
  }

  return (
    <div className={styles.generatorContainer}>
      <div className={styles.generatorHeader}>
        <h3>AI 圖片生成</h3>
        <p className={styles.subtitle}>使用 DALL-E 3 生成輪播圖圖片 (約 $0.04 USD/張)</p>
      </div>

      {/* Prompt 輸入區 */}
      <div className={styles.promptSection}>
        <label htmlFor="aiPrompt">圖片描述 (Prompt)</label>
        <textarea
          id="aiPrompt"
          value={prompt}
          onChange={(e) => setPrompt(e.target.value)}
          placeholder="請描述您想要的輪播圖內容,例如: 春季新品促銷,櫻花盛開的商店櫥窗,溫馨明亮的氛圍"
          rows={4}
          disabled={isGenerating}
        />

        {/* 快速提示詞 */}
        <div className={styles.examplesSection}>
          <span>快速範例:</span>
          <div className={styles.exampleChips}>
            {promptExamples.map((example, index) => (
              <button
                key={index}
                type="button"
                className={styles.exampleChip}
                onClick={() => setPrompt(example)}
                disabled={isGenerating}
              >
                {example}
              </button>
            ))}
          </div>
        </div>

        {/* 錯誤訊息 */}
        {error && (
          <div className={styles.errorMessage}>
            ⚠️ {error}
          </div>
        )}

        {/* 生成按鈕 */}
        <button
          className={styles.generateBtn}
          onClick={handleGenerate}
          disabled={isGenerating || !prompt.trim()}
          type="button"
        >
          {isGenerating ? (
            <>
              <span className={styles.spinner}></span>
              生成中... (約需 10-30 秒)
            </>
          ) : (
            '🎨 生成圖片'
          )}
        </button>
      </div>

      {/* 生成結果預覽 */}
      {generatedImageUrl && !isEditing && (
        <div className={styles.resultSection}>
          <h4>生成結果</h4>
          <div className={styles.imagePreview}>
            <img src={generatedImageUrl} alt="AI 生成的圖片" />
          </div>

          <div className={styles.actionButtons}>
            <button
              className={styles.editBtn}
              onClick={handleEditImage}
              type="button"
            >
              ✏️ 編輯圖片 (裁切/濾鏡)
            </button>
            <button
              className={styles.useBtn}
              onClick={handleUseOriginal}
              type="button"
            >
              ✓ 直接使用此圖片
            </button>
            <button
              className={styles.regenerateBtn}
              onClick={handleRegenerate}
              type="button"
            >
              🔄 重新生成
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default AiImageGenerator;
