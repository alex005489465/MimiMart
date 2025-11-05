import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import bannerService from '../../services/bannerService'
import ConfirmModal from '../../components/ConfirmModal/ConfirmModal'
import { formatDateTime } from '../../utils/format'
import styles from './BannerList.module.css'

/**
 * Banner 列表頁面
 */
function BannerList() {
  const navigate = useNavigate()
  const [banners, setBanners] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')

  // 確認刪除對話框狀態
  const [deleteModal, setDeleteModal] = useState({
    isOpen: false,
    bannerId: null,
    bannerTitle: '',
    isLoading: false
  })

  // 載入 Banner 列表
  useEffect(() => {
    loadBanners()
  }, [])

  const loadBanners = async () => {
    try {
      setIsLoading(true)
      setError('')
      const data = await bannerService.getAll()
      setBanners(data)
    } catch (err) {
      console.error('載入 Banner 列表失敗:', err)
      setError('載入 Banner 列表失敗，請稍後再試')
    } finally {
      setIsLoading(false)
    }
  }

  // 切換 Banner 狀態 (啟用/停用)
  const handleToggleStatus = async (banner) => {
    try {
      const isActive = banner.status === 'ACTIVE'

      if (isActive) {
        await bannerService.deactivate(banner.id)
      } else {
        await bannerService.activate(banner.id)
      }

      // 重新載入列表
      await loadBanners()
    } catch (err) {
      console.error('切換狀態失敗:', err)
      alert(`切換狀態失敗: ${err.message || '未知錯誤'}`)
    }
  }

  // 開啟刪除確認對話框
  const openDeleteModal = (banner) => {
    setDeleteModal({
      isOpen: true,
      bannerId: banner.id,
      bannerTitle: banner.title,
      isLoading: false
    })
  }

  // 關閉刪除確認對話框
  const closeDeleteModal = () => {
    if (!deleteModal.isLoading) {
      setDeleteModal({
        isOpen: false,
        bannerId: null,
        bannerTitle: '',
        isLoading: false
      })
    }
  }

  // 確認刪除
  const handleConfirmDelete = async () => {
    try {
      setDeleteModal(prev => ({ ...prev, isLoading: true }))
      await bannerService.delete(deleteModal.bannerId)
      await loadBanners()
      closeDeleteModal()
    } catch (err) {
      console.error('刪除 Banner 失敗:', err)
      alert(`刪除失敗: ${err.message || '未知錯誤'}`)
      setDeleteModal(prev => ({ ...prev, isLoading: false }))
    }
  }

  // 前往編輯頁面
  const handleEdit = (bannerId) => {
    navigate(`/banners/edit/${bannerId}`)
  }

  // 前往新增頁面
  const handleCreate = () => {
    navigate('/banners/new')
  }

  if (isLoading) {
    return (
      <div className={styles.container}>
        <div className={styles.loading}>載入中...</div>
      </div>
    )
  }

  if (error) {
    return (
      <div className={styles.container}>
        <div className={styles.error}>
          <p>{error}</p>
          <button onClick={loadBanners} className={styles.retryButton}>
            重新載入
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className={styles.container}>
      {/* 頁面標題與操作按鈕 */}
      <div className={styles.header}>
        <h1 className={styles.title}>Banner 管理</h1>
        <button onClick={handleCreate} className={styles.createButton}>
          <svg
            width="20"
            height="20"
            viewBox="0 0 20 20"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M10 5V15M5 10H15"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
            />
          </svg>
          新增 Banner
        </button>
      </div>

      {/* Banner 列表 */}
      {banners.length === 0 ? (
        <div className={styles.empty}>
          <p>尚無 Banner 資料</p>
          <button onClick={handleCreate} className={styles.emptyButton}>
            建立第一個 Banner
          </button>
        </div>
      ) : (
        <div className={styles.tableWrapper}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>圖片</th>
                <th>標題</th>
                <th>跳轉連結</th>
                <th>順序</th>
                <th>狀態</th>
                <th>建立時間</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              {banners.map((banner) => (
                <tr key={banner.id}>
                  <td>
                    <div className={styles.imageCell}>
                      <img
                        src={banner.imageUrl}
                        alt={banner.title}
                        className={styles.thumbnail}
                      />
                    </div>
                  </td>
                  <td className={styles.titleCell}>{banner.title}</td>
                  <td>
                    {banner.linkUrl ? (
                      <a
                        href={banner.linkUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className={styles.link}
                      >
                        {banner.linkUrl}
                      </a>
                    ) : (
                      <span className={styles.noLink}>無</span>
                    )}
                  </td>
                  <td className={styles.orderCell}>{banner.displayOrder}</td>
                  <td>
                    <button
                      onClick={() => handleToggleStatus(banner)}
                      className={`${styles.statusBadge} ${
                        banner.status === 'ACTIVE' ? styles.active : styles.inactive
                      }`}
                    >
                      {banner.status === 'ACTIVE' ? '啟用' : '停用'}
                    </button>
                  </td>
                  <td className={styles.dateCell}>
                    {formatDateTime(banner.createdAt)}
                  </td>
                  <td>
                    <div className={styles.actions}>
                      <button
                        onClick={() => handleEdit(banner.id)}
                        className={styles.editButton}
                        aria-label="編輯"
                      >
                        <svg
                          width="16"
                          height="16"
                          viewBox="0 0 16 16"
                          fill="none"
                          xmlns="http://www.w3.org/2000/svg"
                        >
                          <path
                            d="M11.333 2.00004C11.5081 1.82494 11.716 1.68605 11.9447 1.59129C12.1735 1.49653 12.4187 1.44775 12.6663 1.44775C12.914 1.44775 13.1592 1.49653 13.3879 1.59129C13.6167 1.68605 13.8246 1.82494 13.9997 2.00004C14.1748 2.17513 14.3137 2.383 14.4084 2.61178C14.5032 2.84055 14.552 3.08575 14.552 3.33337C14.552 3.58099 14.5032 3.82619 14.4084 4.05497C14.3137 4.28374 14.1748 4.49161 13.9997 4.66671L5.33301 13.3334L1.33301 14.6667L2.66634 10.6667L11.333 2.00004Z"
                            stroke="currentColor"
                            strokeWidth="1.5"
                            strokeLinecap="round"
                            strokeLinejoin="round"
                          />
                        </svg>
                      </button>
                      <button
                        onClick={() => openDeleteModal(banner)}
                        className={styles.deleteButton}
                        aria-label="刪除"
                      >
                        <svg
                          width="16"
                          height="16"
                          viewBox="0 0 16 16"
                          fill="none"
                          xmlns="http://www.w3.org/2000/svg"
                        >
                          <path
                            d="M2 4H3.33333H14"
                            stroke="currentColor"
                            strokeWidth="1.5"
                            strokeLinecap="round"
                            strokeLinejoin="round"
                          />
                          <path
                            d="M5.33301 4.00004V2.66671C5.33301 2.31309 5.47348 1.97395 5.72353 1.7239C5.97358 1.47385 6.31272 1.33337 6.66634 1.33337H9.33301C9.68663 1.33337 10.0258 1.47385 10.2758 1.7239C10.5259 1.97395 10.6663 2.31309 10.6663 2.66671V4.00004M12.6663 4.00004V13.3334C12.6663 13.687 12.5259 14.0261 12.2758 14.2762C12.0258 14.5262 11.6866 14.6667 11.333 14.6667H4.66634C4.31272 14.6667 3.97358 14.5262 3.72353 14.2762C3.47348 14.0261 3.33301 13.687 3.33301 13.3334V4.00004H12.6663Z"
                            stroke="currentColor"
                            strokeWidth="1.5"
                            strokeLinecap="round"
                            strokeLinejoin="round"
                          />
                        </svg>
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* 刪除確認對話框 */}
      <ConfirmModal
        isOpen={deleteModal.isOpen}
        onClose={closeDeleteModal}
        onConfirm={handleConfirmDelete}
        title="確認刪除"
        message={`確定要刪除 Banner「${deleteModal.bannerTitle}」嗎？此操作無法復原，且會一併刪除 S3 上的圖片檔案。`}
        confirmText="刪除"
        cancelText="取消"
        isDanger={true}
        isLoading={deleteModal.isLoading}
      />
    </div>
  )
}

export default BannerList
