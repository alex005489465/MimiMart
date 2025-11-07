/**
 * Hero Banner 輪播元件
 * 使用 react-slick 並整合 API
 */
import { useState, useEffect, useRef } from 'react';
import Slider from 'react-slick';
import { CircularProgress, Box } from '@mui/material';
import { MdChevronLeft, MdChevronRight } from 'react-icons/md';
import bannerService from '../../services/bannerService';
import styles from './HeroBanner.module.css';
import 'slick-carousel/slick/slick.css';
import 'slick-carousel/slick/slick-theme.css';

// 自定義前一張按鈕元件（過濾 react-slick 內部 props）
const PrevButton = ({ onClick }) => (
  <button
    className={styles.prevButton}
    onClick={onClick}
    aria-label="上一張"
  >
    <MdChevronLeft size={32} />
  </button>
);

// 自定義下一張按鈕元件（過濾 react-slick 內部 props）
const NextButton = ({ onClick }) => (
  <button
    className={styles.nextButton}
    onClick={onClick}
    aria-label="下一張"
  >
    <MdChevronRight size={32} />
  </button>
);

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
      <Box className={styles.heroBanner}>
        <Box className={styles.loadingContainer}>
          <CircularProgress size={60} />
        </Box>
      </Box>
    );
  }

  // react-slick 設定
  const settings = {
    dots: true,
    infinite: slides.length > 1,
    speed: 500,
    slidesToShow: 1,
    slidesToScroll: 1,
    autoplay: slides.length > 1,
    autoplaySpeed: 5000,
    fade: true,
    arrows: slides.length > 1,
    prevArrow: <PrevButton />,
    nextArrow: <NextButton />,
    dotsClass: `slick-dots ${styles.carouselDots}`,
  };

  return (
    <Box className={styles.heroBanner}>
      <Slider {...settings} ref={carouselRef}>
        {slides.map((banner) => (
          <div key={banner.id}>
            <Box
              className={styles.slide}
              onClick={() => handleBannerClick(banner.linkUrl)}
              sx={{
                cursor: banner.linkUrl ? 'pointer' : 'default',
              }}
            >
              <img
                src={banner.imageUrl}
                alt={banner.title || 'Banner'}
                className={styles.bannerImage}
                loading="lazy"
              />
              {banner.title && (
                <Box className={styles.overlay}>
                  <h2 className={styles.title}>{banner.title}</h2>
                </Box>
              )}
            </Box>
          </div>
        ))}
      </Slider>
    </Box>
  );
}
