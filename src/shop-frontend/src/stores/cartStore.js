/**
 * 購物車狀態管理 Store
 *
 * 儲存策略:
 * - 訪客: 使用 LocalStorage 儲存購物車
 * - 會員: 使用後端 API (Redis) 儲存購物車
 * - 登入時: 自動合併訪客購物車與會員購物車
 */
import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import cartService from '../services/cartService';
import useAuthStore from './authStore';

const useCartStore = create(
  persist(
    (set, get) => ({
      // ========== 狀態 ==========
      items: [], // 購物車項目列表
      loading: false, // 載入狀態
      error: null, // 錯誤訊息

      // ========== 私有輔助方法 ==========

      /**
       * 檢查當前是否為會員登入狀態
       */
      _isAuthenticated: () => {
        return useAuthStore.getState().isAuthenticated;
      },

      /**
       * 將後端 CartItemDTO 轉換為前端格式
       */
      _transformCartItem: (backendItem) => ({
        productId: backendItem.productId,
        name: backendItem.productName,
        price: parseFloat(backendItem.price),
        quantity: backendItem.quantity,
        image: backendItem.imageUrl,
        stock: backendItem.stock,
        categoryName: backendItem.categoryName,
        isOutOfStock: backendItem.isOutOfStock,
        totalPrice: parseFloat(backendItem.totalPrice),
      }),

      /**
       * 將前端購物車項目轉換為後端合併格式
       */
      _transformToMergeFormat: (items) => {
        return items.map(item => ({
          productId: item.productId,
          quantity: item.quantity
        }));
      },

      // ========== 計算方法 ==========

      /**
       * 取得購物車項目總數量
       */
      getTotalItems: () => {
        return get().items.reduce((total, item) => total + item.quantity, 0);
      },

      /**
       * 取得購物車總金額
       */
      getTotalPrice: () => {
        return get().items.reduce(
          (total, item) => total + item.price * item.quantity,
          0
        );
      },

      /**
       * 檢查商品是否在購物車中
       */
      isInCart: (productId) => {
        return get().items.some((item) => item.productId === productId);
      },

      /**
       * 取得特定商品的數量
       */
      getItemQuantity: (productId) => {
        const item = get().items.find((item) => item.productId === productId);
        return item ? item.quantity : 0;
      },

      // ========== 購物車操作方法 ==========

      /**
       * 從後端同步購物車資料 (會員專用)
       */
      syncCart: async () => {
        if (!get()._isAuthenticated()) {
          return { success: false, error: '未登入' };
        }

        set({ loading: true, error: null });
        try {
          const response = await cartService.getCart();
          const transformedItems = response.items.map(get()._transformCartItem);
          set({ items: transformedItems, loading: false });
          return { success: true };
        } catch (error) {
          const errorMsg = error.response?.data?.message || '同步購物車失敗';
          set({ loading: false, error: errorMsg });
          return { success: false, error: errorMsg };
        }
      },

      /**
       * 加入商品到購物車
       */
      addItem: async (product) => {
        set({ loading: true, error: null });

        try {
          // 會員模式: 呼叫後端 API
          if (get()._isAuthenticated()) {
            const response = await cartService.addToCart(
              product.productId,
              product.quantity || 1
            );

            // 重新同步購物車
            await get().syncCart();
            set({ loading: false });
            return { success: true, message: '已加入購物車' };
          }

          // 訪客模式: 使用 LocalStorage
          // 檢查庫存是否足夠
          if (product.stock <= 0) {
            set({ loading: false, error: '商品目前缺貨' });
            return { success: false, error: '商品目前缺貨' };
          }

          const items = get().items;
          const existingItemIndex = items.findIndex(
            (item) => item.productId === product.productId
          );

          if (existingItemIndex > -1) {
            // 商品已存在，增加數量
            const newItems = [...items];
            const currentQuantity = newItems[existingItemIndex].quantity;
            const newQuantity = currentQuantity + (product.quantity || 1);

            // 檢查庫存
            if (newQuantity > product.stock) {
              set({ loading: false, error: '商品數量超過庫存限制' });
              return { success: false, error: '商品數量超過庫存限制' };
            }

            newItems[existingItemIndex].quantity = newQuantity;
            set({ items: newItems, loading: false });
          } else {
            // 新增商品 - 檢查要加入的數量是否超過庫存
            const quantityToAdd = product.quantity || 1;
            if (quantityToAdd > product.stock) {
              set({ loading: false, error: '商品數量超過庫存限制' });
              return { success: false, error: '商品數量超過庫存限制' };
            }

            set({
              items: [
                ...items,
                {
                  productId: product.productId,
                  name: product.name,
                  price: product.price,
                  quantity: quantityToAdd,
                  image: product.image,
                  stock: product.stock,
                },
              ],
              loading: false,
            });
          }

          return { success: true, message: '已加入購物車' };
        } catch (error) {
          const errorMsg = error.response?.data?.message || '加入購物車失敗';
          set({ loading: false, error: errorMsg });
          return { success: false, error: errorMsg };
        }
      },

      /**
       * 更新商品數量
       */
      updateQuantity: async (productId, quantity) => {
        set({ loading: true, error: null });

        try {
          // 會員模式: 呼叫後端 API
          if (get()._isAuthenticated()) {
            await cartService.updateCartItem(productId, quantity);

            // 重新同步購物車
            await get().syncCart();
            set({ loading: false });
            return { success: true };
          }

          // 訪客模式: 使用 LocalStorage
          const items = get().items;
          const itemIndex = items.findIndex(
            (item) => item.productId === productId
          );

          if (itemIndex === -1) {
            set({ loading: false, error: '商品不存在' });
            return { success: false, error: '商品不存在' };
          }

          const item = items[itemIndex];

          // 檢查庫存
          if (quantity > item.stock) {
            set({ loading: false, error: '商品數量超過庫存限制' });
            return { success: false, error: '商品數量超過庫存限制' };
          }

          // 如果數量為 0，移除商品
          if (quantity <= 0) {
            await get().removeItem(productId);
            return { success: true };
          }

          const newItems = [...items];
          newItems[itemIndex].quantity = quantity;
          set({ items: newItems, loading: false });

          return { success: true };
        } catch (error) {
          const errorMsg = error.response?.data?.message || '更新數量失敗';
          set({ loading: false, error: errorMsg });
          return { success: false, error: errorMsg };
        }
      },

      /**
       * 移除商品
       */
      removeItem: async (productId) => {
        set({ loading: true, error: null });

        try {
          // 會員模式: 呼叫後端 API
          if (get()._isAuthenticated()) {
            await cartService.removeCartItem(productId);

            // 重新同步購物車
            await get().syncCart();
            set({ loading: false });
            return { success: true };
          }

          // 訪客模式: 使用 LocalStorage
          set({
            items: get().items.filter((item) => item.productId !== productId),
            loading: false,
          });

          return { success: true };
        } catch (error) {
          const errorMsg = error.response?.data?.message || '移除商品失敗';
          set({ loading: false, error: errorMsg });
          return { success: false, error: errorMsg };
        }
      },

      /**
       * 清空購物車
       */
      clearCart: async () => {
        set({ loading: true, error: null });

        try {
          // 會員模式: 呼叫後端 API
          if (get()._isAuthenticated()) {
            await cartService.clearCart();
            set({ items: [], loading: false });
            return { success: true };
          }

          // 訪客模式: 使用 LocalStorage
          set({ items: [], loading: false });
          return { success: true };
        } catch (error) {
          const errorMsg = error.response?.data?.message || '清空購物車失敗';
          set({ loading: false, error: errorMsg });
          return { success: false, error: errorMsg };
        }
      },

      /**
       * 合併購物車 (登入時使用)
       * 將訪客購物車合併到會員購物車
       */
      mergeCart: async () => {
        if (!get()._isAuthenticated()) {
          return { success: false, error: '未登入' };
        }

        const guestItems = get().items;

        // 如果訪客購物車為空，直接同步會員購物車
        if (guestItems.length === 0) {
          return await get().syncCart();
        }

        set({ loading: true, error: null });
        try {
          // 將訪客購物車項目轉換為後端格式
          const mergeItems = get()._transformToMergeFormat(guestItems);

          // 呼叫合併 API
          const response = await cartService.mergeCart(mergeItems);

          // 更新購物車狀態
          const transformedItems = response.items.map(get()._transformCartItem);
          set({ items: transformedItems, loading: false });

          return { success: true, message: '購物車已合併' };
        } catch (error) {
          const errorMsg = error.response?.data?.message || '合併購物車失敗';
          set({ loading: false, error: errorMsg });
          return { success: false, error: errorMsg };
        }
      },

      /**
       * 清空訪客購物車 (登出時使用)
       */
      clearGuestCart: () => {
        set({ items: [], loading: false, error: null });
      },
    }),
    {
      name: 'cart-storage',
      // 只持久化 items,不持久化 loading 和 error
      partialize: (state) => ({
        items: state.items,
      }),
    }
  )
);

export default useCartStore;
