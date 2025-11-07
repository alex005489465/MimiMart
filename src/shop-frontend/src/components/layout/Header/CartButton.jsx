/**
 * 購物車按鈕元件
 */
import { Badge, IconButton } from '@mui/material';
import { MdShoppingCart } from 'react-icons/md';
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
    <Badge badgeContent={totalItems} color="primary" className={styles.cartBadge} showZero>
      <IconButton
        onClick={handleClick}
        aria-label="購物車"
        className={styles.iconButton}
        size="large"
      >
        <MdShoppingCart size={20} />
      </IconButton>
    </Badge>
  );
};

export default CartButton;
