package com.example.vongtay.sales;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class SaleSupportTest {
    @Test void discountsAreCappedAndRoundedInVnd() {
        BigDecimal price = new BigDecimal("249000.00");
        assertEquals(new BigDecimal("24900.00"),
                SaleSupport.discount(price, "PHAN_TRAM", new BigDecimal("10"), null));
        assertEquals(new BigDecimal("15000.00"),
                SaleSupport.discount(price, "PHAN_TRAM", new BigDecimal("10"), new BigDecimal("15000")));
        assertEquals(new BigDecimal("249000.00"),
                SaleSupport.discount(price, "CO_DINH", new BigDecimal("999999"), null));
    }
    @Test void onlyBestPromotionApplies() {
        BigDecimal price = new BigDecimal("100000.00");
        BigDecimal net = PromotionService.effectivePrice(price,
                java.util.List.of(new PromotionService.Rule("PHAN_TRAM", new BigDecimal("10")),
                        new PromotionService.Rule("CO_DINH", new BigDecimal("20000"))));
        assertEquals(new BigDecimal("80000.00"), net);
    }
    @Test void vietnameseTimeIsConvertedToUtc() {
        assertEquals(LocalDateTime.of(2026,10,24,4,30),
                SaleSupport.utcFromForm("2026-10-24T11:30"));
    }
    @Test void voucherCodesAreNormalizedAndValidated() {
        assertEquals("WELCOME10", SaleSupport.code(" welcome10 "));
        assertThrows(IllegalArgumentException.class, () -> SaleSupport.code("invalid code"));
    }
}
