/**
 * Header 主元件
 * 整合所有 Header 子元件
 */
import { AppBar, Toolbar, Box } from '@mui/material';
import Logo from './Logo';
import SearchBar from './SearchBar';
import CartButton from './CartButton';
import UserMenu from './UserMenu';
import ThemeToggle from './ThemeToggle';
import styles from './Header.module.css';

const Header = () => {
  return (
    <AppBar position="static" color="default" elevation={1}>
      <Toolbar className={styles.header} sx={{ minHeight: 64 }}>
        <Box className={styles.container}>
          {/* Logo */}
          <Logo />

          {/* 搜尋欄 */}
          <Box className={styles.searchContainer}>
            <SearchBar />
          </Box>

          {/* 右側工具列 */}
          <Box className={styles.actions}>
            <ThemeToggle />
            <CartButton />
            <UserMenu />
          </Box>
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default Header;
