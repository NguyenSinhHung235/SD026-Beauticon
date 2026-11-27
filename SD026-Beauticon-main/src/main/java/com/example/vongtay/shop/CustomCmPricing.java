package com.example.vongtay.shop;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CustomCmPricing {
    public static final BigDecimal STANDARD_CM = new BigDecimal("16.0");
    private static final Pattern CM_PATTERN = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*cm", Pattern.CASE_INSENSITIVE);

    private CustomCmPricing() {}

    public static BigDecimal fitAllowanceCm(String fit) {
        if (fit == null) return BigDecimal.ZERO;
        return switch (fit.trim().toUpperCase()) {
            case "VUA" -> new BigDecimal("1.0");
            case "RONG" -> new BigDecimal("2.0");
            default -> BigDecimal.ZERO; // OM hoặc không chọn
        };
    }

    public static BigDecimal parseSizeCm(String sizeName) {
        if (sizeName == null || sizeName.isBlank()) return null;
        Matcher m = CM_PATTERN.matcher(sizeName);
        if (!m.find()) return null;
        try {
            return new BigDecimal(m.group(1).replace(',', '.'));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static BigDecimal gramPerCm(BigDecimal gramMoiCm, BigDecimal baseWeightGram) {
        if (gramMoiCm != null && gramMoiCm.signum() > 0) {
            return gramMoiCm;
        }
        if (baseWeightGram != null && baseWeightGram.signum() > 0) {
            return baseWeightGram.divide(STANDARD_CM, 6, RoundingMode.HALF_UP);
        }
        return BigDecimal.ONE;
    }

    public static BigDecimal effectiveLengthCm(BigDecimal wristCm, String fit) {
        if (wristCm == null || wristCm.signum() <= 0) return BigDecimal.ZERO;
        return wristCm.add(fitAllowanceCm(fit));
    }

    public static BigDecimal estimateGram(BigDecimal gramMoiCm, BigDecimal baseWeightGram,
                                          BigDecimal wristCm, String fit) {
        BigDecimal totalCm = effectiveLengthCm(wristCm, fit);
        if (totalCm.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal ratePerCm = gramPerCm(gramMoiCm, baseWeightGram);
        return totalCm.multiply(ratePerCm).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal estimateGram(BigDecimal gramMoiCm, BigDecimal baseWeightGram, BigDecimal wristCm) {
        return estimateGram(gramMoiCm, baseWeightGram, wristCm, null);
    }

    public static BigDecimal calculateByLength(BigDecimal lengthCm,
                                               BigDecimal gramMoiCm, BigDecimal baseWeightGram,
                                               BigDecimal materialRate, BigDecimal craftCost,
                                               BigDecimal surcharge, BigDecimal fallbackPrice) {
        BigDecimal fallback = fallbackPrice != null ? fallbackPrice : BigDecimal.ZERO;
        BigDecimal rate = materialRate != null ? materialRate : BigDecimal.ZERO;
        BigDecimal craft = craftCost != null ? craftCost : BigDecimal.ZERO;
        BigDecimal addon = surcharge != null ? surcharge : BigDecimal.ZERO;

        if (lengthCm != null && lengthCm.signum() > 0 && rate.signum() > 0) {
            BigDecimal estGram = lengthCm.multiply(gramPerCm(gramMoiCm, baseWeightGram))
                    .setScale(2, RoundingMode.HALF_UP);
            return estGram.multiply(rate).add(craft).add(addon).setScale(0, RoundingMode.HALF_UP);
        }
        return fallback.add(addon).setScale(0, RoundingMode.HALF_UP);
    }

    public static BigDecimal resolvePrice(String mode, BigDecimal wristCm, String fit,
                                          String sizeName,
                                          BigDecimal gramMoiCm, BigDecimal baseWeightGram,
                                          BigDecimal materialRate, BigDecimal craftCost,
                                          BigDecimal surcharge, BigDecimal variantPrice) {
        BigDecimal fallback = variantPrice != null ? variantPrice : BigDecimal.ZERO;
        BigDecimal rate = materialRate != null ? materialRate : BigDecimal.ZERO;
        BigDecimal craft = craftCost != null ? craftCost : BigDecimal.ZERO;
        BigDecimal addon = surcharge != null ? surcharge : BigDecimal.ZERO;

        if (SizeSelection.CUSTOM.equals(mode) && wristCm != null && wristCm.signum() > 0) {
            BigDecimal totalCm = effectiveLengthCm(wristCm, fit);
            return calculateByLength(totalCm, gramMoiCm, baseWeightGram, rate, craft, addon, fallback);
        }

        // Với size cố định (ví dụ 16 cm), nếu sản phẩm cấu hình giá theo gram chất liệu thì tính đồng bộ theo cm
        BigDecimal fixedCm = parseSizeCm(sizeName);
        if (fixedCm != null && fixedCm.signum() > 0 && rate.signum() > 0) {
            return calculateByLength(fixedCm, gramMoiCm, baseWeightGram, rate, craft, addon, fallback);
        }

        return fallback;
    }

    public static BigDecimal resolvePrice(String mode, BigDecimal wristCm,
                                          BigDecimal gramMoiCm, BigDecimal baseWeightGram,
                                          BigDecimal materialRate, BigDecimal craftCost,
                                          BigDecimal surcharge, BigDecimal variantPrice) {
        return resolvePrice(mode, wristCm, null, null, gramMoiCm, baseWeightGram,
                materialRate, craftCost, surcharge, variantPrice);
    }
}