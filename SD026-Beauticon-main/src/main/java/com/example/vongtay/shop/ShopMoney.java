// src/main/java/com/example/vongtay/shop/ShopMoney.java
package com.example.vongtay.shop;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public final class ShopMoney {
    private ShopMoney() {}
    public static final BigDecimal SHIPPING = new BigDecimal("30000.00");
    public static final BigDecimal FREE_SHIPPING_FROM = new BigDecimal("500000.00");
    public static final BigDecimal EXPRESS_SHIPPING = new BigDecimal("45000.00");

    public static String normalizeShippingMethod(String raw) {
        if (raw == null || raw.isBlank()) {
            return "GIAO_TIEU_CHUAN";
        }
        String code = raw.trim().toUpperCase(Locale.ROOT);
        return switch (code) {
            case "GIAO_TIEU_CHUAN", "GIAO_HOA_TOC", "NHAN_TAI_CUA_HANG" -> code;
            default -> throw new IllegalArgumentException("Phương thức giao hàng không hợp lệ.");
        };
    }

    public static BigDecimal shipping(BigDecimal items) {
        return shipping(items, "GIAO_TIEU_CHUAN");
    }

    public static BigDecimal shipping(BigDecimal items, String shippingMethod) {
        String method = normalizeShippingMethod(shippingMethod);
        BigDecimal baseItems = items == null ? BigDecimal.ZERO.setScale(2) : items;
        return switch (method) {
            case "NHAN_TAI_CUA_HANG" -> BigDecimal.ZERO.setScale(2);
            case "GIAO_HOA_TOC" -> EXPRESS_SHIPPING;
            default -> baseItems.compareTo(FREE_SHIPPING_FROM) >= 0
                    ? BigDecimal.ZERO.setScale(2) : SHIPPING;
        };
    }

    public static String format(BigDecimal amount) {
        if (amount == null) return "-";
        NumberFormat f = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));
        return f.format(amount) + " \u20ab";
    }
}