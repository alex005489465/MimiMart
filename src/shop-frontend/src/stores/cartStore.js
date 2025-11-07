/**
 * 購物車狀態管理 Store
 */
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

const useCartStore = create(
  persist(
    (set, get) => ({
      // 狀態
      items: [], // { productId, name, price, quantity, image, stock }

      // 取得購物車項目總數
      getTotalItems: () => {
        return get().items.reduce((total, item) => total + item.quantity, 0);
      },

      // 取得購物車總金額
      getTotalPrice: () => {
        return get().items.reduce(
          (total, item) => total + item.price * item.quantity,
          0
        );
      },

      // 加入商品到購物車
      addItem: (product) => {
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
            return {
              success: false,
              error: '商品數量超過庫存限制',
            };
          }

          newItems[existingItemIndex].quantity = newQuantity;
          set({ items: newItems });
        } else {
          // 新增商品
          set({
            items: [
              ...items,
              {
                productId: product.productId,
                name: product.name,
                price: product.price,
                quantity: product.quantity || 1,
                image: product.image,
                stock: product.stock,
              },
            ],
          });
        }

        return { success: true };
      },

      // 移除商品
      removeItem: (productId) => {
        set({
          items: get().items.filter((item) => item.productId !== productId),
        });
      },

      // 更新商品數量
      updateQuantity: (productId, quantity) => {
        const items = get().items;
        const itemIndex = items.findIndex(
          (item) => item.productId === productId
        );

        if (itemIndex === -1) {
          return { success: false, error: '商品不存在' };
        }

        const item = items[itemIndex];

        // 檢查庫存
        if (quantity > item.stock) {
          return {
            success: false,
            error: '商品數量超過庫存限制',
          };
        }

        // 如果數量為 0，移除商品
        if (quantity <= 0) {
          get().removeItem(productId);
          return { success: true };
        }

        const newItems = [...items];
        newItems[itemIndex].quantity = quantity;
        set({ items: newItems });

        return { success: true };
      },

      // 清空購物車
      clearCart: () => {
        set({ items: [] });
      },

      // 檢查商品是否在購物車中
      isInCart: (productId) => {
        return get().items.some((item) => item.productId === productId);
      },

      // 取得特定商品的數量
      getItemQuantity: (productId) => {
        const item = get().items.find((item) => item.productId === productId);
        return item ? item.quantity : 0;
      },
    }),
    {
      name: 'cart-storage',
    }
  )
);

export default useCartStore;
