import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  Container,
  Grid2,
  Card,
  CardMedia,
  CardContent,
  CardActions,
  Typography,
  Button,
  Pagination,
  Box,
  CircularProgress,
  Alert,
  Chip,
  IconButton,
  TextField,
  Badge,
  Snackbar,
} from '@mui/material';
import {
  MdShoppingCart,
  MdSearch,
  MdAdd,
  MdRemove,
} from 'react-icons/md';
import productService from '../../services/productService';
import useCartStore from '../../stores/cartStore';
import styles from './Products.module.css';

const Products = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  // cartStore 整合
  const addItem = useCartStore((state) => state.addItem);
  const isInCart = useCartStore((state) => state.isInCart);
  const getItemQuantity = useCartStore((state) => state.getItemQuantity);
  const cartLoading = useCartStore((state) => state.loading);

  // 從 URL 取得搜尋參數
  const searchKeyword = searchParams.get('search');
  const categoryId = searchParams.get('category');

  // 狀態管理
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [totalItems, setTotalItems] = useState(0);
  const pageSize = 12;

  // 數量狀態管理 (每個商品的選擇數量)
  const [quantities, setQuantities] = useState({});

  // Snackbar 狀態管理
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success',
  });

  // 載入商品資料
  const loadProducts = async (page) => {
    try {
      setLoading(true);
      setError(null);

      let response;

      // 根據搜尋參數決定呼叫哪個 API
      if (searchKeyword) {
        response = await productService.searchProducts(searchKeyword, page, pageSize);
      } else {
        response = await productService.getProductList({
          categoryId: categoryId || undefined,
          page,
          size: pageSize,
          sortBy: 'createdAt',
          sortDir: 'DESC'
        });
      }

      if (response.success) {
        const productList = response.data || [];
        setProducts(productList);

        // 初始化每個商品的數量為 1
        const initialQuantities = {};
        productList.forEach((product) => {
          initialQuantities[product.id] = 1;
        });
        setQuantities(initialQuantities);

        if (response.meta) {
          setCurrentPage(response.meta.currentPage);
          setTotalPages(response.meta.totalPages);
          setTotalItems(response.meta.totalItems);
        }
      } else {
        setError(response.message || '載入商品失敗');
      }
    } catch (err) {
      console.error('載入商品失敗:', err);
      setError('載入商品時發生錯誤，請稍後再試');
    } finally {
      setLoading(false);
    }
  };

  // 初始載入與參數變更時重新載入
  useEffect(() => {
    setCurrentPage(1);
    loadProducts(1);
  }, [searchKeyword, categoryId]);

  // 分頁變更
  const handlePageChange = (event, page) => {
    setCurrentPage(page);
    loadProducts(page);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  // 顯示 Snackbar
  const showSnackbar = (message, severity = 'success') => {
    setSnackbar({ open: true, message, severity });
  };

  // 關閉 Snackbar
  const handleCloseSnackbar = () => {
    setSnackbar({ ...snackbar, open: false });
  };

  // 增加數量
  const handleIncreaseQuantity = (product) => {
    const currentQuantity = quantities[product.id] || 1;
    const cartQuantity = getItemQuantity(product.id);
    const remainingStock = product.stock - cartQuantity;

    if (currentQuantity >= remainingStock) {
      showSnackbar('已達庫存上限', 'warning');
      return;
    }

    setQuantities({
      ...quantities,
      [product.id]: currentQuantity + 1,
    });
  };

  // 減少數量
  const handleDecreaseQuantity = (productId) => {
    const currentQuantity = quantities[productId] || 1;
    if (currentQuantity <= 1) return;

    setQuantities({
      ...quantities,
      [productId]: currentQuantity - 1,
    });
  };

  // 手動輸入數量
  const handleQuantityInput = (productId, value, stock) => {
    const numQuantity = parseInt(value, 10);
    if (isNaN(numQuantity) || numQuantity < 1) {
      setQuantities({ ...quantities, [productId]: 1 });
      return;
    }

    const cartQuantity = getItemQuantity(productId);
    const remainingStock = stock - cartQuantity;

    if (numQuantity > remainingStock) {
      showSnackbar('超過庫存限制', 'warning');
      setQuantities({ ...quantities, [productId]: remainingStock });
      return;
    }

    setQuantities({ ...quantities, [productId]: numQuantity });
  };

  // 加入購物車
  const handleAddToCart = async (product) => {
    const quantity = quantities[product.id] || 1;

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
        showSnackbar(
          result.message || `已將 ${quantity} 件「${product.name}」加入購物車`,
          'success'
        );
        // 重置數量為 1
        setQuantities({ ...quantities, [product.id]: 1 });
      } else {
        showSnackbar(result.error || '加入購物車失敗', 'error');
      }
    } catch (err) {
      showSnackbar(err.message || '加入購物車失敗', 'error');
    }
  };

  // 前往商品詳情
  const handleProductClick = (productId) => {
    navigate(`/products/${productId}`);
  };

  // 取得頁面標題
  const getPageTitle = () => {
    if (searchKeyword) {
      return `搜尋結果: ${searchKeyword}`;
    }
    if (categoryId) {
      return '商品分類';
    }
    return '所有商品';
  };

  return (
    <Container maxWidth="lg" className={styles.container}>
      {/* 頁面標題 */}
      <Box className={styles.header}>
        <Typography variant="h4" component="h1" gutterBottom>
          {getPageTitle()}
        </Typography>
        {totalItems > 0 && (
          <Chip
            label={`共 ${totalItems} 件商品`}
            color="primary"
            variant="outlined"
          />
        )}
      </Box>

      {/* 載入中 */}
      {loading && (
        <Box className={styles.loadingBox}>
          <CircularProgress />
          <Typography variant="body1" sx={{ mt: 2 }}>
            載入中...
          </Typography>
        </Box>
      )}

      {/* 錯誤訊息 */}
      {error && !loading && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* 商品列表 */}
      {!loading && !error && products.length > 0 && (
        <>
          <Grid2 container spacing={3}>
            {products.map((product) => {
              const cartQuantity = getItemQuantity(product.id);
              const remainingStock = product.stock - cartQuantity;
              const selectedQuantity = quantities[product.id] || 1;

              return (
                <Grid2 size={{ xs: 12, sm: 6, md: 4, lg: 3 }} key={product.id}>
                  <Card className={styles.productCard}>
                    {/* 購物車徽章 */}
                    {cartQuantity > 0 && (
                      <Badge
                        badgeContent={cartQuantity}
                        color="primary"
                        className={styles.cartBadge}
                      >
                        <MdShoppingCart size={24} />
                      </Badge>
                    )}

                    {/* 商品圖片 */}
                    <CardMedia
                      component="img"
                      height="200"
                      image={product.imageUrl || 'https://via.placeholder.com/300x300?text=No+Image'}
                      alt={product.name}
                      className={styles.productImage}
                      onClick={() => handleProductClick(product.id)}
                    />

                    {/* 商品資訊 */}
                    <CardContent
                      className={styles.productContent}
                      onClick={() => handleProductClick(product.id)}
                    >
                      <Typography
                        variant="h6"
                        component="h2"
                        className={styles.productName}
                        gutterBottom
                      >
                        {product.name}
                      </Typography>

                      <Box className={styles.priceBox}>
                        <Typography variant="h5" color="primary" fontWeight="bold">
                          NT$ {product.price.toLocaleString()}
                        </Typography>
                      </Box>

                      <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                        庫存: {product.stock > 0 ? product.stock : '缺貨'}
                      </Typography>

                      {cartQuantity > 0 && (
                        <Typography variant="caption" color="primary" display="block">
                          購物車中已有 {cartQuantity} 件
                        </Typography>
                      )}
                    </CardContent>

                    {/* 數量選擇器 */}
                    {product.stock > 0 && (
                      <Box
                        className={styles.quantityControl}
                        onClick={(e) => e.stopPropagation()}
                      >
                        <Typography variant="body2" color="text.secondary" sx={{ mr: 1 }}>
                          數量:
                        </Typography>
                        <IconButton
                          size="small"
                          onClick={() => handleDecreaseQuantity(product.id)}
                          disabled={selectedQuantity <= 1 || cartLoading}
                        >
                          <MdRemove />
                        </IconButton>
                        <TextField
                          size="small"
                          type="number"
                          value={selectedQuantity}
                          onChange={(e) =>
                            handleQuantityInput(product.id, e.target.value, product.stock)
                          }
                          disabled={cartLoading}
                          inputProps={{
                            min: 1,
                            max: remainingStock,
                            style: { textAlign: 'center' },
                          }}
                          className={styles.quantityInput}
                        />
                        <IconButton
                          size="small"
                          onClick={() => handleIncreaseQuantity(product)}
                          disabled={selectedQuantity >= remainingStock || cartLoading}
                        >
                          <MdAdd />
                        </IconButton>
                      </Box>
                    )}

                    {/* 操作按鈕 */}
                    <CardActions className={styles.productActions}>
                      <Button
                        fullWidth
                        variant="contained"
                        startIcon={cartLoading ? <CircularProgress size={20} /> : <MdShoppingCart />}
                        onClick={() => handleAddToCart(product)}
                        disabled={product.stock <= 0 || remainingStock <= 0 || cartLoading}
                      >
                        {product.stock <= 0
                          ? '已售完'
                          : remainingStock <= 0
                          ? '已達購買上限'
                          : cartLoading
                          ? '處理中...'
                          : '加入購物車'}
                      </Button>
                    </CardActions>
                  </Card>
                </Grid2>
              );
            })}
          </Grid2>

          {/* 分頁 */}
          {totalPages > 1 && (
            <Box className={styles.paginationBox}>
              <Pagination
                count={totalPages}
                page={currentPage}
                onChange={handlePageChange}
                color="primary"
                size="large"
                showFirstButton
                showLastButton
              />
            </Box>
          )}
        </>
      )}

      {/* 空狀態 */}
      {!loading && !error && products.length === 0 && (
        <Box className={styles.emptyBox}>
          <MdSearch size={80} color="#ccc" />
          <Typography variant="h5" color="text.secondary" sx={{ mt: 2 }}>
            {searchKeyword ? '找不到相關商品' : '目前沒有商品'}
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ mt: 1 }}>
            {searchKeyword ? '請嘗試其他關鍵字' : '請稍後再來'}
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

export default Products;
