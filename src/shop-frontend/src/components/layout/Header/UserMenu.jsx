/**
 * 使用者選單元件
 */
import { useState } from 'react';
import { Menu, MenuItem, Button, Avatar, Divider, Box } from '@mui/material';
import {
  MdPerson,
  MdSettings,
  MdLogout,
  MdLogin,
  MdPersonAdd,
} from 'react-icons/md';
import { useNavigate } from 'react-router-dom';
import useAuthStore from '../../../stores/authStore';
import styles from './Header.module.css';

const UserMenu = () => {
  const navigate = useNavigate();
  const { isAuthenticated, user, logout } = useAuthStore();
  const [anchorEl, setAnchorEl] = useState(null);

  const handleClick = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    logout();
    navigate('/');
    handleClose();
  };

  const handleMenuClick = (path) => {
    navigate(path);
    handleClose();
  };

  return (
    <>
      <Button
        onClick={handleClick}
        className={styles.userMenuButton}
        aria-label="使用者選單"
        sx={{
          color: 'text.primary',
          textTransform: 'none',
          minWidth: 'auto',
          gap: 0.5,
        }}
      >
        {isAuthenticated ? (
          <>
            <Avatar sx={{ width: 24, height: 24 }}>
              <MdPerson />
            </Avatar>
            <span className={styles.username}>{user?.username || '會員'}</span>
          </>
        ) : (
          <>
            <MdPerson size={20} />
            <span className={styles.username}>登入/註冊</span>
          </>
        )}
      </Button>

      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleClose}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'right',
        }}
        transformOrigin={{
          vertical: 'top',
          horizontal: 'right',
        }}
      >
        {isAuthenticated ? (
          [
            <MenuItem key="profile" onClick={() => handleMenuClick('/member')}>
              <MdPerson style={{ marginRight: 8 }} />
              會員中心
            </MenuItem>,
            <Divider key="divider1" />,
            <MenuItem key="settings" onClick={() => handleMenuClick('/member/settings')}>
              <MdSettings style={{ marginRight: 8 }} />
              帳號設定
            </MenuItem>,
            <Divider key="divider2" />,
            <MenuItem key="logout" onClick={handleLogout} sx={{ color: 'error.main' }}>
              <MdLogout style={{ marginRight: 8 }} />
              登出
            </MenuItem>,
          ]
        ) : (
          [
            <MenuItem key="login" onClick={() => handleMenuClick('/login')}>
              <MdLogin style={{ marginRight: 8 }} />
              登入
            </MenuItem>,
            <MenuItem key="register" onClick={() => handleMenuClick('/login?tab=register')}>
              <MdPersonAdd style={{ marginRight: 8 }} />
              註冊
            </MenuItem>,
          ]
        )}
      </Menu>
    </>
  );
};

export default UserMenu;
