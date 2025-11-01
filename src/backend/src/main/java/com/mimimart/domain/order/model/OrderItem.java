package com.mimimart.domain.order.model;

import java.util.Objects;

/**
 * 訂單項目值對象(不可變)
 * 包含商品快照資訊,不受後續商品變動影響
 */
public class OrderItem {
    private final Long productId;
    private final ProductSnapshot snapshot;
    private final int quantity;
    private final Money subtotal;

    private OrderItem(Long productId, ProductSnapshot snapshot, int quantity) {
        this.productId = productId;
        this.snapshot = snapshot;
        this.quantity = quantity;
        this.subtotal = snapshot.getPrice().multiply(quantity);
    }

    /**
     * 建立訂單項目
     *
     * @param productId 商品 ID
     * @param snapshot  商品快照
     * @param quantity  購買數量
     * @return OrderItem 實例
     */
    public static OrderItem of(Long productId, ProductSnapshot snapshot, int quantity) {
        if (productId == null) {
            throw new IllegalArgumentException("商品 ID 不可為 null");
        }
        if (snapshot == null) {
            throw new IllegalArgumentException("商品快照不可為 null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("購買數量必須大於 0");
        }
        return new OrderItem(productId, snapshot, quantity);
    }

    // Getters
    public Long getProductId() {
        return productId;
    }

    public ProductSnapshot getSnapshot() {
        return snapshot;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money getSubtotal() {
        return subtotal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return quantity == orderItem.quantity &&
               Objects.equals(productId, orderItem.productId) &&
               Objects.equals(snapshot, orderItem.snapshot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, snapshot, quantity);
    }

    /**
     * 商品快照值對象
     * 記錄訂單建立時的商品資訊
     */
    public static class ProductSnapshot {
        private final String productName;
        private final Money price;
        private final Money originalPrice;
        private final String productImage;

        private ProductSnapshot(Builder builder) {
            this.productName = builder.productName;
            this.price = builder.price;
            this.originalPrice = builder.originalPrice;
            this.productImage = builder.productImage;
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public String getProductName() {
            return productName;
        }

        public Money getPrice() {
            return price;
        }

        public Money getOriginalPrice() {
            return originalPrice;
        }

        public String getProductImage() {
            return productImage;
        }

        /**
         * 是否有折扣
         */
        public boolean hasDiscount() {
            return price.isLessThan(originalPrice);
        }

        /**
         * 計算節省金額
         */
        public Money getSavings() {
            return hasDiscount() ? originalPrice.subtract(price) : Money.zero();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProductSnapshot that = (ProductSnapshot) o;
            return Objects.equals(productName, that.productName) &&
                   Objects.equals(price, that.price) &&
                   Objects.equals(originalPrice, that.originalPrice) &&
                   Objects.equals(productImage, that.productImage);
        }

        @Override
        public int hashCode() {
            return Objects.hash(productName, price, originalPrice, productImage);
        }

        public static class Builder {
            private String productName;
            private Money price;
            private Money originalPrice;
            private String productImage;

            public Builder productName(String productName) {
                this.productName = productName;
                return this;
            }

            public Builder price(Money price) {
                this.price = price;
                return this;
            }

            public Builder originalPrice(Money originalPrice) {
                this.originalPrice = originalPrice;
                return this;
            }

            public Builder productImage(String productImage) {
                this.productImage = productImage;
                return this;
            }

            public ProductSnapshot build() {
                validate();
                return new ProductSnapshot(this);
            }

            private void validate() {
                if (productName == null || productName.isBlank()) {
                    throw new IllegalArgumentException("商品名稱不可為空");
                }
                if (price == null) {
                    throw new IllegalArgumentException("商品價格不可為 null");
                }
                if (originalPrice == null) {
                    throw new IllegalArgumentException("商品原價不可為 null");
                }
                if (price.isGreaterThan(originalPrice)) {
                    throw new IllegalArgumentException("商品價格不可高於原價");
                }
            }
        }
    }
}
