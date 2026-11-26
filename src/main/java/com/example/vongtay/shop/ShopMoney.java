package com.example.vongtay.shop;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public final class ShopMoney {
    private ShopMoney() {}
    public static final BigDecimal SHIPPING = new BigDecimal("30000.00");
    public static final BigDecimal FREE_SHIPPING_FROM = new BigDecimal("500000.00");
    public static BigDecimal shipping(BigDecimal items) {
        return items.compareTo(FREE_SHIPPING_FROM) >= 0
                ? BigDecimal.ZERO.setScale(2) : SHIPPING;
    }
    public static String format(BigDecimal amount) {
        if (amount == null) return "-";
        NumberFormat f = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));
        return f.format(amount) + " \u20ab";
    }
}
