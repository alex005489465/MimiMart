/**
 * 使用者選單元件
 */
import { Dropdown, Button, Avatar } from 'antd';
import {
  UserOutlined,
  SettingOutlined,
  LogoutOutlined,
  LoginOutlined,
  UserAddOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import useAuthStore from '../../../stores/authStore';
import styles from './Header.module.css';

const UserMenu = () => {
  const navigate = useNavigate();
  const { isAuthenticated, user, logout } = useAuthStore();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  // 未登入選單
  const guestItems = [
    {
      key: 'login',
      icon: <LoginOutlined />,
      label: '登入',
      onClick: () => navigate('/login'),
    },
    {
      key: 'register',
      icon: <UserAddOutlined />,
      label: '註冊',
      onClick: () => navigate('/login?tab=register'),
    },
  ];

  // 已登入選單
  const userItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '會員中心',
      onClick: () => navigate('/member'),
    },
    {
      type: 'divider',
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: '帳號設定',
      onClick: () => navigate('/member/settings'),
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '登出',
      onClick: handleLogout,
      danger: true,
    },
  ];

  const menuItems = isAuthenticated ? userItems : guestItems;

  return (
    <Dropdown menu={{ items: menuItems }} placement="bottomRight" arrow>
      <Button
        type="text"
        className={styles.userMenuButton}
        aria-label="使用者選單"
      >
        {isAuthenticated ? (
          <>
            <Avatar
              size="small"
              icon={<UserOutlined />}
              style={{ marginRight: 8 }}
            />
            <span className={styles.username}>{user?.username || '會員'}</span>
          </>
        ) : (
          <>
            <UserOutlined style={{ fontSize: '20px' }} />
            <span className={styles.username}>登入/註冊</span>
          </>
        )}
      </Button>
    </Dropdown>
  );
};

export default UserMenu;
