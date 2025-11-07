/**
 * MimiMart 主題配置
 * 統一管理顏色、字體、間距等設計 token
 */

// 亮色主題
export const lightTheme = {
  token: {
    // 品牌色
    colorPrimary: '#1890ff',
    colorSuccess: '#52c41a',
    colorWarning: '#faad14',
    colorError: '#ff4d4f',
    colorInfo: '#1890ff',

    // 背景色
    colorBgBase: '#ffffff',
    colorBgContainer: '#ffffff',
    colorBgElevated: '#ffffff',
    colorBgLayout: '#f5f5f5',

    // 文字色
    colorText: '#333333',
    colorTextSecondary: '#666666',
    colorTextTertiary: '#999999',
    colorTextDisabled: '#cccccc',

    // 邊框
    colorBorder: '#e8e8e8',
    colorBorderSecondary: '#f0f0f0',
    borderRadius: 8,

    // 字體
    fontSize: 14,
    fontSizeHeading1: 38,
    fontSizeHeading2: 30,
    fontSizeHeading3: 24,
    fontSizeHeading4: 20,
    fontSizeHeading5: 16,
    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji"',

    // 間距
    marginXS: 8,
    marginSM: 12,
    margin: 16,
    marginMD: 20,
    marginLG: 24,
    marginXL: 32,
    marginXXL: 48,

    // 陰影
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
    boxShadowSecondary: '0 4px 12px rgba(0,0,0,0.15)',

    // 佈局
    controlHeight: 40,
    controlHeightLG: 48,
    controlHeightSM: 32,
  },

  // 元件專用配置
  components: {
    Layout: {
      headerBg: '#ffffff',
      headerHeight: 64,
      headerPadding: '0 24px',
      footerBg: '#001529',
      footerPadding: '48px 24px 24px',
    },
    Menu: {
      itemBg: 'transparent',
      itemSelectedBg: '#e6f7ff',
      itemSelectedColor: '#1890ff',
      horizontalItemSelectedBg: 'transparent',
    },
    Button: {
      controlHeight: 40,
      controlHeightLG: 48,
      controlHeightSM: 32,
    },
    Input: {
      controlHeight: 40,
      controlHeightLG: 48,
      controlHeightSM: 32,
    },
    Card: {
      borderRadiusLG: 12,
    },
  },
};

// 深色主題
export const darkTheme = {
  token: {
    // 品牌色
    colorPrimary: '#1890ff',
    colorSuccess: '#52c41a',
    colorWarning: '#faad14',
    colorError: '#ff4d4f',
    colorInfo: '#1890ff',

    // 背景色
    colorBgBase: '#141414',
    colorBgContainer: '#1f1f1f',
    colorBgElevated: '#2a2a2a',
    colorBgLayout: '#000000',

    // 文字色
    colorText: '#e8e8e8',
    colorTextSecondary: '#999999',
    colorTextTertiary: '#666666',
    colorTextDisabled: '#444444',

    // 邊框
    colorBorder: '#3a3a3a',
    colorBorderSecondary: '#2a2a2a',
    borderRadius: 8,

    // 字體
    fontSize: 14,
    fontSizeHeading1: 38,
    fontSizeHeading2: 30,
    fontSizeHeading3: 24,
    fontSizeHeading4: 20,
    fontSizeHeading5: 16,
    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji"',

    // 間距
    marginXS: 8,
    marginSM: 12,
    margin: 16,
    marginMD: 20,
    marginLG: 24,
    marginXL: 32,
    marginXXL: 48,

    // 陰影
    boxShadow: '0 2px 8px rgba(0,0,0,0.3)',
    boxShadowSecondary: '0 4px 12px rgba(0,0,0,0.45)',

    // 佈局
    controlHeight: 40,
    controlHeightLG: 48,
    controlHeightSM: 32,
  },

  // 元件專用配置
  components: {
    Layout: {
      headerBg: '#1f1f1f',
      headerHeight: 64,
      headerPadding: '0 24px',
      footerBg: '#000000',
      footerPadding: '48px 24px 24px',
    },
    Menu: {
      itemBg: 'transparent',
      itemSelectedBg: '#1f1f1f',
      itemSelectedColor: '#1890ff',
      horizontalItemSelectedBg: 'transparent',
    },
    Button: {
      controlHeight: 40,
      controlHeightLG: 48,
      controlHeightSM: 32,
    },
    Input: {
      controlHeight: 40,
      controlHeightLG: 48,
      controlHeightSM: 32,
    },
    Card: {
      borderRadiusLG: 12,
    },
  },
};

// 響應式斷點（與 Ant Design 保持一致）
export const breakpoints = {
  xs: 480,
  sm: 576,
  md: 768,
  lg: 992,
  xl: 1200,
  xxl: 1600,
};

// 通用間距
export const spacing = {
  xs: 8,
  sm: 12,
  md: 16,
  lg: 24,
  xl: 32,
  xxl: 48,
};

// Z-index 層級
export const zIndex = {
  dropdown: 1050,
  sticky: 1020,
  fixed: 1030,
  modalBackdrop: 1040,
  modal: 1050,
  popover: 1060,
  tooltip: 1070,
};
