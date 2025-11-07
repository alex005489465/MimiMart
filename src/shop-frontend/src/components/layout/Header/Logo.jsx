/**
 * Logo 元件
 */
import { Link } from 'react-router-dom';
import { ShoppingOutlined } from '@ant-design/icons';
import styles from './Header.module.css';

const Logo = () => {
  return (
    <Link to="/" className={styles.logo}>
      <ShoppingOutlined className={styles.logoIcon} />
      <span className={styles.logoText}>MimiMart</span>
    </Link>
  );
};

export default Logo;
