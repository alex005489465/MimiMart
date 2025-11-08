import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import productService from '../../services/productService'
import categoryService from '../../services/categoryService'
import ConfirmModal from '../../components/ConfirmModal/ConfirmModal'
import { formatDateTime, formatCurrency } from '../../utils/format'
import styles from './ProductList.module.css'

/**
 * 商品列表頁面
 */
function ProductList() {
  const navigate = useNavigate()
  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')

  // 分頁與篩選狀態
  const [currentPage, setCurrentPage] = useState(1)
  const [totalPages, setTotalPages] = useState(1)
  const [totalItems, setTotalItems] = useState(0)
  const [pageSize] = useState(20)
  const [statusFilter, setStatusFilter] = useState('all') // all, published, unpublished

  // 確認刪除對話框狀態
  const [deleteModal, setDeleteModal] = useState({
    isOpen: false,
    productId: null,
    productName: '',
    isLoading: false
  })

  // 載入分類列表
  useEffect(() => {
    loadCategories()
  }, [])

  // 載入商品列表（當頁碼或篩選條件改變時）
  useEffect(() => {
    loadProducts()
  }, [currentPage, statusFilter])

  const loadCategories = async () => {
    try {
      const data = await categoryService.getAll()
      setCategories(data)
    } catch (err) {
      console.error('載入分類列表失敗:', err)
    }
  }

  const loadProducts = async () => {
    try {
      setIsLoading(true)
      setError('')
      const response = await productService.getList(statusFilter, currentPage, pageSize)

      setProducts(response.data || [])

      // 設定分頁資訊
      if (response.meta) {
        setCurrentPage(response.meta.currentPage)
        setTotalPages(response.meta.totalPages)
        setTotalItems(response.meta.totalItems)
      }
    } catch (err) {
      console.error('載入商品列表失敗:', err)
      setError('載入商品列表失敗，請稍後再試')
    } finally {
      setIsLoading(false)
    }
  }

  // 取得分類名稱
  const getCategoryName = (categoryId) => {
    const category = categories.find((c) => c.id === categoryId)
    return category ? category.name : '未知分類'
  }

  // 切換商品上下架狀態
  const handleTogglePublish = async (product) => {
    try {
      if (product.isPublished) {
        await productService.unpublish(product.id)
      } else {
        await productService.publish(product.id)
      }

      // 重新載入列表
      await loadProducts()
    } catch (err) {
      console.error('切換上下架狀態失敗:', err)
      alert(`操作失敗: ${err.message || '未知錯誤'}`)
    }
  }

  // 開啟刪除確認對話框
  const openDeleteModal = (product) => {
    setDeleteModal({
      isOpen: true,
      productId: product.id,
      productName: product.name,
      isLoading: false
    })
  }

  // 關閉刪除確認對話框
  const closeDeleteModal = () => {
    if (!deleteModal.isLoading) {
      setDeleteModal({
        isOpen: false,
        productId: null,
        productName: '',
        isLoading: false
      })
    }
  }

  // 確認刪除
  const handleConfirmDelete = async () => {
    try {
      setDeleteModal((prev) => ({ ...prev, isLoading: true }))
      await productService.delete(deleteModal.productId)
      await loadProducts()
      closeDeleteModal()
    } catch (err) {
      console.error('刪除商品失敗:', err)
      alert(`刪除失敗: ${err.message || '未知錯誤'}`)
      setDeleteModal((prev) => ({ ...prev, isLoading: false }))
    }
  }

  // 前往編輯頁面
  const handleEdit = (productId) => {
    navigate(`/products/edit/${productId}`)
  }

  // 前往新增頁面
  const handleCreate = () => {
    navigate('/products/new')
  }

  // 切換狀態篩選
  const handleFilterChange = (status) => {
    setStatusFilter(status)
    setCurrentPage(1) // 重置為第一頁
  }

  // 換頁
  const handlePageChange = (newPage) => {
    if (newPage >= 1 && newPage <= totalPages) {
      setCurrentPage(newPage)
    }
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
          <button onClick={loadProducts} className={styles.retryButton}>
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
        <h1 className={styles.title}>商品管理</h1>
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
          新增商品
        </button>
      </div>

      {/* 狀態篩選 Tab */}
      <div className={styles.filters}>
        <button
          className={`${styles.filterTab} ${statusFilter === 'all' ? styles.active : ''}`}
          onClick={() => handleFilterChange('all')}
        >
          全部
        </button>
        <button
          className={`${styles.filterTab} ${statusFilter === 'published' ? styles.active : ''}`}
          onClick={() => handleFilterChange('published')}
        >
          已上架
        </button>
        <button
          className={`${styles.filterTab} ${statusFilter === 'unpublished' ? styles.active : ''}`}
          onClick={() => handleFilterChange('unpublished')}
        >
          未上架
        </button>
      </div>

      {/* 商品列表 */}
      {products.length === 0 ? (
        <div className={styles.empty}>
          <p>尚無商品資料</p>
          <button onClick={handleCreate} className={styles.emptyButton}>
            建立第一個商品
          </button>
        </div>
      ) : (
        <>
          <div className={styles.tableWrapper}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th className={styles.actionsHeader}>操作</th>
                  <th>圖片</th>
                  <th className={styles.nameHeader}>商品名稱</th>
                  <th>分類</th>
                  <th>價格</th>
                  <th>庫存</th>
                  <th>狀態</th>
                  <th>上架時間</th>
                  <th>下架時間</th>
                  <th>建立時間</th>
                </tr>
              </thead>
              <tbody>
                {products.map((product) => (
                  <tr key={product.id}>
                    <td data-label="操作">
                      <div className={styles.actions}>
                        <button
                          onClick={() => handleEdit(product.id)}
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
                          onClick={() => openDeleteModal(product)}
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
                    <td data-label="圖片">
                      <div className={styles.imageCell}>
                        {product.imageUrl ? (
                          <img
                            src={product.imageUrl}
                            alt={product.name}
                            className={styles.thumbnail}
                          />
                        ) : (
                          <div className={styles.noImage}>無圖片</div>
                        )}
                      </div>
                    </td>
                    <td data-label="商品名稱" className={styles.nameCell}>
                      {product.name}
                    </td>
                    <td data-label="分類" className={styles.categoryCell}>
                      {getCategoryName(product.categoryId)}
                    </td>
                    <td data-label="價格" className={styles.priceCell}>
                      {formatCurrency(product.price)}
                    </td>
                    <td data-label="庫存" className={styles.stockCell}>
                      {product.stock}
                    </td>
                    <td data-label="狀態">
                      <button
                        onClick={() => handleTogglePublish(product)}
                        className={`${styles.statusBadge} ${
                          product.isPublished ? styles.published : styles.unpublished
                        }`}
                      >
                        {product.isPublished ? '已上架' : '未上架'}
                      </button>
                    </td>
                    <td data-label="上架時間" className={styles.dateCell}>
                      {product.publishedAt ? (
                        formatDateTime(product.publishedAt)
                      ) : (
                        <span className={styles.noData}>不限制</span>
                      )}
                    </td>
                    <td data-label="下架時間" className={styles.dateCell}>
                      {product.unpublishedAt ? (
                        formatDateTime(product.unpublishedAt)
                      ) : (
                        <span className={styles.noData}>不限制</span>
                      )}
                    </td>
                    <td data-label="建立時間" className={styles.dateCell}>
                      {formatDateTime(product.createdAt)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* 分頁控制 */}
          {totalPages > 1 && (
            <div className={styles.pagination}>
              <button
                onClick={() => handlePageChange(currentPage - 1)}
                disabled={currentPage === 1}
                className={styles.pageButton}
              >
                上一頁
              </button>

              <span className={styles.pageInfo}>
                第 {currentPage} / {totalPages} 頁（共 {totalItems} 筆）
              </span>

              <button
                onClick={() => handlePageChange(currentPage + 1)}
                disabled={currentPage === totalPages}
                className={styles.pageButton}
              >
                下一頁
              </button>
            </div>
          )}
        </>
      )}

      {/* 刪除確認對話框 */}
      <ConfirmModal
        isOpen={deleteModal.isOpen}
        onClose={closeDeleteModal}
        onConfirm={handleConfirmDelete}
        title="確認刪除"
        message={`確定要刪除商品「${deleteModal.productName}」嗎？此操作無法復原。`}
        confirmText="刪除"
        cancelText="取消"
        isDanger={true}
        isLoading={deleteModal.isLoading}
      />
    </div>
  )
}

export default ProductList
