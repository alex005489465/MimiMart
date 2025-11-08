/**
 * 購物車頁面
 * 使用 MUI v6 和 cartStore (整合後端 API)
 */
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Card,
  CardContent,
  CardMedia,
  Button,
  TextField,
  Typography,
  Box,
  Divider,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Snackbar,
  Alert,
  IconButton,
  Grid2,
  CircularProgress,
  Chip,
} from '@mui/material';
import {
  MdDelete,
  MdShoppingCart,
  MdShoppingBag,
  MdAdd,
  MdRemove,
} from 'react-icons/md';
import useCartStore from '../../stores/cartStore';
import useAuthStore from '../../stores/authStore';
import styles from './Cart.module.css';

const Cart = () => {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuthStore();
  const {
    items,
    getTotalPrice,
    getTotalItems,
    updateQuantity,
    removeItem,
    clearCart,
    syncCart,
    loading,
    error,
  } = useCartStore();

  // Snackbar 狀態管理
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success',
  });

  // 確認對話框狀態管理
  const [deleteDialog, setDeleteDialog] = useState({
    open: false,
    productId: null,
  });

  const [clearDialog, setClearDialog] = useState(false);

  // 會員登入時同步購物車
  useEffect(() => {
    if (isAuthenticated) {
      syncCart();
    }
  }, [isAuthenticated]);

  // 顯示錯誤訊息
  useEffect(() => {
    if (error) {
      showSnackbar(error, 'error');
    }
  }, [error]);

  // 顯示 Snackbar
  const showSnackbar = (message, severity = 'success') => {
    setSnackbar({ open: true, message, severity });
  };

  // 關閉 Snackbar
  const handleCloseSnackbar = () => {
    setSnackbar({ ...snackbar, open: false });
  };

  // 增加數量
  const handleIncreaseQuantity = async (item) => {
    const newQuantity = item.quantity + 1;
    if (newQuantity > item.stock) {
      showSnackbar('已達庫存上限', 'warning');
      return;
    }

    const result = await updateQuantity(item.productId, newQuantity);
    if (result.success) {
      showSnackbar('數量已更新', 'success');
    } else {
      showSnackbar(result.error, 'error');
    }
  };

  // 減少數量
  const handleDecreaseQuantity = async (item) => {
    if (item.quantity <= 1) {
      openDeleteDialog(item.productId);
      return;
    }

    const result = await updateQuantity(item.productId, item.quantity - 1);
    if (result.success) {
      showSnackbar('數量已更新', 'success');
    } else {
      showSnackbar(result.error, 'error');
    }
  };

  // 手動輸入數量
  const handleQuantityInput = async (productId, value, stock) => {
    const numQuantity = parseInt(value, 10);
    if (isNaN(numQuantity) || numQuantity < 1) return;

    if (numQuantity > stock) {
      showSnackbar('超過庫存限制', 'warning');
      return;
    }

    const result = await updateQuantity(productId, numQuantity);
    if (!result.success) {
      showSnackbar(result.error, 'error');
    }
  };

  // 開啟移除商品對話框
  const openDeleteDialog = (productId) => {
    setDeleteDialog({ open: true, productId });
  };

  // 關閉移除商品對話框
  const closeDeleteDialog = () => {
    setDeleteDialog({ open: false, productId: null });
  };

  // 確認移除商品
  const confirmRemove = async () => {
    if (deleteDialog.productId) {
      const result = await removeItem(deleteDialog.productId);
      if (result.success) {
        showSnackbar('已移除商品', 'success');
      } else {
        showSnackbar(result.error, 'error');
      }
      closeDeleteDialog();
    }
  };

  // 開啟清空購物車對話框
  const openClearDialog = () => {
    setClearDialog(true);
  };

  // 關閉清空購物車對話框
  const closeClearDialog = () => {
    setClearDialog(false);
  };

  // 確認清空購物車
  const confirmClearCart = async () => {
    const result = await clearCart();
    if (result.success) {
      showSnackbar('已清空購物車', 'success');
    } else {
      showSnackbar(result.error, 'error');
    }
    closeClearDialog();
  };

  // 前往結帳
  const handleCheckout = () => {
    navigate('/checkout');
  };

  // 購物車為空的狀態
  if (items.length === 0 && !loading) {
    return (
      <div className={styles.cartPage}>
        <Box className={styles.container}>
          <Card>
            <CardContent>
              <Box
                sx={{
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  py: 8,
                }}
              >
                <MdShoppingCart size={80} color="#ccc" />
                <Typography variant="h6" color="text.secondary" sx={{ mt: 2 }}>
                  購物車是空的
                </Typography>
                <Button
                  variant="contained"
                  color="primary"
                  size="large"
                  startIcon={<MdShoppingBag />}
                  onClick={() => navigate('/products')}
                  sx={{ mt: 3 }}
                >
                  去逛逛
                </Button>
              </Box>
            </CardContent>
          </Card>
        </Box>
      </div>
    );
  }

  // 載入中狀態
  if (loading && items.length === 0) {
    return (
      <div className={styles.cartPage}>
        <Box className={styles.container}>
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
            <CircularProgress />
          </Box>
        </Box>
      </div>
    );
  }

  return (
    <div className={styles.cartPage}>
      <Box className={styles.container}>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
          <MdShoppingCart size={32} />
          <Typography variant="h4" sx={{ ml: 1 }}>
            購物車
          </Typography>
        </Box>

        <Card>
          <CardContent>
            {/* 購物車項目列表 */}
            {items.map((item) => (
              <Box key={item.productId}>
                <Grid2 container spacing={2} sx={{ py: 2 }}>
                  {/* 商品圖片與名稱 */}
                  <Grid2 size={{ xs: 12, sm: 6, md: 5 }}>
                    <Box sx={{ display: 'flex', gap: 2 }}>
                      <CardMedia
                        component="img"
                        image={item.image || 'https://via.placeholder.com/100'}
                        alt={item.name}
                        sx={{
                          width: 100,
                          height: 100,
                          objectFit: 'cover',
                          borderRadius: 1,
                        }}
                      />
                      <Box>
                        <Typography variant="subtitle1" fontWeight="bold">
                          {item.name}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          商品編號: {item.productId}
                        </Typography>
                        {item.categoryName && (
                          <Chip
                            label={item.categoryName}
                            size="small"
                            sx={{ mt: 1 }}
                          />
                        )}
                        {item.isOutOfStock && (
                          <Chip
                            label="缺貨"
                            color="error"
                            size="small"
                            sx={{ mt: 1, ml: 1 }}
                          />
                        )}
                      </Box>
                    </Box>
                  </Grid2>

                  {/* 單價 */}
                  <Grid2
                    size={{ xs: 6, sm: 3, md: 2 }}
                    sx={{
                      display: 'flex',
                      flexDirection: 'column',
                      justifyContent: 'center',
                    }}
                  >
                    <Typography variant="body2" color="text.secondary">
                      單價
                    </Typography>
                    <Typography variant="body1">
                      NT$ {item.price.toLocaleString()}
                    </Typography>
                  </Grid2>

                  {/* 數量控制 */}
                  <Grid2
                    size={{ xs: 6, sm: 3, md: 2 }}
                    sx={{
                      display: 'flex',
                      flexDirection: 'column',
                      justifyContent: 'center',
                    }}
                  >
                    <Typography variant="body2" color="text.secondary">
                      數量
                    </Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', mt: 1 }}>
                      <IconButton
                        size="small"
                        onClick={() => handleDecreaseQuantity(item)}
                        disabled={loading || item.isOutOfStock}
                      >
                        <MdRemove />
                      </IconButton>
                      <TextField
                        size="small"
                        type="number"
                        value={item.quantity}
                        onChange={(e) =>
                          handleQuantityInput(
                            item.productId,
                            e.target.value,
                            item.stock
                          )
                        }
                        disabled={loading || item.isOutOfStock}
                        inputProps={{
                          min: 1,
                          max: item.stock,
                          style: { textAlign: 'center' },
                        }}
                        sx={{ width: 60, mx: 1 }}
                      />
                      <IconButton
                        size="small"
                        onClick={() => handleIncreaseQuantity(item)}
                        disabled={loading || item.isOutOfStock}
                      >
                        <MdAdd />
                      </IconButton>
                    </Box>
                    <Typography
                      variant="caption"
                      color="text.secondary"
                      sx={{ mt: 0.5 }}
                    >
                      庫存: {item.stock}
                    </Typography>
                  </Grid2>

                  {/* 小計 */}
                  <Grid2
                    size={{ xs: 6, sm: 6, md: 2 }}
                    sx={{
                      display: 'flex',
                      flexDirection: 'column',
                      justifyContent: 'center',
                    }}
                  >
                    <Typography variant="body2" color="text.secondary">
                      小計
                    </Typography>
                    <Typography variant="h6" color="primary">
                      NT$ {(item.price * item.quantity).toLocaleString()}
                    </Typography>
                  </Grid2>

                  {/* 操作按鈕 */}
                  <Grid2
                    size={{ xs: 6, sm: 6, md: 1 }}
                    sx={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'flex-end',
                    }}
                  >
                    <IconButton
                      color="error"
                      onClick={() => openDeleteDialog(item.productId)}
                      disabled={loading}
                    >
                      <MdDelete />
                    </IconButton>
                  </Grid2>
                </Grid2>
                <Divider />
              </Box>
            ))}

            {/* 底部操作區 */}
            <Box
              sx={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                mt: 3,
                flexWrap: 'wrap',
                gap: 2,
              }}
            >
              <Button
                variant="outlined"
                color="error"
                onClick={openClearDialog}
                disabled={loading}
              >
                清空購物車
              </Button>

              <Box sx={{ textAlign: 'right' }}>
                <Typography variant="body1" sx={{ mb: 1 }}>
                  共 <strong>{getTotalItems()}</strong> 件商品 (
                  {items.length} 種)
                </Typography>
                <Typography variant="h5" color="error" fontWeight="bold">
                  總計:NT$ {getTotalPrice().toLocaleString()}
                </Typography>
                <Button
                  variant="contained"
                  color="primary"
                  size="large"
                  onClick={handleCheckout}
                  disabled={loading || items.some((item) => item.isOutOfStock)}
                  sx={{ mt: 2, minWidth: 200 }}
                >
                  前往結帳
                </Button>
                {items.some((item) => item.isOutOfStock) && (
                  <Typography variant="caption" color="error" display="block">
                    購物車中有缺貨商品,請先移除
                  </Typography>
                )}
              </Box>
            </Box>
          </CardContent>
        </Card>
      </Box>

      {/* 移除商品確認對話框 */}
      <Dialog open={deleteDialog.open} onClose={closeDeleteDialog}>
        <DialogTitle>移除商品</DialogTitle>
        <DialogContent>
          <DialogContentText>確定要移除此商品嗎?</DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={closeDeleteDialog}>取消</Button>
          <Button onClick={confirmRemove} color="error" variant="contained">
            確定
          </Button>
        </DialogActions>
      </Dialog>

      {/* 清空購物車確認對話框 */}
      <Dialog open={clearDialog} onClose={closeClearDialog}>
        <DialogTitle>清空購物車</DialogTitle>
        <DialogContent>
          <DialogContentText>確定要清空整個購物車嗎?</DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={closeClearDialog}>取消</Button>
          <Button onClick={confirmClearCart} color="error" variant="contained">
            確定
          </Button>
        </DialogActions>
      </Dialog>

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
    </div>
  );
};

export default Cart;
