/**
 * Footer 元件
 * 四欄式頁尾：關於我們、客服中心、購物指南、聯絡方式
 */
import { Box, Grid2, Stack, Typography, Link as MuiLink, IconButton } from '@mui/material';
import {
  MdPhone,
  MdEmail,
  MdLocationOn,
} from 'react-icons/md';
import { FaFacebook, FaInstagram, FaTwitter, FaYoutube } from 'react-icons/fa';
import { Link } from 'react-router-dom';
import styles from './Footer.module.css';

const Footer = () => {
  const currentYear = new Date().getFullYear();

  return (
    <Box component="footer" className={styles.footer}>
      <Box className={styles.container}>
        <Grid2 container spacing={4}>
          {/* 關於我們 */}
          <Grid2 size={{ xs: 12, sm: 6, md: 3 }}>
            <Box className={styles.section}>
              <Typography variant="h6" className={styles.title} gutterBottom>
                關於我們
              </Typography>
              <Stack spacing={1}>
                <Link to="/about" className={styles.link}>
                  公司簡介
                </Link>
                <Link to="/team" className={styles.link}>
                  團隊介紹
                </Link>
                <Link to="/careers" className={styles.link}>
                  加入我們
                </Link>
                <Link to="/news" className={styles.link}>
                  最新消息
                </Link>
              </Stack>
            </Box>
          </Grid2>

          {/* 客服中心 */}
          <Grid2 size={{ xs: 12, sm: 6, md: 3 }}>
            <Box className={styles.section}>
              <Typography variant="h6" className={styles.title} gutterBottom>
                客服中心
              </Typography>
              <Stack spacing={1}>
                <Link to="/help" className={styles.link}>
                  常見問題
                </Link>
                <Link to="/shipping" className={styles.link}>
                  配送說明
                </Link>
                <Link to="/returns" className={styles.link}>
                  退換貨政策
                </Link>
                <Link to="/payment" className={styles.link}>
                  付款方式
                </Link>
              </Stack>
            </Box>
          </Grid2>

          {/* 購物指南 */}
          <Grid2 size={{ xs: 12, sm: 6, md: 3 }}>
            <Box className={styles.section}>
              <Typography variant="h6" className={styles.title} gutterBottom>
                購物指南
              </Typography>
              <Stack spacing={1}>
                <Link to="/how-to-order" className={styles.link}>
                  如何購物
                </Link>
                <Link to="/member-benefits" className={styles.link}>
                  會員權益
                </Link>
                <Link to="/privacy" className={styles.link}>
                  隱私權政策
                </Link>
                <Link to="/terms" className={styles.link}>
                  服務條款
                </Link>
              </Stack>
            </Box>
          </Grid2>

          {/* 聯絡方式 */}
          <Grid2 size={{ xs: 12, sm: 6, md: 3 }}>
            <Box className={styles.section}>
              <Typography variant="h6" className={styles.title} gutterBottom>
                聯絡我們
              </Typography>
              <Stack spacing={2}>
                <Box className={styles.contact}>
                  <MdPhone className={styles.icon} />
                  <Typography variant="body2">(02) 1234-5678</Typography>
                </Box>
                <Box className={styles.contact}>
                  <MdEmail className={styles.icon} />
                  <MuiLink href="mailto:support@mimimart.com" color="inherit">
                    support@mimimart.com
                  </MuiLink>
                </Box>
                <Box className={styles.contact}>
                  <MdLocationOn className={styles.icon} />
                  <Typography variant="body2">台北市信義區信義路五段7號</Typography>
                </Box>

                {/* 社群媒體 */}
                <Stack direction="row" spacing={1} className={styles.social}>
                  <IconButton
                    component="a"
                    href="https://facebook.com"
                    target="_blank"
                    aria-label="Facebook"
                    size="small"
                  >
                    <FaFacebook className={styles.socialIcon} />
                  </IconButton>
                  <IconButton
                    component="a"
                    href="https://instagram.com"
                    target="_blank"
                    aria-label="Instagram"
                    size="small"
                  >
                    <FaInstagram className={styles.socialIcon} />
                  </IconButton>
                  <IconButton
                    component="a"
                    href="https://twitter.com"
                    target="_blank"
                    aria-label="Twitter"
                    size="small"
                  >
                    <FaTwitter className={styles.socialIcon} />
                  </IconButton>
                  <IconButton
                    component="a"
                    href="https://youtube.com"
                    target="_blank"
                    aria-label="YouTube"
                    size="small"
                  >
                    <FaYoutube className={styles.socialIcon} />
                  </IconButton>
                </Stack>
              </Stack>
            </Box>
          </Grid2>
        </Grid2>

        {/* 版權聲明 */}
        <Box className={styles.copyright}>
          <Typography variant="body2" color="text.secondary">
            © {currentYear} MimiMart. All rights reserved.
          </Typography>
        </Box>
      </Box>
    </Box>
  );
};

export default Footer;
