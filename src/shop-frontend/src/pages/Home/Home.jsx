/**
 * 首頁元件
 * 使用 MUI v6 元件重構
 */
import { Typography, Card, CardContent, Button, Box, Stack } from '@mui/material';
import Grid from '@mui/material/Grid2';
import { MdShoppingCart, MdSecurity, MdSupportAgent, MdRocketLaunch } from 'react-icons/md';
import { useNavigate } from 'react-router-dom';
import HeroBanner from '../../components/HeroBanner/HeroBanner';
import styles from './Home.module.css';

export default function Home() {
  const navigate = useNavigate();

  // 服務特色
  const features = [
    {
      icon: <MdShoppingCart style={{ fontSize: '48px', color: '#1976d2' }} />,
      title: '商品多樣',
      description: '提供豐富的商品選擇，滿足您的各種需求',
    },
    {
      icon: <MdSecurity style={{ fontSize: '48px', color: '#2e7d32' }} />,
      title: '安全保障',
      description: '嚴格的品質把關，讓您購物更安心',
    },
    {
      icon: <MdSupportAgent style={{ fontSize: '48px', color: '#ed6c02' }} />,
      title: '優質客服',
      description: '專業的客服團隊，隨時為您服務',
    },
    {
      icon: <MdRocketLaunch style={{ fontSize: '48px', color: '#9c27b0' }} />,
      title: '快速配送',
      description: '高效的物流系統，確保商品準時送達',
    },
  ];

  return (
    <>
      {/* 輪播 Banner */}
      <HeroBanner />

      {/* 歡迎區塊 */}
      <Box component="section" className={styles.welcomeSection}>
        <Box className={styles.container}>
          <Typography variant="h2" sx={{ textAlign: 'center', mb: 2 }}>
            歡迎來到 MimiMart
          </Typography>
          <Typography
            variant="body1"
            sx={{
              textAlign: 'center',
              fontSize: '16px',
              maxWidth: '600px',
              margin: '0 auto 6',
              color: 'text.secondary',
            }}
          >
            您的線上購物好夥伴，提供多樣化的商品選擇與優質的購物體驗
          </Typography>

          {/* 服務特色 */}
          <Grid container spacing={3}>
            {features.map((feature, index) => (
              <Grid size={{ xs: 12, sm: 6, md: 3 }} key={index}>
                <Card
                  sx={{
                    textAlign: 'center',
                    height: '100%',
                    transition: 'transform 0.2s',
                    '&:hover': { transform: 'translateY(-4px)' },
                  }}
                >
                  <CardContent sx={{ p: 4 }}>
                    <Stack spacing={2} alignItems="center">
                      {feature.icon}
                      <Typography variant="h5">
                        {feature.title}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {feature.description}
                      </Typography>
                    </Stack>
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>

          {/* CTA 按鈕 */}
          <Box sx={{ textAlign: 'center', mt: 6 }}>
            <Stack direction="row" spacing={2} justifyContent="center">
              <Button
                variant="contained"
                size="large"
                onClick={() => navigate('/products')}
              >
                開始購物
              </Button>
              <Button
                variant="outlined"
                size="large"
                onClick={() => navigate('/promotions')}
              >
                查看優惠
              </Button>
            </Stack>
          </Box>
        </Box>
      </Box>

      {/* 未來可以在這裡加入更多區塊 */}
      {/* - 熱門商品推薦 */}
      {/* - 優惠活動 */}
      {/* - 新品上市 */}
    </>
  );
}
