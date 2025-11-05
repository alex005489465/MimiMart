import { useEffect } from 'react'
import styles from './ConfirmModal.module.css'

/**
 * 通用確認對話框元件
 * @param {Object} props
 * @param {boolean} props.isOpen - 是否顯示對話框
 * @param {Function} props.onClose - 關閉對話框的回調
 * @param {Function} props.onConfirm - 確認操作的回調
 * @param {string} props.title - 對話框標題
 * @param {string} props.message - 對話框訊息
 * @param {string} [props.confirmText='確認'] - 確認按鈕文字
 * @param {string} [props.cancelText='取消'] - 取消按鈕文字
 * @param {boolean} [props.isDanger=false] - 是否為危險操作 (紅色按鈕)
 * @param {boolean} [props.isLoading=false] - 是否處於載入中狀態
 */
function ConfirmModal({
  isOpen,
  onClose,
  onConfirm,
  title,
  message,
  confirmText = '確認',
  cancelText = '取消',
  isDanger = false,
  isLoading = false
}) {
  // ESC 鍵關閉對話框
  useEffect(() => {
    const handleEscape = (e) => {
      if (e.key === 'Escape' && isOpen && !isLoading) {
        onClose()
      }
    }

    if (isOpen) {
      document.addEventListener('keydown', handleEscape)
      // 防止背景滾動
      document.body.style.overflow = 'hidden'
    }

    return () => {
      document.removeEventListener('keydown', handleEscape)
      document.body.style.overflow = 'unset'
    }
  }, [isOpen, isLoading, onClose])

  if (!isOpen) return null

  const handleBackdropClick = (e) => {
    if (e.target === e.currentTarget && !isLoading) {
      onClose()
    }
  }

  return (
    <div className={styles.backdrop} onClick={handleBackdropClick}>
      <div className={styles.modal}>
        <div className={styles.header}>
          <h2 className={styles.title}>{title}</h2>
          {!isLoading && (
            <button
              className={styles.closeButton}
              onClick={onClose}
              aria-label="關閉對話框"
            >
              <svg
                width="20"
                height="20"
                viewBox="0 0 20 20"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  d="M15 5L5 15M5 5L15 15"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                />
              </svg>
            </button>
          )}
        </div>

        <div className={styles.body}>
          <p className={styles.message}>{message}</p>
        </div>

        <div className={styles.footer}>
          <button
            className={styles.cancelButton}
            onClick={onClose}
            disabled={isLoading}
          >
            {cancelText}
          </button>
          <button
            className={`${styles.confirmButton} ${isDanger ? styles.danger : ''}`}
            onClick={onConfirm}
            disabled={isLoading}
          >
            {isLoading ? '處理中...' : confirmText}
          </button>
        </div>
      </div>
    </div>
  )
}

export default ConfirmModal
