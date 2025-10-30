import { useState, useEffect } from 'react'
import styles from './HeroBanner.module.css'

export default function HeroBanner() {
  const [currentSlide, setCurrentSlide] = useState(0)

  // 輪播資料（可以後續改為從 API 取得）
  const slides = [
    {
      id: 1,
      title: '夏季特賣會',
      subtitle: '全館商品最高 5 折優惠',
      bgColor: '#FFE5E5'
    },
    {
      id: 2,
      title: '新品上市',
      subtitle: '搶先體驗最新商品',
      bgColor: '#E5F3FF'
    },
    {
      id: 3,
      title: '會員專屬優惠',
      subtitle: '註冊即享首購折扣',
      bgColor: '#FFF5E5'
    }
  ]

  // 自動輪播
  useEffect(() => {
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

  return (
    <div className={styles.heroBanner}>
      <div className={styles.slideContainer}>
        {slides.map((slide, index) => (
          <div
            key={slide.id}
            className={`${styles.slide} ${
              index === currentSlide ? styles.active : ''
            }`}
            style={{ backgroundColor: slide.bgColor }}
          >
            <div className={styles.content}>
              <h1 className={styles.title}>{slide.title}</h1>
              <p className={styles.subtitle}>{slide.subtitle}</p>
              <button className={styles.ctaButton}>立即選購</button>
            </div>
          </div>
        ))}
      </div>

      {/* 左右箭頭 */}
      <button className={styles.prevButton} onClick={goToPrev}>
        ‹
      </button>
      <button className={styles.nextButton} onClick={goToNext}>
        ›
      </button>

      {/* 指示器 */}
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
    </div>
  )
}
