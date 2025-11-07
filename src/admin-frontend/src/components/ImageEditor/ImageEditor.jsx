import { useState, useRef, useEffect } from 'react'
import ReactCrop from 'react-image-crop'
import 'react-image-crop/dist/ReactCrop.css'
import styles from './ImageEditor.module.css'

/**
 * 圖片編輯器 - 懸浮窗模式，支援圖片裁切
 * @param {Object} props
 * @param {string} props.imageUrl - 圖片來源 (Data URL)
 * @param {Function} props.onSave - 儲存回調 (blob)
 * @param {Function} props.onCancel - 取消回調
 */
function ImageEditor({ imageUrl, onSave, onCancel }) {
  const [crop, setCrop] = useState({
    unit: '%',
    width: 80,
    height: 45, // 80 / (16/9) = 45
    x: 10,
    y: 10
  })
  const [completedCrop, setCompletedCrop] = useState(null)
  const [isProcessing, setIsProcessing] = useState(false)

  const imgRef = useRef(null)
  const canvasRef = useRef(null)

  // 當圖片載入完成時，繪製初始預覽
  const onImageLoad = (e) => {
    const { naturalWidth, naturalHeight } = e.currentTarget

    // 根據圖片尺寸計算初始裁切區域
    const aspect = 16 / 9
    let width = 80
    let height = width / aspect

    // 確保裁切區域不超出圖片
    if (height > 90) {
      height = 90
      width = height * aspect
    }

    const x = (100 - width) / 2
    const y = (100 - height) / 2

    const initialCrop = {
      unit: '%',
      width,
      height,
      x,
      y
    }

    setCrop(initialCrop)
    setCompletedCrop(initialCrop)
  }

  // 當裁切完成時，更新預覽
  useEffect(() => {
    if (!completedCrop || !imgRef.current || !canvasRef.current) {
      return
    }

    const image = imgRef.current
    const canvas = canvasRef.current
    const crop = completedCrop

    const scaleX = image.naturalWidth / image.width
    const scaleY = image.naturalHeight / image.height

    const pixelCrop = {
      x: crop.x * scaleX,
      y: crop.y * scaleY,
      width: crop.width * scaleX,
      height: crop.height * scaleY
    }

    canvas.width = pixelCrop.width
    canvas.height = pixelCrop.height

    const ctx = canvas.getContext('2d')

    ctx.drawImage(
      image,
      pixelCrop.x,
      pixelCrop.y,
      pixelCrop.width,
      pixelCrop.height,
      0,
      0,
      pixelCrop.width,
      pixelCrop.height
    )
  }, [completedCrop])

  // 確認裁切並輸出
  const handleSave = async () => {
    const canvas = canvasRef.current

    if (!canvas || canvas.width === 0 || canvas.height === 0) {
      alert('請先完成圖片裁切')
      return
    }

    setIsProcessing(true)
    try {
      // 將 canvas 轉換為 Blob
      const blob = await new Promise((resolve, reject) => {
        canvas.toBlob(
          (blob) => {
            if (blob) {
              resolve(blob)
            } else {
              reject(new Error('圖片轉換失敗'))
            }
          },
          'image/jpeg',
          0.95
        )
      })

      // 建立 File 物件
      const file = new File([blob], `product-${Date.now()}.jpg`, {
        type: 'image/jpeg'
      })

      onSave(file)
    } catch (error) {
      console.error('圖片處理失敗:', error)
      alert('圖片處理失敗，請重試')
    } finally {
      setIsProcessing(false)
    }
  }

  return (
    <div className={styles.modalOverlay}>
      <div className={styles.modalContent}>
        {/* 標題列 */}
        <div className={styles.header}>
          <h2 className={styles.title}>裁切圖片</h2>
          <button
            onClick={onCancel}
            className={styles.closeButton}
            type="button"
            disabled={isProcessing}
          >
            ✕
          </button>
        </div>

        {/* 裁切區域 */}
        <div className={styles.cropContainer}>
          <ReactCrop
            crop={crop}
            onChange={(c) => setCrop(c)}
            onComplete={(c) => setCompletedCrop(c)}
            aspect={16 / 9}
          >
            <img
              ref={imgRef}
              src={imageUrl}
              alt="待裁切圖片"
              onLoad={onImageLoad}
              className={styles.cropImage}
            />
          </ReactCrop>
        </div>

        {/* 預覽區域（隱藏） */}
        <canvas ref={canvasRef} style={{ display: 'none' }} />

        {/* 操作按鈕 */}
        <div className={styles.footer}>
          <button
            onClick={onCancel}
            className={styles.cancelButton}
            type="button"
            disabled={isProcessing}
          >
            取消
          </button>
          <button
            onClick={handleSave}
            className={styles.saveButton}
            type="button"
            disabled={isProcessing}
          >
            {isProcessing ? '處理中...' : '確認裁切'}
          </button>
        </div>
      </div>
    </div>
  )
}

export default ImageEditor
