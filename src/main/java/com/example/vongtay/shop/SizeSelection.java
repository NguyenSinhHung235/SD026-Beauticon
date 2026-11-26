package com.example.vongtay.shop;

import java.math.BigDecimal;
import java.util.Locale;

/** Per-line bracelet sizing. Never rely on browser-provided size mode alone. */
public final class SizeSelection {
    public static final String FIXED = "CO_SAN";
    public static final String FREE = "FREESIZE";
    public static final String CUSTOM = "THEO_YEU_CAU";
    public static final BigDecimal MIN_WRIST = new BigDecimal("10.0");
    public static final BigDecimal MAX_WRIST = new BigDecimal("30.0");

    private SizeSelection() {}

    public record Choice(String mode, BigDecimal wristCm, String fit, String note) {
        public String description(String sizeName) {
            if (CUSTOM.equals(mode)) {
                return "Theo yêu cầu - cổ tay " + wristCm.stripTrailingZeros().toPlainString()
                        + " cm - " + fitText(fit);
            }
            if (FREE.equals(mode)) return "Freesize (điều chỉnh được)";
            return sizeName;
        }
    }

    public static String modeFor(String sizeName) {
        String s = sizeName == null ? "" : sizeName.trim().toLowerCase(Locale.ROOT);
        if (s.equals("freesize") || s.equals("free size")) return FREE;
        if (s.equals("theo yêu cầu") || s.equals("theo yeu cau")) return CUSTOM;
        return FIXED;
    }

    public static String fitText(String code) {
        return switch (code == null ? "" : code) {
            case "OM" -> "đeo ôm";
            case "VUA" -> "đeo vừa";
            case "RONG" -> "đeo rộng";
            default -> "";
        };
    }

    public static Choice validate(String sizeName, String postedMode, String rawWrist,
                                  String rawFit, String rawNote,
                                  BigDecimal freeMin, BigDecimal freeMax) {
        String mode = modeFor(sizeName);
        if (postedMode != null && !postedMode.isBlank() && !mode.equals(postedMode))
            throw new IllegalArgumentException("Kiểu kích thước không phù hợp với phiên bản sản phẩm đã chọn.");
        String note = rawNote == null ? "" : rawNote.trim();
        if (note.length() > 300 || note.chars().anyMatch(c -> c < 32 && c != '\n' && c != '\t'))
            throw new IllegalArgumentException("Ghi chú kích thước không quá 300 ký tự và không chứa ký tự điều khiển.");
        if (FREE.equals(mode)) {
            if (freeMin == null || freeMax == null || freeMin.compareTo(freeMax) >= 0)
                throw new IllegalArgumentException("Sản phẩm Freesize chưa được cửa hàng thiết lập khoảng điều chỉnh. Vui lòng liên hệ shop.");
        }
        String wristText = rawWrist == null ? "" : rawWrist.trim();
        String fit = rawFit == null ? "" : rawFit.trim();
        if (!CUSTOM.equals(mode)) {
            // Ignore stale hidden fields when switching from custom to a normal size.
            return new Choice(mode, null, null, note);
        }
        BigDecimal wrist;
        try {
            wrist = new BigDecimal(wristText.replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Vui lòng nhập chu vi cổ tay theo cm (ví dụ 16,5).");
        }
        if (wrist.scale() > 1 || wrist.compareTo(MIN_WRIST) < 0 || wrist.compareTo(MAX_WRIST) > 0)
            throw new IllegalArgumentException("Chu vi cổ tay phải từ 10 đến 30 cm, tối đa một chữ số thập phân.");
        if (!"OM".equals(fit) && !"VUA".equals(fit) && !"RONG".equals(fit))
            throw new IllegalArgumentException("Vui lòng chọn độ rộng đeo: ôm, vừa hoặc rộng.");
        return new Choice(mode, wrist.setScale(1), fit, note);
    }
}
