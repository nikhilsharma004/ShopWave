package com.shopwave.cart;

import com.shopwave.model.Product;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class Cart {

    private final Map<Long, CartItem> items = new LinkedHashMap<>();

    public void add(Product product) {
        CartItem item = items.get(product.getId());
        if (item == null) {
            items.put(product.getId(), new CartItem(product, 1));
        } else {
            item.increase();
        }
    }

    public void increase(Long productId) {
        CartItem item = items.get(productId);
        if (item != null) {
            item.increase();
        }
    }

    public void decrease(Long productId) {
        CartItem item = items.get(productId);
        if (item == null) {
            return;
        }
        if (item.getQuantity() == 1) {
            items.remove(productId);
        } else {
            item.decrease();
        }
    }

    public void remove(Long productId) {
        items.remove(productId);
    }

    public void clear() {
        items.clear();
    }

    public Collection<CartItem> getItems() {
        return items.values();
    }

    public int getItemCount() {
        return items.values().stream().mapToInt(CartItem::getQuantity).sum();
    }

    public BigDecimal getTotal() {
        return items.values().stream()
            .map(CartItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
