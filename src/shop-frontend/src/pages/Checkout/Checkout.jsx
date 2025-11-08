/**
 * 結帳頁面
 * 目前僅提供 UI 規劃,不實作實際的訂單建立功能
 */
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Grid2,
  Typography,
  Button,
  Box,
  Card,
  CardContent,
  TextField,
  Divider,
  Alert,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Breadcrumbs,
  Link,
} from '@mui/material';
import {
  MdShoppingCart,
  MdHome,
  MdChevronRight,
  MdPayment,
} from 'react-icons/md';
import useCartStore from '../../stores/cartStore';
import useAuthStore from '../../stores/authStore';
import styles from './Checkout.module.css';

const Checkout = () => {
  const navigate = useNavigate();
  const { items, getTotalPrice, getTotalItems } = useCartStore();
  const { isAuthenticated, user } = useAuthStore();

  // 收件人資訊狀態
  const [shippingInfo, setShippingInfo] = useState({
    receiverName: user?.name || '',
    receiverPhone: user?.phone || '',
    receiverEmail: user?.email || '',
    shippingAddress: '',
    note: '',
  });

  // 處理輸入變更
  const handleInputChange = (field) => (e) => {
    setShippingInfo({
      ...shippingInfo,
      [field]: e.target.value,
    });
  };

  // 麵包屑導航
  const breadcrumbs = [
    <Link
      key="home"
      underline="hover"
      color="inherit"
      href="/"
      onClick={(e) => {
        e.preventDefault();
        navigate('/');
      }}
      sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}
    >
      <MdHome /> 首頁
    </Link>,
    <Link
      key="cart"
      underline="hover"
      color="inherit"
      href="/cart"
      onClick={(e) => {
        e.preventDefault();
        navigate('/cart');
      }}
      sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}
    >
      <MdShoppingCart /> 購物車
    </Link>,
    <Typography key="current" color="text.primary">
      結帳
    </Typography>,
  ];

  // 購物車為空時導向購物車頁面
  if (items.length === 0) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Alert severity="warning" sx={{ mb: 3 }}>
          購物車是空的,請先加入商品
        </Alert>
        <Button variant="contained" onClick={() => navigate('/products')}>
          前往商品列表
        </Button>
      </Container>
    );
  }

  // 提交訂單 (目前僅顯示開發中提示)
  const handleSubmitOrder = () => {
    alert('訂單功能開發中,敬請期待!');
  };

  return (
    <Container maxWidth="lg" className={styles.checkoutPage}>
      {/* 麵包屑 */}
      <Breadcrumbs separator={<MdChevronRight />} sx={{ mb: 3 }}>
        {breadcrumbs}
      </Breadcrumbs>

      <Typography variant="h4" sx={{ mb: 3 }}>
        <MdPayment style={{ verticalAlign: 'middle', marginRight: 8 }} />
        結帳
      </Typography>

      <Grid2 container spacing={3}>
        {/* 左側:收件人資訊 */}
        <Grid2 size={{ xs: 12, md: 7 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                收件人資訊
              </Typography>
              <Divider sx={{ mb: 3 }} />

              {!isAuthenticated && (
                <Alert severity="info" sx={{ mb: 3 }}>
                  尚未登入,請
                  <Link
                    href="/login"
                    onClick={(e) => {
                      e.preventDefault();
                      navigate('/login');
                    }}
                    sx={{ mx: 0.5 }}
                  >
                    登入
                  </Link>
                  以繼續結帳
                </Alert>
              )}

              <Grid2 container spacing={2}>
                <Grid2 size={{ xs: 12, sm: 6 }}>
                  <TextField
                    fullWidth
                    label="收件人姓名"
                    value={shippingInfo.receiverName}
                    onChange={handleInputChange('receiverName')}
                    required
                    disabled={!isAuthenticated}
                  />
                </Grid2>
                <Grid2 size={{ xs: 12, sm: 6 }}>
                  <TextField
                    fullWidth
                    label="聯絡電話"
                    value={shippingInfo.receiverPhone}
                    onChange={handleInputChange('receiverPhone')}
                    required
                    disabled={!isAuthenticated}
                  />
                </Grid2>
                <Grid2 size={12}>
                  <TextField
                    fullWidth
                    label="Email"
                    type="email"
                    value={shippingInfo.receiverEmail}
                    onChange={handleInputChange('receiverEmail')}
                    required
                    disabled={!isAuthenticated}
                  />
                </Grid2>
                <Grid2 size={12}>
                  <TextField
                    fullWidth
                    label="收件地址"
                    value={shippingInfo.shippingAddress}
                    onChange={handleInputChange('shippingAddress')}
                    required
                    disabled={!isAuthenticated}
                    multiline
                    rows={2}
                  />
                </Grid2>
                <Grid2 size={12}>
                  <TextField
                    fullWidth
                    label="備註"
                    value={shippingInfo.note}
                    onChange={handleInputChange('note')}
                    disabled={!isAuthenticated}
                    multiline
                    rows={3}
                    placeholder="如有特殊需求請於此處說明"
                  />
                </Grid2>
              </Grid2>
            </CardContent>
          </Card>
        </Grid2>

        {/* 右側:訂單摘要 */}
        <Grid2 size={{ xs: 12, md: 5 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                訂單摘要
              </Typography>
              <Divider sx={{ mb: 2 }} />

              {/* 商品列表 */}
              <TableContainer component={Paper} variant="outlined">
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>商品</TableCell>
                      <TableCell align="right">數量</TableCell>
                      <TableCell align="right">小計</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {items.map((item) => (
                      <TableRow key={item.productId}>
                        <TableCell>{item.name}</TableCell>
                        <TableCell align="right">{item.quantity}</TableCell>
                        <TableCell align="right">
                          NT$ {(item.price * item.quantity).toLocaleString()}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>

              <Divider sx={{ my: 2 }} />

              {/* 總計資訊 */}
              <Box sx={{ mb: 2 }}>
                <Box
                  sx={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    mb: 1,
                  }}
                >
                  <Typography variant="body1">
                    商品總數:{getTotalItems()} 件
                  </Typography>
                </Box>
                <Box
                  sx={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    mb: 1,
                  }}
                >
                  <Typography variant="body1">商品種類:</Typography>
                  <Typography variant="body1">{items.length} 種</Typography>
                </Box>
                <Box
                  sx={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    mb: 1,
                  }}
                >
                  <Typography variant="body1">運費:</Typography>
                  <Typography variant="body1" color="success.main">
                    免運費
                  </Typography>
                </Box>
              </Box>

              <Divider sx={{ mb: 2 }} />

              {/* 總金額 */}
              <Box
                sx={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                }}
              >
                <Typography variant="h6">總計:</Typography>
                <Typography variant="h5" color="error" fontWeight="bold">
                  NT$ {getTotalPrice().toLocaleString()}
                </Typography>
              </Box>

              {/* 提交按鈕 */}
              <Button
                variant="contained"
                color="primary"
                size="large"
                fullWidth
                onClick={handleSubmitOrder}
                disabled={!isAuthenticated}
                sx={{ mt: 3 }}
              >
                {isAuthenticated ? '確認送出訂單' : '請先登入'}
              </Button>

              {isAuthenticated && (
                <Alert severity="info" sx={{ mt: 2 }}>
                  訂單功能開發中,目前僅提供 UI 預覽
                </Alert>
              )}

              <Button
                variant="outlined"
                fullWidth
                onClick={() => navigate('/cart')}
                sx={{ mt: 2 }}
              >
                返回購物車
              </Button>
            </CardContent>
          </Card>
        </Grid2>
      </Grid2>
    </Container>
  );
};

export default Checkout;
