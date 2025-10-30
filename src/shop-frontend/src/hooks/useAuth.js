/**
 * 自訂 Hook 用於存取 AuthContext
 */
import { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';

export const useAuth = () => {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth 必須在 AuthProvider 內部使用');
  }

  return context;
};
