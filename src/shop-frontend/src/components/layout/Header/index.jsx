/**
 * Header 主元件
 * 整合所有 Header 子元件
 */
import { useState } from 'react';
import { Layout, Drawer, Button } from 'antd';
import { MenuOutlined } from '@ant-design/icons';
import Logo from './Logo';
import MainNav from './MainNav';
import SearchBar from './SearchBar';
import CartButton from './CartButton';
import UserMenu from './UserMenu';
import ThemeToggle from './ThemeToggle';
import useUIStore from '../../../stores/uiStore';
import styles from './Header.module.css';

const { Header: AntHeader } = Layout;

const Header = () => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const theme = useUIStore((state) => state.theme);

  return (
    <AntHeader className={styles.header}>
      <div className={styles.container}>
        {/* Logo */}
        <Logo />

        {/* 桌面版導航 */}
        <div className={styles.desktopNav}>
          <MainNav />
        </div>

        {/* 搜尋欄 */}
        <div className={styles.searchContainer}>
          <SearchBar />
        </div>

        {/* 右側工具列 */}
        <div className={styles.actions}>
          <ThemeToggle />
          <CartButton />
          <UserMenu />

          {/* 行動版選單按鈕 */}
          <Button
            type="text"
            icon={<MenuOutlined style={{ fontSize: '20px' }} />}
            className={styles.mobileMenuButton}
            onClick={() => setMobileMenuOpen(true)}
            aria-label="開啟選單"
          />
        </div>
      </div>

      {/* 行動版抽屜選單 */}
      <Drawer
        title="選單"
        placement="right"
        onClose={() => setMobileMenuOpen(false)}
        open={mobileMenuOpen}
        width={280}
      >
        <MainNav mode="inline" />
      </Drawer>
    </AntHeader>
  );
};

export default Header;
