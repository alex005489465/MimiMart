import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import categoryService from '../../services/categoryService'
import { isRequired } from '../../utils/validation'
import styles from './CategoryForm.module.css'

/**
 * 分類表單頁面 (新增/編輯)
 */
function CategoryForm() {
  const navigate = useNavigate()
  const { id } = useParams() // 如果有 id 代表編輯模式
  const isEditMode = Boolean(id)

  // 表單資料
  const [formData, setFormData] = useState({
    name: '',
    description: ''
  })

  // UI 狀態
  const [isLoading, setIsLoading] = useState(false)
  const [isLoadingData, setIsLoadingData] = useState(false)
  const [error, setError] = useState('')
  const [errors, setErrors] = useState({})

  // 編輯模式：載入現有資料
  useEffect(() => {
    if (isEditMode) {
      loadCategoryData()
    }
  }, [id, isEditMode])

  const loadCategoryData = async () => {
    try {
      setIsLoadingData(true)
      const data = await categoryService.getDetail(id)
      setFormData({
        name: data.name,
        description: data.description || ''
      })
    } catch (err) {
      console.error('載入分類資料失敗:', err)
      setError('載入資料失敗，請稍後再試')
    } finally {
      setIsLoadingData(false)
    }
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

  // 表單驗證
  const validateForm = () => {
    const newErrors = {}

    // 分類名稱驗證
    if (!isRequired(formData.name)) {
      newErrors.name = '分類名稱為必填項'
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
        description: formData.description || null
      }

      if (isEditMode) {
        // 編輯模式
        await categoryService.update({
          categoryId: Number(id),
          ...submitData
        })
      } else {
        // 新增模式
        await categoryService.create(submitData)
      }

      // 成功後導向列表頁
      navigate('/categories')
    } catch (err) {
      console.error('儲存分類失敗:', err)

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
    navigate('/categories')
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
        <h1 className={styles.title}>{isEditMode ? '編輯分類' : '新增分類'}</h1>
      </div>

      <div className={styles.formCard}>
        <form onSubmit={handleSubmit}>
          {/* 全域錯誤訊息 */}
          {error && <div className={styles.errorAlert}>{error}</div>}

          {/* 分類名稱 */}
          <div className={styles.formGroup}>
            <label htmlFor="name" className={styles.label}>
              分類名稱 <span className={styles.required}>*</span>
            </label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              className={`${styles.input} ${errors.name ? styles.inputError : ''}`}
              placeholder="請輸入分類名稱"
              disabled={isLoading}
            />
            {errors.name && <span className={styles.errorText}>{errors.name}</span>}
          </div>

          {/* 分類描述 */}
          <div className={styles.formGroup}>
            <label htmlFor="description" className={styles.label}>
              分類描述 <span className={styles.optional}>（選填）</span>
            </label>
            <textarea
              id="description"
              name="description"
              value={formData.description}
              onChange={handleChange}
              className={styles.textarea}
              placeholder="請輸入分類描述"
              rows="5"
              disabled={isLoading}
            />
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
              {isLoading ? '儲存中...' : isEditMode ? '儲存變更' : '建立分類'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default CategoryForm
