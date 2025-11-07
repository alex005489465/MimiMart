import { useState, useEffect } from 'react'
import bannerService from '../../services/bannerService'
import styles from './HeroBanner.module.css'

export default function HeroBanner() {
  const [currentSlide, setCurrentSlide] = useState(0)
  const [banners, setBanners] = useState([])
  const [isLoading, setIsLoading] = useState(true)

  // 靜態 fallback Banner (當 API 無資料時使用)
  const fallbackBanners = [
    {
      id: 'fallback-1',
      title: 'MimiMart 精選商品',
      imageUrl: 'https://images.unsplash.com/photo-1441986300917-64674bd600d8',
      linkUrl: null,
      displayOrder: 1
    }
  ]

  // 從 API 取得輪播圖資料
  useEffect(() => {
    const fetchBanners = async () => {
      setIsLoading(true)
      const data = await bannerService.getActiveBanners()

      if (data.length > 0) {
        setBanners(data)
      } else {
        // 無資料時使用 fallback
        console.info('使用靜態 fallback Banner')
        setBanners(fallbackBanners)
      }

      setIsLoading(false)
    }

    fetchBanners()
  }, [])

  // 取得實際要顯示的輪播資料
  const slides = banners.length > 0 ? banners : fallbackBanners

  // 自動輪播
  useEffect(() => {
    if (slides.length <= 1) return // 只有一張圖時不自動輪播

    const timer = setInterval(() => {
      setCurrentSlide((prev) => (prev + 1) % slides.length)
    }, 5000)

    return () => clearInterval(timer)
  }, [slides.length])

  const goToSlide = (index) => {
    setCurrentSlide(index)
  }

  const goToPrev = () => {
    setCurrentSlide((prev) => (prev - 1 + slides.length) % slides.length)
  }

  const goToNext = () => {
    setCurrentSlide((prev) => (prev + 1) % slides.length)
  }

  // 處理點擊跳轉
  const handleBannerClick = (linkUrl) => {
    if (linkUrl) {
      window.location.href = linkUrl
    }
  }

  // 載入中狀態
  if (isLoading) {
    return (
      <div className={styles.heroBanner}>
        <div className={styles.loadingContainer}>
          <div className={styles.loadingSpinner}></div>
        </div>
      </div>
    )
  }

  return (
    <div className={styles.heroBanner}>
      <div className={styles.slideContainer}>
        {slides.map((banner, index) => {
          const slideContent = (
            <div
              key={banner.id}
              className={`${styles.slide} ${
                index === currentSlide ? styles.active : ''
              }`}
              style={{
                backgroundImage: `url(${banner.imageUrl})`,
                backgroundSize: 'cover',
                backgroundPosition: 'center',
                backgroundRepeat: 'no-repeat'
              }}
            >
              {/* Hover 時顯示的標題提示 */}
              {banner.title && (
                <div className={styles.hoverOverlay}>
                  <span className={styles.hoverTitle}>{banner.title}</span>
                </div>
              )}
            </div>
          )

          // 如果有 linkUrl,包裹在可點擊元素中
          return banner.linkUrl ? (
            <div
              key={banner.id}
              onClick={() => handleBannerClick(banner.linkUrl)}
              style={{ cursor: 'pointer' }}
            >
              {slideContent}
            </div>
          ) : (
            slideContent
          )
        })}
      </div>

      {/* 左右箭頭 - 只在多張圖片時顯示 */}
      {slides.length > 1 && (
        <>
          <button className={styles.prevButton} onClick={goToPrev}>
            ‹
          </button>
          <button className={styles.nextButton} onClick={goToNext}>
            ›
          </button>
        </>
      )}

      {/* 指示器 - 只在多張圖片時顯示 */}
      {slides.length > 1 && (
        <div className={styles.indicators}>
          {slides.map((_, index) => (
            <button
              key={index}
              className={`${styles.indicator} ${
                index === currentSlide ? styles.activeIndicator : ''
              }`}
              onClick={() => goToSlide(index)}
              aria-label={`前往第 ${index + 1} 張輪播`}
            />
          ))}
        </div>
      )}
    </div>
  )
}
