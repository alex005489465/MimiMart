import React, { useState } from 'react';
import aiService from '../../services/aiService';
import styles from './AiDescriptionGenerator.module.css';

/**
 * AI 文案生成器元件
 * @param {Object} props
 * @param {Function} props.onDescriptionGenerated - 文案生成完成後的回調 (description)
 */
const AiDescriptionGenerator = ({ onDescriptionGenerated }) => {
  const [context, setContext] = useState('');
  const [isGenerating, setIsGenerating] = useState(false);
  const [generatedDescription, setGeneratedDescription] = useState('');
  const [error, setError] = useState(null);

  // 預設上下文範例
  const contextExamples = [
    '春季新品上市活動',
    '夏日清涼飲品促銷',
    '秋季居家用品特賣',
    '冬季節日限定優惠'
  ];

  // 生成文案
  const handleGenerate = async () => {
    if (!context.trim()) {
      setError('請輸入活動上下文');
      return;
    }

    setIsGenerating(true);
    setError(null);
    setGeneratedDescription('');

    try {
      const response = await aiService.generateDescription(context);
      setGeneratedDescription(response.description);
      setError(null);
    } catch (err) {
      console.error('AI 文案生成失敗:', err);
      setError(err.response?.data?.message || '文案生成失敗,請檢查 API 配置或稍後重試');
    } finally {
      setIsGenerating(false);
    }
  };

  // 使用生成的文案
  const handleUseDescription = () => {
    if (generatedDescription) {
      onDescriptionGenerated(generatedDescription);
      // 重置狀態
      setContext('');
      setGeneratedDescription('');
      setError(null);
    }
  };

  // 複製到剪貼簿
  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(generatedDescription);
      alert('已複製到剪貼簿');
    } catch (err) {
      console.error('複製失敗:', err);
      alert('複製失敗,請手動選取文字複製');
    }
  };

  // 重新生成
  const handleRegenerate = () => {
    setGeneratedDescription('');
    setError(null);
  };

  return (
    <div className={styles.generatorContainer}>
      <div className={styles.generatorHeader}>
        <h4>AI 文案生成</h4>
        <p className={styles.subtitle}>使用 Deepseek Chat 生成輪播圖標題文案</p>
      </div>

      {/* Context 輸入區 */}
      <div className={styles.inputSection}>
        <label htmlFor="aiContext">活動上下文</label>
        <input
          type="text"
          id="aiContext"
          value={context}
          onChange={(e) => setContext(e.target.value)}
          placeholder="例如: 春季新品上市活動"
          disabled={isGenerating}
        />

        {/* 快速範例 */}
        <div className={styles.examplesSection}>
          <span>快速範例:</span>
          <div className={styles.exampleChips}>
            {contextExamples.map((example, index) => (
              <button
                key={index}
                type="button"
                className={styles.exampleChip}
                onClick={() => setContext(example)}
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
          disabled={isGenerating || !context.trim()}
          type="button"
        >
          {isGenerating ? (
            <>
              <span className={styles.spinner}></span>
              生成中...
            </>
          ) : (
            '✨ 生成文案'
          )}
        </button>
      </div>

      {/* 生成結果 */}
      {generatedDescription && (
        <div className={styles.resultSection}>
          <div className={styles.descriptionBox}>
            <div className={styles.descriptionHeader}>
              <span>生成結果</span>
              <button
                className={styles.copyBtn}
                onClick={handleCopy}
                type="button"
                title="複製到剪貼簿"
              >
                📋 複製
              </button>
            </div>
            <div className={styles.descriptionContent}>
              {generatedDescription}
            </div>
          </div>

          <div className={styles.actionButtons}>
            <button
              className={styles.useBtn}
              onClick={handleUseDescription}
              type="button"
            >
              ✓ 使用此文案
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

export default AiDescriptionGenerator;
