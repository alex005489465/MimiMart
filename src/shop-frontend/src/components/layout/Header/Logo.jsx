/**
 * Logo 元件
 */
import { Link } from 'react-router-dom';
import { MdStorefront } from 'react-icons/md';
import styles from './Header.module.css';

const Logo = () => {
  return (
    <Link to="/" className={styles.logo}>
      <MdStorefront className={styles.logoIcon} />
      <span className={styles.logoText}>MimiMart</span>
    </Link>
  );
};

export default Logo;
