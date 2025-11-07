/**
 * 購物車頁面
 * 使用 MUI v6 和 cartStore
 */
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Card,
  CardContent,
  Button,
  TextField,
  Typography,
  Box,
  TableContainer,
  Table,
  TableHead,
  TableBody,
  TableRow,
  TableCell,
  Paper,
  Divider,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Snackbar,
  Alert,
} from '@mui/material';
import {
  MdDelete,
  MdShoppingCart,
  MdShoppingBag,
} from 'react-icons/md';
import useCartStore from '../../stores/cartStore';
import styles from './Cart.module.css';

const Cart = () => {
  const navigate = useNavigate();
  const { items, getTotalPrice, updateQuantity, removeItem, clearCart } =
    useCartStore();

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

  // 顯示 Snackbar
  const showSnackbar = (message, severity = 'success') => {
    setSnackbar({ open: true, message, severity });
  };

  // 關閉 Snackbar
  const handleCloseSnackbar = () => {
    setSnackbar({ ...snackbar, open: false });
  };

  // 更新商品數量
  const handleQuantityChange = (productId, quantity) => {
    const numQuantity = parseInt(quantity, 10);
    if (isNaN(numQuantity) || numQuantity < 1) return;

    const result = updateQuantity(productId, numQuantity);
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
  const confirmRemove = () => {
    if (deleteDialog.productId) {
      removeItem(deleteDialog.productId);
      showSnackbar('已移除商品', 'success');
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
  const confirmClearCart = () => {
    clearCart();
    showSnackbar('已清空購物車', 'success');
    closeClearDialog();
  };

  // 前往結帳
  const handleCheckout = () => {
    showSnackbar('結帳功能開發中', 'info');
    // navigate('/checkout');
  };

  // 表格欄位定義
  const columns = [
    {
      title: '商品',
      dataIndex: 'name',
      key: 'name',
      render: (text, record) => (
        <Space>
          <Image
            src={record.image || 'https://via.placeholder.com/80'}
            alt={text}
            width={80}
            height={80}
            style={{ objectFit: 'cover', borderRadius: 8 }}
            preview={false}
          />
          <div>
            <Text strong>{text}</Text>
            <br />
            <Text type="secondary">商品編號: {record.productId}</Text>
          </div>
        </Space>
      ),
    },
    {
      title: '單價',
      dataIndex: 'price',
      key: 'price',
      width: 120,
      render: (price) => <Text>NT$ {price.toLocaleString()}</Text>,
    },
    {
      title: '數量',
      dataIndex: 'quantity',
      key: 'quantity',
      width: 150,
      render: (quantity, record) => (
        <InputNumber
          min={1}
          max={record.stock}
          value={quantity}
          onChange={(value) => handleQuantityChange(record.productId, value)}
        />
      ),
    },
    {
      title: '小計',
      key: 'subtotal',
      width: 120,
      render: (_, record) => (
        <Text strong>
          NT$ {(record.price * record.quantity).toLocaleString()}
        </Text>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 100,
      render: (_, record) => (
        <Popconfirm
          title="確定要移除此商品嗎？"
          onConfirm={() => handleRemove(record.productId)}
          okText="確定"
          cancelText="取消"
        >
          <Button type="text" danger icon={<DeleteOutlined />}>
            移除
          </Button>
        </Popconfirm>
      ),
    },
  ];

  // 購物車為空的狀態
  if (items.length === 0) {
    return (
      <div className={styles.cartPage}>
        <div className={styles.container}>
          <Card>
            <Empty
              image={<ShoppingCartOutlined style={{ fontSize: 80 }} />}
              description="購物車是空的"
            >
              <Button
                type="primary"
                size="large"
                icon={<ShoppingOutlined />}
                onClick={() => navigate('/products')}
              >
                去逛逛
              </Button>
            </Empty>
          </Card>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.cartPage}>
      <div className={styles.container}>
        <Title level={2}>
          <ShoppingCartOutlined /> 購物車
        </Title>

        <Card>
          <Table
            dataSource={items}
            columns={columns}
            rowKey="productId"
            pagination={false}
            scroll={{ x: 800 }}
          />

          <Divider />

          <div className={styles.footer}>
            <Space>
              <Popconfirm
                title="確定要清空購物車嗎？"
                onConfirm={handleClearCart}
                okText="確定"
                cancelText="取消"
              >
                <Button danger>清空購物車</Button>
              </Popconfirm>
            </Space>

            <div className={styles.summary}>
              <Space direction="vertical" align="end" size="small">
                <Text>
                  共 <Text strong>{items.length}</Text> 件商品
                </Text>
                <Title level={3} style={{ margin: 0, color: '#ff4d4f' }}>
                  總計：NT$ {getTotalPrice().toLocaleString()}
                </Title>
                <Button
                  type="primary"
                  size="large"
                  onClick={handleCheckout}
                  style={{ width: 200 }}
                >
                  前往結帳
                </Button>
              </Space>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
};

export default Cart;
