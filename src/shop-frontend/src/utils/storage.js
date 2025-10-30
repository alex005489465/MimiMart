/**
 * LocalStorage 工具函式
 * 用於管理 Token 和其他持久化資料
 */

const STORAGE_KEYS = {
  ACCESS_TOKEN: 'accessToken',
  REFRESH_TOKEN: 'refreshToken',
  USER_PROFILE: 'userProfile',
};

export const storage = {
  // Token 管理
  setAccessToken(token) {
    localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, token);
  },

  getAccessToken() {
    return localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
  },

  setRefreshToken(token) {
    localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, token);
  },

  getRefreshToken() {
    return localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN);
  },

  // 使用者資料管理
  setUserProfile(profile) {
    localStorage.setItem(STORAGE_KEYS.USER_PROFILE, JSON.stringify(profile));
  },

  getUserProfile() {
    const profile = localStorage.getItem(STORAGE_KEYS.USER_PROFILE);
    return profile ? JSON.parse(profile) : null;
  },

  // 清除所有認證資料
  clearAuth() {
    localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
    localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
    localStorage.removeItem(STORAGE_KEYS.USER_PROFILE);
  },

  // 檢查是否已登入
  isAuthenticated() {
    return !!this.getAccessToken();
  },
};
