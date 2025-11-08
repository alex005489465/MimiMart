import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Container,
  Grid2,
  Typography,
  Button,
  Box,
  Card,
  CardMedia,
  CircularProgress,
  Alert,
  Breadcrumbs,
  Link,
  TextField,
  Divider,
  Chip,
  Snackbar
} from '@mui/material';
import {
  MdShoppingCart,
  MdHome,
  MdChevronRight,
  MdInventory,
  MdLocalOffer
} from 'react-icons/md';
import productService from '../../services/productService';
import useCartStore from '../../stores/cartStore';
import styles from './ProductDetail.module.css';

const ProductDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const addItem = useCartStore((state) => state.addItem);
  const isInCart = useCartStore((state) => state.isInCart);
  const getItemQuantity = useCartStore((state) => state.getItemQuantity);

  // 狀態管理
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success'
  });

  // 載入商品詳情
  useEffect(() => {
    const loadProductDetail = async () => {
      try {
        setLoading(true);
        setError(null);

        const response = await productService.getProductDetail(id);

        if (response.success) {
          setProduct(response.data);
        } else {
          setError(response.message || '載入商品失敗');
        }
      } catch (err) {
        console.error('載入商品詳情失敗:', err);
        setError('載入商品時發生錯誤，請稍後再試');
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      loadProductDetail();
    }
  }, [id]);

  // 數量變更
  const handleQuantityChange = (e) => {
    const value = parseInt(e.target.value) || 1;
    if (value >= 1 && value <= product.stock) {
      setQuantity(value);
    }
  };

  // 增加數量
  const handleIncreaseQuantity = () => {
    if (quantity < product.stock) {
      setQuantity(quantity + 1);
    }
  };

  // 減少數量
  const handleDecreaseQuantity = () => {
    if (quantity > 1) {
      setQuantity(quantity - 1);
    }
  };

  // 顯示 Snackbar
  const showSnackbar = (message, severity = 'success') => {
    setSnackbar({ open: true, message, severity });
  };

  // 關閉 Snackbar
  const handleCloseSnackbar = () => {
    setSnackbar({ ...snackbar, open: false });
  };

  // 加入購物車
  const handleAddToCart = async () => {
    try {
      const result = await addItem({
        productId: product.id,
        name: product.name,
        price: product.price,
        quantity: quantity,
        image: product.imageUrl || 'https://via.placeholder.com/300x300?text=No+Image',
        stock: product.stock
      });

      if (result.success) {
        showSnackbar(result.message || `已將 ${quantity} 件「${product.name}」加入購物車`);
      } else {
        showSnackbar(result.error || '加入購物車失敗', 'error');
      }
    } catch (err) {
      showSnackbar(err.message || '加入購物車失敗', 'error');
    }
  };

  // 立即購買
  const handleBuyNow = async () => {
    await handleAddToCart();
    navigate('/cart');
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
      key="products"
      underline="hover"
      color="inherit"
      href="/products"
      onClick={(e) => {
        e.preventDefault();
        navigate('/products');
      }}
    >
      商品列表
    </Link>,
    <Typography key="current" color="text.primary">
      {product?.name || '商品詳情'}
    </Typography>
  ];

  // 載入中
  if (loading) {
    return (
      <Container maxWidth="lg" className={styles.container}>
        <Box className={styles.loadingBox}>
          <CircularProgress size={60} />
          <Typography variant="h6" sx={{ mt: 2 }}>
            載入中...
          </Typography>
        </Box>
      </Container>
    );
  }

  // 錯誤狀態
  if (error || !product) {
    return (
      <Container maxWidth="lg" className={styles.container}>
        <Alert severity="error" sx={{ mb: 3 }}>
          {error || '找不到此商品'}
        </Alert>
        <Button variant="contained" onClick={() => navigate('/products')}>
          返回商品列表
        </Button>
      </Container>
    );
  }

  // 計算購物車中的數量
  const cartQuantity = getItemQuantity(product.id);
  const remainingStock = product.stock - cartQuantity;

  return (
    <Container maxWidth="lg" className={styles.container}>
      {/* 麵包屑 */}
      <Breadcrumbs
        separator={<MdChevronRight />}
        className={styles.breadcrumbs}
      >
        {breadcrumbs}
      </Breadcrumbs>

      {/* 商品詳情 */}
      <Grid2 container spacing={4}>
        {/* 左側：商品圖片 */}
        <Grid2 size={{ xs: 12, md: 5 }}>
          <Card className={styles.imageCard}>
            <CardMedia
              component="img"
              image={product.imageUrl || 'https://via.placeholder.com/600x600?text=No+Image'}
              alt={product.name}
              className={styles.productImage}
            />
          </Card>
        </Grid2>

        {/* 右側：商品資訊 */}
        <Grid2 size={{ xs: 12, md: 7 }}>
          <Box className={styles.productInfo}>
            {/* 商品名稱 */}
            <Typography variant="h4" component="h1" gutterBottom>
              {product.name}
            </Typography>

            {/* 上架狀態 */}
            {product.isPublished && (
              <Chip
                icon={<MdLocalOffer />}
                label="上架中"
                color="success"
                size="small"
                sx={{ mb: 2 }}
              />
            )}

            <Divider sx={{ my: 2 }} />

            {/* 價格 */}
            <Box className={styles.priceSection}>
              <Typography variant="h3" color="primary" fontWeight="bold">
                NT$ {product.price.toLocaleString()}
              </Typography>
            </Box>

            <Divider sx={{ my: 2 }} />

            {/* 庫存資訊 */}
            <Box className={styles.stockSection}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <MdInventory size={24} />
                <Typography variant="h6">庫存狀態</Typography>
              </Box>
              <Typography
                variant="body1"
                color={product.stock > 0 ? 'success.main' : 'error.main'}
                fontWeight="bold"
                sx={{ mt: 1 }}
              >
                {product.stock > 0 ? `庫存: ${product.stock} 件` : '目前缺貨'}
              </Typography>
              {cartQuantity > 0 && (
                <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                  購物車中已有 {cartQuantity} 件，剩餘可購買 {remainingStock} 件
                </Typography>
              )}
            </Box>

            <Divider sx={{ my: 3 }} />

            {/* 數量選擇 */}
            {product.stock > 0 && (
              <Box className={styles.quantitySection}>
                <Typography variant="h6" gutterBottom>
                  選擇數量
                </Typography>
                <Box className={styles.quantityControl}>
                  <Button
                    variant="outlined"
                    onClick={handleDecreaseQuantity}
                    disabled={quantity <= 1}
                  >
                    -
                  </Button>
                  <TextField
                    type="number"
                    value={quantity}
                    onChange={handleQuantityChange}
                    inputProps={{
                      min: 1,
                      max: remainingStock,
                      style: { textAlign: 'center' }
                    }}
                    sx={{ width: '80px' }}
                  />
                  <Button
                    variant="outlined"
                    onClick={handleIncreaseQuantity}
                    disabled={quantity >= remainingStock}
                  >
                    +
                  </Button>
                </Box>
              </Box>
            )}

            {/* 操作按鈕 */}
            <Box className={styles.actionButtons}>
              <Button
                variant="outlined"
                size="large"
                startIcon={<MdShoppingCart />}
                onClick={handleAddToCart}
                disabled={product.stock <= 0 || remainingStock <= 0}
                fullWidth
              >
                {remainingStock <= 0 ? '已達購買上限' : '加入購物車'}
              </Button>
              <Button
                variant="contained"
                size="large"
                onClick={handleBuyNow}
                disabled={product.stock <= 0 || remainingStock <= 0}
                fullWidth
              >
                立即購買
              </Button>
            </Box>
          </Box>
        </Grid2>
      </Grid2>

      {/* 商品描述 */}
      {product.description && (
        <Box className={styles.descriptionSection}>
          <Typography variant="h5" gutterBottom>
            商品描述
          </Typography>
          <Divider sx={{ mb: 2 }} />
          <Typography
            variant="body1"
            color="text.secondary"
            sx={{ whiteSpace: 'pre-line', lineHeight: 1.8 }}
          >
            {product.description}
          </Typography>
        </Box>
      )}

      {/* Snackbar 提示訊息 */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={handleCloseSnackbar}
        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
      >
        <Alert
          onClose={handleCloseSnackbar}
          severity={snackbar.severity}
          sx={{ width: '100%' }}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Container>
  );
};

export default ProductDetail;
