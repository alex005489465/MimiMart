/**
 * 主題切換按鈕元件
 */
import { Button } from 'antd';
import { BulbOutlined, BulbFilled } from '@ant-design/icons';
import useUIStore from '../../../stores/uiStore';
import styles from './Header.module.css';

const ThemeToggle = () => {
  const { theme, toggleTheme } = useUIStore();
  const isDark = theme === 'dark';

  return (
    <Button
      type="text"
      icon={
        isDark ? (
          <BulbFilled style={{ fontSize: '20px', color: '#faad14' }} />
        ) : (
          <BulbOutlined style={{ fontSize: '20px' }} />
        )
      }
      onClick={toggleTheme}
      aria-label={isDark ? '切換至亮色模式' : '切換至深色模式'}
      className={styles.iconButton}
    />
  );
};

export default ThemeToggle;
