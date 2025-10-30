/**
 * 會員認證狀態管理 Context
 */
import { createContext, useState, useEffect } from 'react';
import { authService } from '../services/authService';
import { memberService } from '../services/memberService';
import { storage } from '../utils/storage';

export const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // 初始化: 檢查本地是否有已登入的使用者
  useEffect(() => {
    const initAuth = async () => {
      try {
        const savedUser = storage.getUserProfile();
        const token = storage.getAccessToken();

        if (savedUser && token) {
          // 嘗試從 API 取得最新的使用者資料
          try {
            const response = await memberService.getProfile();
            if (response.success) {
              setUser(response.data);
              storage.setUserProfile(response.data);
            }
          } catch (error) {
            // 如果 API 失敗,使用本地儲存的資料
            setUser(savedUser);
          }
        }
      } catch (error) {
        console.error('初始化認證失敗:', error);
      } finally {
        setLoading(false);
      }
    };

    initAuth();
  }, []);

  /**
   * 會員註冊
   */
  const register = async (data) => {
    try {
      const response = await authService.register(data);
      if (response.success) {
        return { success: true, data: response.data };
      }
      return { success: false, message: response.message };
    } catch (error) {
      return { success: false, message: error.message || '註冊失敗' };
    }
  };

  /**
   * 會員登入
   */
  const login = async (credentials) => {
    try {
      const response = await authService.login(credentials);
      if (response.success) {
        const { accessToken, refreshToken, profile } = response.data;

        // 儲存 Token 和使用者資料
        storage.setAccessToken(accessToken);
        storage.setRefreshToken(refreshToken);
        storage.setUserProfile(profile);

        // 更新狀態
        setUser(profile);

        return { success: true };
      }
      return { success: false, message: response.message };
    } catch (error) {
      return { success: false, message: error.message || '登入失敗' };
    }
  };

  /**
   * 會員登出
   */
  const logout = () => {
    storage.clearAuth();
    setUser(null);
  };

  /**
   * 更新使用者資料
   */
  const updateUserProfile = (newProfile) => {
    setUser(newProfile);
    storage.setUserProfile(newProfile);
  };

  const value = {
    user,
    loading,
    isAuthenticated: !!user,
    register,
    login,
    logout,
    updateUserProfile,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
