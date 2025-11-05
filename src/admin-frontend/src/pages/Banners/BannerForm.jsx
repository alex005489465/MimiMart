import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import bannerService from '../../services/bannerService'
import { isRequired, isPositiveInteger } from '../../utils/validation'
import { formatFileSize } from '../../utils/format'
import styles from './BannerForm.module.css'

/**
 * Banner 表單頁面 (新增/編輯)
 */
function BannerForm() {
  const navigate = useNavigate()
  const { id } = useParams() // 如果有 id 代表編輯模式
  const isEditMode = Boolean(id)

  // 表單資料
  const [formData, setFormData] = useState({
    title: '',
    linkUrl: '',
    displayOrder: 1
  })

  // 圖片相關狀態
  const [imageFile, setImageFile] = useState(null)
  const [imagePreview, setImagePreview] = useState(null)
  const [existingImageUrl, setExistingImageUrl] = useState(null)

  // UI 狀態
  const [isLoading, setIsLoading] = useState(false)
  const [isLoadingData, setIsLoadingData] = useState(false)
  const [error, setError] = useState('')
  const [errors, setErrors] = useState({})

  // 編輯模式：載入現有資料
  useEffect(() => {
    if (isEditMode) {
      loadBannerData()
    }
  }, [id, isEditMode])

  const loadBannerData = async () => {
    try {
      setIsLoadingData(true)
      const data = await bannerService.getDetail(id)
      setFormData({
        title: data.title,
        linkUrl: data.linkUrl || '',
        displayOrder: data.displayOrder
      })
      setExistingImageUrl(data.imageUrl)
    } catch (err) {
      console.error('載入 Banner 資料失敗:', err)
      setError('載入資料失敗，請稍後再試')
    } finally {
      setIsLoadingData(false)
    }
  }

  // 處理輸入框變更
  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: value
    }))
    // 清除該欄位的錯誤訊息
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }))
    }
    if (error) setError('')
  }

  // 處理圖片選擇
  const handleImageChange = (e) => {
    const file = e.target.files[0]
    if (!file) return

    // 驗證檔案類型
    if (!file.type.startsWith('image/')) {
      setErrors(prev => ({
        ...prev,
        imageFile: '請選擇圖片檔案'
      }))
      return
    }

    // 驗證檔案大小 (最大 5MB)
    const maxSize = 5 * 1024 * 1024
    if (file.size > maxSize) {
      setErrors(prev => ({
        ...prev,
        imageFile: '圖片大小不可超過 5MB'
      }))
      return
    }

    setImageFile(file)
    setErrors(prev => ({
      ...prev,
      imageFile: ''
    }))

    // 生成預覽圖
    const reader = new FileReader()
    reader.onloadend = () => {
      setImagePreview(reader.result)
    }
    reader.readAsDataURL(file)
  }

  // 清除圖片
  const handleClearImage = () => {
    setImageFile(null)
    setImagePreview(null)
    const fileInput = document.getElementById('imageFile')
    if (fileInput) {
      fileInput.value = ''
    }
  }

  // 表單驗證
  const validateForm = () => {
    const newErrors = {}

    // 標題驗證
    if (!isRequired(formData.title)) {
      newErrors.title = '標題為必填項'
    }

    // 圖片驗證 (新增模式必須上傳)
    if (!isEditMode && !imageFile) {
      newErrors.imageFile = '請選擇 Banner 圖片'
    }

    // 連結 URL 驗證 (可選，但如果填寫則需為有效 URL)
    if (formData.linkUrl && formData.linkUrl.trim()) {
      try {
        new URL(formData.linkUrl)
      } catch {
        newErrors.linkUrl = '請輸入有效的 URL（需包含 http:// 或 https://）'
      }
    }

    // 顯示順序驗證
    if (!isPositiveInteger(formData.displayOrder)) {
      newErrors.displayOrder = '顯示順序必須為正整數'
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  // 提交表單
  const handleSubmit = async (e) => {
    e.preventDefault()

    if (!validateForm()) {
      return
    }

    try {
      setIsLoading(true)
      setError('')

      if (isEditMode) {
        // 編輯模式
        if (imageFile) {
          // 有上傳新圖片：使用 updateWithImage
          await bannerService.updateWithImage({
            bannerId: Number(id),
            imageFile,
            title: formData.title,
            linkUrl: formData.linkUrl || null,
            displayOrder: Number(formData.displayOrder)
          })
        } else {
          // 沒有上傳新圖片：使用 update
          await bannerService.update({
            bannerId: Number(id),
            title: formData.title,
            linkUrl: formData.linkUrl || null,
            displayOrder: Number(formData.displayOrder)
          })
        }
      } else {
        // 新增模式
        await bannerService.create({
          title: formData.title,
          imageFile,
          linkUrl: formData.linkUrl || null,
          displayOrder: Number(formData.displayOrder)
        })
      }

      // 成功後導向列表頁
      navigate('/banners')
    } catch (err) {
      console.error('儲存 Banner 失敗:', err)

      // 根據錯誤回應設定錯誤訊息
      if (err.response?.data?.message) {
        setError(err.response.data.message)
      } else if (err.message) {
        setError(err.message)
      } else {
        setError('儲存失敗，請稍後再試')
      }
    } finally {
      setIsLoading(false)
    }
  }

  // 取消返回列表頁
  const handleCancel = () => {
    navigate('/banners')
  }

  if (isLoadingData) {
    return (
      <div className={styles.container}>
        <div className={styles.loading}>載入中...</div>
      </div>
    )
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1 className={styles.title}>
          {isEditMode ? '編輯 Banner' : '新增 Banner'}
        </h1>
      </div>

      <div className={styles.formCard}>
        <form onSubmit={handleSubmit}>
          {/* 全域錯誤訊息 */}
          {error && (
            <div className={styles.errorAlert}>
              {error}
            </div>
          )}

          {/* 標題 */}
          <div className={styles.formGroup}>
            <label htmlFor="title" className={styles.label}>
              標題 <span className={styles.required}>*</span>
            </label>
            <input
              type="text"
              id="title"
              name="title"
              value={formData.title}
              onChange={handleChange}
              className={`${styles.input} ${errors.title ? styles.inputError : ''}`}
              placeholder="請輸入 Banner 標題"
              disabled={isLoading}
            />
            {errors.title && (
              <span className={styles.errorText}>{errors.title}</span>
            )}
          </div>

          {/* 圖片上傳 */}
          <div className={styles.formGroup}>
            <label htmlFor="imageFile" className={styles.label}>
              Banner 圖片 {!isEditMode && <span className={styles.required}>*</span>}
            </label>
            <div className={styles.imageUploadSection}>
              {/* 現有圖片預覽 (編輯模式) */}
              {isEditMode && existingImageUrl && !imagePreview && (
                <div className={styles.existingImage}>
                  <img src={existingImageUrl} alt="目前的 Banner" />
                  <p className={styles.imageHint}>
                    目前圖片（如需更換，請上傳新圖片）
                  </p>
                </div>
              )}

              {/* 新圖片預覽 */}
              {imagePreview && (
                <div className={styles.imagePreview}>
                  <img src={imagePreview} alt="預覽" />
                  <button
                    type="button"
                    onClick={handleClearImage}
                    className={styles.clearImageButton}
                    disabled={isLoading}
                  >
                    清除圖片
                  </button>
                </div>
              )}

              {/* 上傳按鈕 */}
              <div className={styles.uploadArea}>
                <input
                  type="file"
                  id="imageFile"
                  accept="image/*"
                  onChange={handleImageChange}
                  className={styles.fileInput}
                  disabled={isLoading}
                />
                <label htmlFor="imageFile" className={styles.uploadLabel}>
                  <svg
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    fill="none"
                    xmlns="http://www.w3.org/2000/svg"
                  >
                    <path
                      d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    />
                    <path
                      d="M17 8L12 3L7 8"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    />
                    <path
                      d="M12 3V15"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    />
                  </svg>
                  <span>點擊選擇圖片</span>
                  <span className={styles.uploadHint}>
                    {imageFile
                      ? `${imageFile.name} (${formatFileSize(imageFile.size)})`
                      : '支援 JPG、PNG、GIF，大小不超過 5MB'}
                  </span>
                </label>
              </div>

              {errors.imageFile && (
                <span className={styles.errorText}>{errors.imageFile}</span>
              )}
            </div>
          </div>

          {/* 跳轉連結 */}
          <div className={styles.formGroup}>
            <label htmlFor="linkUrl" className={styles.label}>
              跳轉連結 <span className={styles.optional}>（選填）</span>
            </label>
            <input
              type="text"
              id="linkUrl"
              name="linkUrl"
              value={formData.linkUrl}
              onChange={handleChange}
              className={`${styles.input} ${errors.linkUrl ? styles.inputError : ''}`}
              placeholder="例如：https://www.example.com/products"
              disabled={isLoading}
            />
            {errors.linkUrl && (
              <span className={styles.errorText}>{errors.linkUrl}</span>
            )}
            <p className={styles.fieldHint}>
              使用者點擊 Banner 後導向的頁面，留空則無法點擊
            </p>
          </div>

          {/* 顯示順序 */}
          <div className={styles.formGroup}>
            <label htmlFor="displayOrder" className={styles.label}>
              顯示順序 <span className={styles.required}>*</span>
            </label>
            <input
              type="number"
              id="displayOrder"
              name="displayOrder"
              value={formData.displayOrder}
              onChange={handleChange}
              className={`${styles.input} ${errors.displayOrder ? styles.inputError : ''}`}
              min="1"
              disabled={isLoading}
            />
            {errors.displayOrder && (
              <span className={styles.errorText}>{errors.displayOrder}</span>
            )}
            <p className={styles.fieldHint}>
              數字越小排序越前面
            </p>
          </div>

          {/* 按鈕區 */}
          <div className={styles.buttonGroup}>
            <button
              type="button"
              onClick={handleCancel}
              className={styles.cancelButton}
              disabled={isLoading}
            >
              取消
            </button>
            <button
              type="submit"
              className={styles.submitButton}
              disabled={isLoading}
            >
              {isLoading ? '儲存中...' : (isEditMode ? '儲存變更' : '建立 Banner')}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default BannerForm
