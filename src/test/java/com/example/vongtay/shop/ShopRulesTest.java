package com.example.vongtay.shop;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ShopRulesTest {
    @Test void normalizedEmailAndName() {
        assertEquals("my.name@example.com",ShopAuthService.email("  MY.Name@Example.Com "));
        assertEquals("Nguyen Van A",ShopAuthService.fullName(" Nguyen  Van  A "));
        assertThrows(IllegalArgumentException.class,()->ShopAuthService.email("not-an-email"));
    }
    @Test void VietnamesePhoneAndPassword() {
        assertEquals("0912345678",ShopAuthService.phone("0912345678",true));
        assertThrows(IllegalArgumentException.class,()->ShopAuthService.phone("+84912345678",true));
        assertThrows(IllegalArgumentException.class,()->ShopAuthService.password("short"));
        assertDoesNotThrow(()->ShopAuthService.password("secure-very-long-1234"));
    }
    @Test void QuantityAndCheckoutToken() {
        assertDoesNotThrow(()->ShopCartService.validateQuantity(20));
        assertThrows(IllegalArgumentException.class,()->ShopCartService.validateQuantity(0));
        assertThrows(IllegalArgumentException.class,()->ShopCartService.validateQuantity(21));
        String token=UUID.randomUUID().toString();
        assertEquals(token,ShopCheckoutService.canonicalToken(token));
        assertThrows(IllegalArgumentException.class,()->ShopCheckoutService.canonicalToken("bad-token"));
    }
    @Test void ShippingPolicies() {
        assertEquals(new BigDecimal("30000.00"),ShopMoney.shipping(new BigDecimal("499999")));
        assertEquals(BigDecimal.ZERO.setScale(2),ShopMoney.shipping(new BigDecimal("500000")));
    }
}
