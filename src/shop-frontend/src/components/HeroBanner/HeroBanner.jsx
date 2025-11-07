/**
 * Hero Banner 輪播元件
 * 使用 Ant Design Carousel 並整合 API
 */
import { useState, useEffect, useRef } from 'react';
import { Carousel, Spin, Image } from 'antd';
import { LeftOutlined, RightOutlined } from '@ant-design/icons';
import bannerService from '../../services/bannerService';
import styles from './HeroBanner.module.css';

export default function HeroBanner() {
  const [banners, setBanners] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const carouselRef = useRef(null);

  // 靜態 fallback Banner (當 API 無資料時使用)
  const fallbackBanners = [
    {
      id: 'fallback-1',
      title: 'MimiMart 精選商品',
      imageUrl: 'https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=1200&h=400&fit=crop',
      linkUrl: null,
      displayOrder: 1,
    },
    {
      id: 'fallback-2',
      title: '全館優惠中',
      imageUrl: 'https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=1200&h=400&fit=crop',
      linkUrl: null,
      displayOrder: 2,
    },
    {
      id: 'fallback-3',
      title: '新品上市',
      imageUrl: 'https://images.unsplash.com/photo-1472851294608-062f824d29cc?w=1200&h=400&fit=crop',
      linkUrl: null,
      displayOrder: 3,
    },
  ];

  // 從 API 取得輪播圖資料
  useEffect(() => {
    const fetchBanners = async () => {
      setIsLoading(true);
      const data = await bannerService.getActiveBanners();

      if (data && data.length > 0) {
        setBanners(data);
      } else {
        // 無資料時使用 fallback
        console.info('使用靜態 fallback Banner');
        setBanners(fallbackBanners);
      }

      setIsLoading(false);
    };

    fetchBanners();
  }, []);

  // 取得實際要顯示的輪播資料
  const slides = banners.length > 0 ? banners : fallbackBanners;

  // 處理點擊跳轉
  const handleBannerClick = (linkUrl) => {
    if (linkUrl) {
      window.location.href = linkUrl;
    }
  };

  // 載入中狀態
  if (isLoading) {
    return (
      <div className={styles.heroBanner}>
        <div className={styles.loadingContainer}>
          <Spin size="large" tip="載入輪播圖..." />
        </div>
      </div>
    );
  }

  return (
    <div className={styles.heroBanner}>
      {slides.length > 1 && (
        <>
          <button
            className={styles.prevButton}
            onClick={() => carouselRef.current?.prev()}
            aria-label="上一張"
          >
            <LeftOutlined />
          </button>
          <button
            className={styles.nextButton}
            onClick={() => carouselRef.current?.next()}
            aria-label="下一張"
          >
            <RightOutlined />
          </button>
        </>
      )}

      <Carousel
        ref={carouselRef}
        autoplay={slides.length > 1}
        autoplaySpeed={5000}
        effect="fade"
        dots={{ className: styles.carouselDots }}
      >
        {slides.map((banner) => (
          <div key={banner.id}>
            <div
              className={styles.slide}
              onClick={() => handleBannerClick(banner.linkUrl)}
              style={{
                cursor: banner.linkUrl ? 'pointer' : 'default',
              }}
            >
              <Image
                src={banner.imageUrl}
                alt={banner.title || 'Banner'}
                preview={false}
                className={styles.bannerImage}
                placeholder={
                  <div className={styles.imagePlaceholder}>
                    <Spin />
                  </div>
                }
              />
              {banner.title && (
                <div className={styles.overlay}>
                  <h2 className={styles.title}>{banner.title}</h2>
                </div>
              )}
            </div>
          </div>
        ))}
      </Carousel>
    </div>
  );
}
