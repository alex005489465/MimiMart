import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import productService from '../../services/productService'
import categoryService from '../../services/categoryService'
import ImageEditor from '../../components/ImageEditor/ImageEditor'
import { isRequired, isPositiveNumber } from '../../utils/validation'
import { formatFileSize } from '../../utils/format'
import styles from './ProductForm.module.css'

/**
 * 商品表單頁面 (新增/編輯)
 */
function ProductForm() {
  const navigate = useNavigate()
  const { id } = useParams() // 如果有 id 代表編輯模式
  const isEditMode = Boolean(id)

  // 表單資料
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    price: '',
    categoryId: '',
    publishedAt: '',
    unpublishedAt: ''
  })

  // 分類列表
  const [categories, setCategories] = useState([])

  // 圖片相關狀態
  const [imageFile, setImageFile] = useState(null)
  const [imagePreview, setImagePreview] = useState(null)
  const [existingImageUrl, setExistingImageUrl] = useState(null)
  const [showImageEditor, setShowImageEditor] = useState(false)
  const [selectedImageForEdit, setSelectedImageForEdit] = useState(null)

  // UI 狀態
  const [isLoading, setIsLoading] = useState(false)
  const [isLoadingData, setIsLoadingData] = useState(false)
  const [isUploadingImage, setIsUploadingImage] = useState(false)
  const [error, setError] = useState('')
  const [errors, setErrors] = useState({})

  // 載入分類列表
  useEffect(() => {
    loadCategories()
  }, [])

  // 編輯模式：載入現有資料
  useEffect(() => {
    if (isEditMode) {
      loadProductData()
    }
  }, [id, isEditMode])

  const loadCategories = async () => {
    try {
      const data = await categoryService.getAll()
      setCategories(data)
    } catch (err) {
      console.error('載入分類列表失敗:', err)
    }
  }

  const loadProductData = async () => {
    try {
      setIsLoadingData(true)
      const data = await productService.getDetail(id)
      setFormData({
        name: data.name,
        description: data.description || '',
        price: data.price,
        categoryId: data.categoryId,
        publishedAt: data.publishedAt ? formatDateTimeLocal(data.publishedAt) : '',
        unpublishedAt: data.unpublishedAt ? formatDateTimeLocal(data.unpublishedAt) : ''
      })
      setExistingImageUrl(data.imageUrl)
    } catch (err) {
      console.error('載入商品資料失敗:', err)
      setError('載入資料失敗，請稍後再試')
    } finally {
      setIsLoadingData(false)
    }
  }

  // 格式化日期時間為 datetime-local input 格式
  const formatDateTimeLocal = (dateTimeString) => {
    if (!dateTimeString) return ''
    // ISO 8601: "2025-01-01T10:00:00" -> "2025-01-01T10:00"
    return dateTimeString.slice(0, 16)
  }

  // 處理輸入框變更
  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]: value
    }))
    // 清除該欄位的錯誤訊息
    if (errors[name]) {
      setErrors((prev) => ({
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
      setErrors((prev) => ({
        ...prev,
        imageFile: '請選擇圖片檔案'
      }))
      return
    }

    // 驗證檔案大小 (最大 5MB)
    const maxSize = 5 * 1024 * 1024
    if (file.size > maxSize) {
      setErrors((prev) => ({
        ...prev,
        imageFile: '圖片大小不可超過 5MB'
      }))
      return
    }

    // 開啟圖片編輯器
    const reader = new FileReader()
    reader.onloadend = () => {
      setSelectedImageForEdit(reader.result)
      setShowImageEditor(true)
    }
    reader.readAsDataURL(file)
  }

  // 圖片編輯完成回調
  const handleImageEdited = async (editedFile) => {
    try {
      setIsUploadingImage(true)
      setErrors((prev) => ({
        ...prev,
        imageFile: ''
      }))

      // 上傳圖片到 S3
      const uploadResult = await productService.uploadImage(editedFile)

      // 設定圖片 URL
      if (uploadResult.imageUrl) {
        setImageFile(null)
        setImagePreview(uploadResult.imageUrl)
        setExistingImageUrl(uploadResult.imageUrl)

        // 如果是編輯模式，立即更新商品圖片
        if (isEditMode) {
          await productService.update({
            productId: Number(id),
            ...formData,
            imageUrl: uploadResult.imageUrl,
            publishedAt: formData.publishedAt || null,
            unpublishedAt: formData.unpublishedAt || null
          })
        }
      }
    } catch (err) {
      console.error('上傳圖片失敗:', err)
      setErrors((prev) => ({
        ...prev,
        imageFile: '上傳圖片失敗，請稍後再試'
      }))
    } finally {
      setIsUploadingImage(false)
      setShowImageEditor(false)
      setSelectedImageForEdit(null)
    }
  }

  // 關閉圖片編輯器
  const handleImageEditorClose = () => {
    setShowImageEditor(false)
    setSelectedImageForEdit(null)
    // 清除 file input
    const fileInput = document.getElementById('imageFile')
    if (fileInput) {
      fileInput.value = ''
    }
  }

  // 清除圖片
  const handleClearImage = async () => {
    if (existingImageUrl) {
      try {
        await productService.deleteImage(existingImageUrl)
      } catch (err) {
        console.error('刪除圖片失敗:', err)
      }
    }
    setImageFile(null)
    setImagePreview(null)
    setExistingImageUrl(null)
    const fileInput = document.getElementById('imageFile')
    if (fileInput) {
      fileInput.value = ''
    }
  }

  // 表單驗證
  const validateForm = () => {
    const newErrors = {}

    // 商品名稱驗證
    if (!isRequired(formData.name)) {
      newErrors.name = '商品名稱為必填項'
    } else if (formData.name.length > 200) {
      newErrors.name = '商品名稱不可超過 200 字元'
    }

    // 價格驗證
    if (!isRequired(formData.price)) {
      newErrors.price = '價格為必填項'
    } else if (!isPositiveNumber(formData.price)) {
      newErrors.price = '價格必須為正數'
    } else {
      const price = Number(formData.price)
      if (price < 0.01 || price > 99999999.99) {
        newErrors.price = '價格範圍為 0.01 ~ 99,999,999.99'
      }
    }

    // 分類驗證
    if (!formData.categoryId) {
      newErrors.categoryId = '請選擇商品分類'
    }

    // 圖片驗證 (新增模式建議上傳)
    if (!isEditMode && !imagePreview && !existingImageUrl) {
      newErrors.imageFile = '建議上傳商品圖片'
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

      const submitData = {
        name: formData.name,
        description: formData.description || null,
        price: Number(formData.price),
        imageUrl: existingImageUrl || imagePreview || null,
        categoryId: Number(formData.categoryId),
        publishedAt: formData.publishedAt || null,
        unpublishedAt: formData.unpublishedAt || null
      }

      if (isEditMode) {
        // 編輯模式
        await productService.update({
          productId: Number(id),
          ...submitData
        })
      } else {
        // 新增模式
        await productService.create(submitData)
      }

      // 成功後導向列表頁
      navigate('/products')
    } catch (err) {
      console.error('儲存商品失敗:', err)

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
    navigate('/products')
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
        <h1 className={styles.title}>{isEditMode ? '編輯商品' : '新增商品'}</h1>
      </div>

      <div className={styles.formCard}>
        <form onSubmit={handleSubmit}>
          {/* 全域錯誤訊息 */}
          {error && <div className={styles.errorAlert}>{error}</div>}

          {/* 商品名稱 */}
          <div className={styles.formGroup}>
            <label htmlFor="name" className={styles.label}>
              商品名稱 <span className={styles.required}>*</span>
            </label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              className={`${styles.input} ${errors.name ? styles.inputError : ''}`}
              placeholder="請輸入商品名稱（最多 200 字元）"
              maxLength="200"
              disabled={isLoading}
            />
            {errors.name && <span className={styles.errorText}>{errors.name}</span>}
          </div>

          {/* 商品描述 */}
          <div className={styles.formGroup}>
            <label htmlFor="description" className={styles.label}>
              商品描述 <span className={styles.optional}>（選填）</span>
            </label>
            <textarea
              id="description"
              name="description"
              value={formData.description}
              onChange={handleChange}
              className={styles.textarea}
              placeholder="請輸入商品描述"
              rows="5"
              disabled={isLoading}
            />
          </div>

          {/* 商品價格 */}
          <div className={styles.formGroup}>
            <label htmlFor="price" className={styles.label}>
              商品價格 <span className={styles.required}>*</span>
            </label>
            <input
              type="number"
              id="price"
              name="price"
              value={formData.price}
              onChange={handleChange}
              className={`${styles.input} ${errors.price ? styles.inputError : ''}`}
              placeholder="請輸入商品價格"
              step="0.01"
              min="0.01"
              max="99999999.99"
              disabled={isLoading}
            />
            {errors.price && <span className={styles.errorText}>{errors.price}</span>}
            <p className={styles.fieldHint}>價格範圍：0.01 ~ 99,999,999.99</p>
          </div>

          {/* 商品分類 */}
          <div className={styles.formGroup}>
            <label htmlFor="categoryId" className={styles.label}>
              商品分類 <span className={styles.required}>*</span>
            </label>
            <select
              id="categoryId"
              name="categoryId"
              value={formData.categoryId}
              onChange={handleChange}
              className={`${styles.select} ${errors.categoryId ? styles.inputError : ''}`}
              disabled={isLoading}
            >
              <option value="">請選擇分類</option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
            {errors.categoryId && <span className={styles.errorText}>{errors.categoryId}</span>}
          </div>

          {/* 商品圖片 */}
          <div className={styles.formGroup}>
            <label className={styles.label}>
              商品圖片 <span className={styles.optional}>（選填）</span>
            </label>

            {/* 現有圖片預覽 */}
            {(existingImageUrl || imagePreview) && (
              <div className={styles.imagePreview}>
                <img src={existingImageUrl || imagePreview} alt="商品圖片" />
                <button
                  type="button"
                  onClick={handleClearImage}
                  className={styles.clearImageButton}
                  disabled={isLoading || isUploadingImage}
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
                disabled={isLoading || isUploadingImage}
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
                <span>{isUploadingImage ? '上傳中...' : '點擊選擇圖片並裁切'}</span>
                <span className={styles.uploadHint}>支援 JPG、PNG、GIF，大小不超過 5MB</span>
              </label>
            </div>

            {errors.imageFile && <span className={styles.errorText}>{errors.imageFile}</span>}
          </div>

          {/* 上架時間 */}
          <div className={styles.formGroup}>
            <label htmlFor="publishedAt" className={styles.label}>
              上架時間 <span className={styles.optional}>（選填）</span>
            </label>
            <input
              type="datetime-local"
              id="publishedAt"
              name="publishedAt"
              value={formData.publishedAt}
              onChange={handleChange}
              className={styles.input}
              disabled={isLoading}
            />
            <p className={styles.fieldHint}>不設定則不限制上架時間</p>
          </div>

          {/* 下架時間 */}
          <div className={styles.formGroup}>
            <label htmlFor="unpublishedAt" className={styles.label}>
              下架時間 <span className={styles.optional}>（選填）</span>
            </label>
            <input
              type="datetime-local"
              id="unpublishedAt"
              name="unpublishedAt"
              value={formData.unpublishedAt}
              onChange={handleChange}
              className={styles.input}
              disabled={isLoading}
            />
            <p className={styles.fieldHint}>不設定則不限制下架時間</p>
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
            <button type="submit" className={styles.submitButton} disabled={isLoading}>
              {isLoading ? '儲存中...' : isEditMode ? '儲存變更' : '建立商品'}
            </button>
          </div>
        </form>
      </div>

      {/* 圖片編輯器 */}
      {showImageEditor && selectedImageForEdit && (
        <ImageEditor
          imageUrl={selectedImageForEdit}
          onSave={handleImageEdited}
          onCancel={handleImageEditorClose}
        />
      )}
    </div>
  )
}

export default ProductForm
