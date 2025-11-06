import React, { useState, useRef, useEffect } from 'react';
import ReactCrop from 'react-image-crop';
import 'react-image-crop/dist/ReactCrop.css';
import aiService from '../../services/aiService';
import styles from './ImageEditor.module.css';

/**
 * 圖片編輯器 - 支援裁切、旋轉、濾鏡效果
 * @param {Object} props
 * @param {string} props.imageSrc - 原始圖片來源 (URL 或 Data URL)
 * @param {Function} props.onComplete - 編輯完成回調 (blob, file)
 * @param {Function} props.onCancel - 取消編輯回調
 */
const ImageEditor = ({ imageSrc, onComplete, onCancel }) => {
  const [crop, setCrop] = useState({
    unit: '%',
    width: 90,
    aspect: 16 / 9 // 輪播圖建議比例
  });
  const [completedCrop, setCompletedCrop] = useState(null);
  const [rotation, setRotation] = useState(0);
  const [filters, setFilters] = useState({
    brightness: 100,
    contrast: 100,
    saturation: 100,
    temperature: 0
  });
  const [isProcessing, setIsProcessing] = useState(false);

  const imgRef = useRef(null);
  const previewCanvasRef = useRef(null);

  // 當圖片載入完成時初始化裁切區域
  const onImageLoad = (e) => {
    const { width, height } = e.currentTarget;
    const cropWidth = Math.min(90, (width / width) * 100);
    setCrop({
      unit: '%',
      width: cropWidth,
      aspect: 16 / 9,
      x: 5,
      y: 5
    });
  };

  // 實時更新預覽畫布
  useEffect(() => {
    if (!completedCrop || !imgRef.current || !previewCanvasRef.current) {
      return;
    }

    const image = imgRef.current;
    const canvas = previewCanvasRef.current;
    const crop = completedCrop;

    const scaleX = image.naturalWidth / image.width;
    const scaleY = image.naturalHeight / image.height;
    const ctx = canvas.getContext('2d');

    // 計算旋轉後的畫布尺寸
    const rotationRad = (rotation * Math.PI) / 180;
    const sin = Math.abs(Math.sin(rotationRad));
    const cos = Math.abs(Math.cos(rotationRad));

    const cropWidth = crop.width * scaleX;
    const cropHeight = crop.height * scaleY;

    const rotatedWidth = cropWidth * cos + cropHeight * sin;
    const rotatedHeight = cropWidth * sin + cropHeight * cos;

    canvas.width = rotatedWidth;
    canvas.height = rotatedHeight;

    ctx.save();

    // 應用濾鏡效果
    ctx.filter = generateFilterString(filters);

    // 移動到畫布中心
    ctx.translate(canvas.width / 2, canvas.height / 2);
    ctx.rotate(rotationRad);
    ctx.translate(-cropWidth / 2, -cropHeight / 2);

    // 繪製裁切後的圖片
    ctx.drawImage(
      image,
      crop.x * scaleX,
      crop.y * scaleY,
      cropWidth,
      cropHeight,
      0,
      0,
      cropWidth,
      cropHeight
    );

    ctx.restore();
  }, [completedCrop, rotation, filters]);

  // 生成 CSS filter 字串
  const generateFilterString = (f) => {
    const tempColor = f.temperature > 0
      ? `sepia(${f.temperature / 100}) saturate(${100 + f.temperature}%)`
      : `hue-rotate(${f.temperature}deg)`;

    return `brightness(${f.brightness}%) contrast(${f.contrast}%) saturate(${f.saturation}%) ${tempColor}`;
  };

  // 處理濾鏡變更
  const handleFilterChange = (filterName, value) => {
    setFilters(prev => ({
      ...prev,
      [filterName]: parseFloat(value)
    }));
  };

  // 90° 旋轉
  const handleRotate = () => {
    setRotation((prev) => (prev + 90) % 360);
  };

  // 重置所有編輯
  const handleReset = () => {
    setRotation(0);
    setFilters({
      brightness: 100,
      contrast: 100,
      saturation: 100,
      temperature: 0
    });
    setCrop({
      unit: '%',
      width: 90,
      aspect: 16 / 9,
      x: 5,
      y: 5
    });
  };

  // 確認編輯並輸出
  const handleConfirm = async () => {
    if (!previewCanvasRef.current) {
      alert('請先完成圖片編輯');
      return;
    }

    setIsProcessing(true);
    try {
      const canvas = previewCanvasRef.current;
      const blob = await aiService.canvasToBlob(canvas);
      const file = aiService.blobToFile(blob, `edited-banner-${Date.now()}.png`);

      onComplete(blob, file);
    } catch (error) {
      console.error('圖片處理失敗:', error);
      alert('圖片處理失敗,請重試');
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <div className={styles.editorContainer}>
      <div className={styles.editorHeader}>
        <h3>編輯圖片</h3>
        <button
          className={styles.resetBtn}
          onClick={handleReset}
          type="button"
        >
          重置
        </button>
      </div>

      <div className={styles.editorBody}>
        {/* 左側: 裁切與旋轉 */}
        <div className={styles.cropSection}>
          <ReactCrop
            crop={crop}
            onChange={(c) => setCrop(c)}
            onComplete={(c) => setCompletedCrop(c)}
            aspect={16 / 9}
          >
            <img
              ref={imgRef}
              src={imageSrc}
              alt="編輯中"
              onLoad={onImageLoad}
              style={{
                transform: `rotate(${rotation}deg)`,
                maxWidth: '100%'
              }}
            />
          </ReactCrop>
          <button
            className={styles.rotateBtn}
            onClick={handleRotate}
            type="button"
          >
            ↻ 旋轉 90°
          </button>
        </div>

        {/* 右側: 濾鏡控制 */}
        <div className={styles.filterSection}>
          <h4>濾鏡調整</h4>

          <div className={styles.filterControl}>
            <label>
              亮度 (Brightness)
              <span>{filters.brightness}%</span>
            </label>
            <input
              type="range"
              min="0"
              max="200"
              value={filters.brightness}
              onChange={(e) => handleFilterChange('brightness', e.target.value)}
            />
          </div>

          <div className={styles.filterControl}>
            <label>
              對比度 (Contrast)
              <span>{filters.contrast}%</span>
            </label>
            <input
              type="range"
              min="0"
              max="200"
              value={filters.contrast}
              onChange={(e) => handleFilterChange('contrast', e.target.value)}
            />
          </div>

          <div className={styles.filterControl}>
            <label>
              飽和度 (Saturation)
              <span>{filters.saturation}%</span>
            </label>
            <input
              type="range"
              min="0"
              max="200"
              value={filters.saturation}
              onChange={(e) => handleFilterChange('saturation', e.target.value)}
            />
          </div>

          <div className={styles.filterControl}>
            <label>
              色溫 (Temperature)
              <span>{filters.temperature > 0 ? '暖' : filters.temperature < 0 ? '冷' : '中性'}</span>
            </label>
            <input
              type="range"
              min="-50"
              max="50"
              value={filters.temperature}
              onChange={(e) => handleFilterChange('temperature', e.target.value)}
            />
          </div>

          {/* 預覽畫布 */}
          <div className={styles.previewSection}>
            <h5>編輯預覽</h5>
            <canvas ref={previewCanvasRef} className={styles.previewCanvas} />
          </div>
        </div>
      </div>

      {/* 底部按鈕 */}
      <div className={styles.editorFooter}>
        <button
          className={styles.cancelBtn}
          onClick={onCancel}
          type="button"
        >
          取消
        </button>
        <button
          className={styles.confirmBtn}
          onClick={handleConfirm}
          disabled={isProcessing}
          type="button"
        >
          {isProcessing ? '處理中...' : '確認使用此圖片'}
        </button>
      </div>
    </div>
  );
};

export default ImageEditor;
