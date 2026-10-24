package com.example.vongtay.sales;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class SaleSupport {
    private SaleSupport() {}
    public static final ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter HTML = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final BigDecimal HUNDRED = new BigDecimal("100");

    public static LocalDateTime utcFromForm(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Ngay gio khong duoc de trong.");
        try {
            return LocalDateTime.ofInstant(LocalDateTime.parse(value.trim()).atZone(VN).toInstant(), ZoneOffset.UTC);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Ngay gio khong hop le (mau: yyyy-MM-ddTHH:mm).");
        }
    }
    public static String toHtml(Object utc) {
        if (utc == null) return "";
        return utcLocal(utc).format(HTML);
    }
    public static String toDisplay(Object utc) {
        if (utc == null) return "";
        return utcLocal(utc).format(DISPLAY);
    }
    private static LocalDateTime utcLocal(Object utc) {
        LocalDateTime dt = utc instanceof Timestamp t ? t.toLocalDateTime() : (LocalDateTime) utc;
        return LocalDateTime.ofInstant(dt.toInstant(ZoneOffset.UTC), VN);
    }
    public static void validateWindow(LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) throw new IllegalArgumentException("Ngay ket thuc phai sau ngay bat dau.");
    }
    public static String type(String value) {
        if (value == null || (!value.equals("PHAN_TRAM") && !value.equals("CO_DINH")))
            throw new IllegalArgumentException("Loai giam gia khong hop le.");
        return value;
    }
    public static BigDecimal amount(BigDecimal value, String label, boolean positive) {
        if (value == null || (positive && value.signum() <= 0) || (!positive && value.signum() < 0))
            throw new IllegalArgumentException(label + " khong hop le.");
        if (value.scale() > 2 || value.precision() - value.scale() > 16)
            throw new IllegalArgumentException(label + " vuot do chinh xac DECIMAL(18,2).");
        BigDecimal normalized = value.setScale(2, RoundingMode.UNNECESSARY);
        if (normalized.precision() > 18)
            throw new IllegalArgumentException(label + " vuot do chinh xac DECIMAL(18,2).");
        return normalized;
    }
    public static void validateDiscount(String type, BigDecimal value) {
        type(type);
        amount(value, "Gia tri giam", true);
        if ("PHAN_TRAM".equals(type) && value.compareTo(HUNDRED) > 0)
            throw new IllegalArgumentException("Phan tram giam khong duoc vuot 100.");
    }
    public static BigDecimal discount(BigDecimal base, String type, BigDecimal value, BigDecimal max) {
        BigDecimal d = "PHAN_TRAM".equals(type)
                ? base.multiply(value).divide(HUNDRED, 0, RoundingMode.HALF_UP)
                : value;
        if (max != null) d = d.min(max);
        return d.min(base).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }
    public static String code(String code) {
        String result = code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
        if (!result.matches("[A-Z0-9_-]{3,40}"))
            throw new IllegalArgumentException("Ma voucher chi gom A-Z, 0-9, -, _, dai 3-40 ky tu.");
        return result;
    }
}
