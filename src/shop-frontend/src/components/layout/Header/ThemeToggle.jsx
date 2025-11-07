/**
 * 主題切換按鈕元件
 */
import { IconButton } from '@mui/material';
import { MdLightMode, MdDarkMode } from 'react-icons/md';
import useUIStore from '../../../stores/uiStore';
import styles from './Header.module.css';

const ThemeToggle = () => {
  const { theme, toggleTheme } = useUIStore();
  const isDark = theme === 'dark';

  return (
    <IconButton
      onClick={toggleTheme}
      aria-label={isDark ? '切換至亮色模式' : '切換至深色模式'}
      className={styles.iconButton}
      size="large"
      sx={{
        color: isDark ? 'warning.main' : 'text.primary',
      }}
    >
      {isDark ? <MdLightMode size={20} /> : <MdDarkMode size={20} />}
    </IconButton>
  );
};

export default ThemeToggle;
