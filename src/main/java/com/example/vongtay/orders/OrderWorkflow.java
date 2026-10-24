package com.example.vongtay.orders;

/** The legal workflow for online orders; POS receipts are final and read-only here. */
public final class OrderWorkflow {
    private OrderWorkflow() {}

    public static final String PENDING = "CHUA_XU_LY";
    public static final String PROCESSING = "DANG_XU_LY";
    public static final String PROCESSED = "DA_XU_LY";
    public static final String COMPLETED = "HOAN_THANH";
    public static final String CANCELLED = "DA_HUY";

    public static String next(String status) {
        return switch (status) {
            case PENDING -> PROCESSING;
            case PROCESSING -> PROCESSED;
            case PROCESSED -> COMPLETED;
            default -> null;
        };
    }

    public static boolean mayCancel(String status) {
        return PENDING.equals(status) || PROCESSING.equals(status) || PROCESSED.equals(status);
    }

    public static String label(String status) {
        return switch (status) {
            case PENDING -> "Chưa xử lý";
            case PROCESSING -> "Đang xử lý";
            case PROCESSED -> "Đã xử lý";
            case COMPLETED -> "Hoàn thành";
            case CANCELLED -> "Đã hủy";
            default -> "Không xác định";
        };
    }
}
