package com.example.vongtay.shop;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class CustomCmPricing {
    public static final BigDecimal STANDARD_CM = new BigDecimal("16.0");

    private CustomCmPricing() {}

    public static BigDecimal gramPerCm(BigDecimal gramMoiCm, BigDecimal baseWeightGram) {
        if (gramMoiCm != null && gramMoiCm.signum() > 0) {
            return gramMoiCm;
        }
        if (baseWeightGram != null && baseWeightGram.signum() > 0) {
            return baseWeightGram.divide(STANDARD_CM, 6, RoundingMode.HALF_UP);
        }
        return BigDecimal.ONE;
    }

    public static BigDecimal estimateGram(BigDecimal gramMoiCm, BigDecimal baseWeightGram, BigDecimal wristCm) {
        if (wristCm == null || wristCm.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal ratePerCm = gramPerCm(gramMoiCm, baseWeightGram);
        return wristCm.multiply(ratePerCm).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal resolvePrice(String mode, BigDecimal wristCm,
                                          BigDecimal gramMoiCm, BigDecimal baseWeightGram,
                                          BigDecimal materialRate, BigDecimal craftCost,
                                          BigDecimal surcharge, BigDecimal variantPrice) {
        BigDecimal fallback = variantPrice != null ? variantPrice : BigDecimal.ZERO;
        if (!SizeSelection.CUSTOM.equals(mode) || wristCm == null || wristCm.signum() <= 0) {
            return fallback;
        }
        BigDecimal rate = materialRate != null ? materialRate : BigDecimal.ZERO;
        BigDecimal craft = craftCost != null ? craftCost : BigDecimal.ZERO;
        BigDecimal addon = surcharge != null ? surcharge : BigDecimal.ZERO;
        if (rate.signum() > 0) {
            BigDecimal estGram = estimateGram(gramMoiCm, baseWeightGram, wristCm);
            return estGram.multiply(rate).add(craft).add(addon).setScale(0, RoundingMode.HALF_UP);
        }
        return fallback.add(addon).setScale(0, RoundingMode.HALF_UP);
    }
}