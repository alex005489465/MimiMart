/**
 * 購物車按鈕元件
 */
import { Badge, Button } from 'antd';
import { ShoppingCartOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import useCartStore from '../../../stores/cartStore';
import styles from './Header.module.css';

const CartButton = () => {
  const navigate = useNavigate();
  const getTotalItems = useCartStore((state) => state.getTotalItems);
  const totalItems = getTotalItems();

  const handleClick = () => {
    navigate('/cart');
  };

  return (
    <Badge count={totalItems} className={styles.cartBadge} showZero>
      <Button
        type="text"
        icon={<ShoppingCartOutlined style={{ fontSize: '20px' }} />}
        onClick={handleClick}
        aria-label="購物車"
        className={styles.iconButton}
      />
    </Badge>
  );
};

export default CartButton;
