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
  Chip
} from '@mui/material';
import { MdShoppingCart, MdSearch } from 'react-icons/md';
import productService from '../../services/productService';
import useCartStore from '../../stores/cartStore';
import styles from './Products.module.css';

const Products = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const addItem = useCartStore((state) => state.addItem);

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
        setProducts(response.data || []);
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

  // 加入購物車
  const handleAddToCart = (product) => {
    try {
      addItem({
        productId: product.id,
        name: product.name,
        price: product.price,
        quantity: 1,
        image: product.imageUrl || 'https://via.placeholder.com/300x300?text=No+Image',
        stock: product.stock
      });

      // TODO: 可以加入 snackbar 或 toast 提示
      alert(`已將「${product.name}」加入購物車`);
    } catch (err) {
      alert(err.message || '加入購物車失敗');
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
            {products.map((product) => (
              <Grid2 size={{ xs: 12, sm: 6, md: 4, lg: 3 }} key={product.id}>
                <Card className={styles.productCard}>
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

                    <Typography variant="body2" color="text.secondary">
                      庫存: {product.stock > 0 ? product.stock : '缺貨'}
                    </Typography>
                  </CardContent>

                  {/* 操作按鈕 */}
                  <CardActions className={styles.productActions}>
                    <Button
                      fullWidth
                      variant="contained"
                      startIcon={<MdShoppingCart />}
                      onClick={() => handleAddToCart(product)}
                      disabled={product.stock <= 0}
                    >
                      {product.stock > 0 ? '加入購物車' : '已售完'}
                    </Button>
                  </CardActions>
                </Card>
              </Grid2>
            ))}
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
    </Container>
  );
};

export default Products;
