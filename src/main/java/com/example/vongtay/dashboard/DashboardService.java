package com.example.vongtay.dashboard;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class DashboardService {
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter FULL_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final NamedParameterJdbcTemplate jdbc;

    public DashboardService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Map<String, Object> load(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null || fromDate.isAfter(toDate)
                || fromDate.isBefore(toDate.minusDays(90))) {
            throw new IllegalArgumentException("Khoảng ngày phải từ 1 đến 91 ngày.");
        }
        LocalDateTime fromUtc = LocalDateTime.ofInstant(
                fromDate.atStartOfDay(VN_ZONE).toInstant(), ZoneOffset.UTC);
        LocalDateTime untilUtc = LocalDateTime.ofInstant(
                toDate.plusDays(1).atStartOfDay(VN_ZONE).toInstant(), ZoneOffset.UTC);

        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("fromUtc", fromUtc)
                .addValue("untilUtc", untilUtc);

        Map<String, Object> orderStats = jdbc.queryForMap("""
                SELECT COUNT_BIG(*) AS totalOrders,
                  COALESCE(SUM(CASE WHEN trang_thai = 'CHUA_XU_LY' THEN 1 ELSE 0 END), 0) AS pending,
                  COALESCE(SUM(CASE WHEN trang_thai = 'DANG_XU_LY' THEN 1 ELSE 0 END), 0) AS processing,
                  COALESCE(SUM(CASE WHEN trang_thai = 'DA_XU_LY' THEN 1 ELSE 0 END), 0) AS processed,
                  COALESCE(SUM(CASE WHEN trang_thai = 'HOAN_THANH' THEN 1 ELSE 0 END), 0) AS completed,
                  COALESCE(SUM(CASE WHEN trang_thai = 'DA_HUY' THEN 1 ELSE 0 END), 0) AS cancelled
                FROM dbo.don_hang WHERE ngay_dat >= :fromUtc AND ngay_dat < :untilUtc
                """, p);

        // Net revenue = successful receipts - successful refunds; grouped by payment date, not order date.
        Map<String, Object> paymentStats = jdbc.queryForMap("""
                SELECT
                   COALESCE(SUM(CASE WHEN t.loai_giao_dich = 'THANH_TOAN'
                         THEN t.so_tien ELSE -t.so_tien END), 0) AS revenue,
                   COALESCE(SUM(CASE WHEN d.kenh_ban = 'ONLINE'
                         THEN CASE WHEN t.loai_giao_dich = 'THANH_TOAN' THEN t.so_tien ELSE -t.so_tien END
                         ELSE 0 END), 0) AS onlineRevenue,
                   COALESCE(SUM(CASE WHEN d.kenh_ban = 'OFFLINE'
                         THEN CASE WHEN t.loai_giao_dich = 'THANH_TOAN' THEN t.so_tien ELSE -t.so_tien END
                         ELSE 0 END), 0) AS offlineRevenue
                FROM dbo.thanh_toan t JOIN dbo.don_hang d ON d.id_don_hang = t.id_don_hang
                WHERE t.trang_thai = 'THANH_CONG'
                  AND t.ngay_thanh_toan >= :fromUtc AND t.ngay_thanh_toan < :untilUtc
                """, p);

        Long lowStock = jdbc.queryForObject("""
                SELECT COUNT_BIG(*) FROM dbo.san_pham_bien_the bt
                JOIN dbo.san_pham sp ON sp.id_san_pham = bt.id_san_pham
                WHERE bt.trang_thai = 1 AND sp.trang_thai = 1 AND bt.so_luong_ton <= 5
                """, new MapSqlParameterSource(), Long.class);
        Long newCustomers = jdbc.queryForObject("""
                SELECT COUNT_BIG(*) FROM dbo.khach_hang
                WHERE ngay_tao >= :fromUtc AND ngay_tao < :untilUtc
                """, p, Long.class);

        List<Map<String, Object>> orders = jdbc.query("""
                SELECT TOP (10) d.id_don_hang, d.ngay_dat, d.kenh_ban, d.trang_thai,
                    d.tong_tien, COALESCE(k.ho_ten, N'Khách vãng lai') AS customer
                FROM dbo.don_hang d
                LEFT JOIN dbo.khach_hang k ON k.id_khach_hang = d.id_khach_hang
                WHERE d.ngay_dat >= :fromUtc AND d.ngay_dat < :untilUtc
                ORDER BY d.ngay_dat DESC, d.id_don_hang DESC
                """, p, (rs, rowNum) -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", rs.getLong("id_don_hang"));
                    m.put("customer", rs.getString("customer"));
                    m.put("channel", rs.getString("kenh_ban"));
                    String status = rs.getString("trang_thai");
                    m.put("status", status);
                    m.put("statusLabel", statusLabel(status));
                    m.put("date", utcDateText(rs, "ngay_dat"));
                    m.put("total", money(rs.getBigDecimal("tong_tien")));
                    return m;
                });

        List<Map<String, Object>> topProducts = jdbc.query("""
                SELECT TOP (5) sp.ten_san_pham AS name,
                       SUM(ct.so_luong) AS quantity,
                       SUM(ct.gia_ban * ct.so_luong) AS gross
                FROM dbo.don_hang_chi_tiet ct
                JOIN dbo.don_hang d ON d.id_don_hang = ct.id_don_hang
                JOIN dbo.san_pham_bien_the bt ON bt.id_bien_the = ct.id_bien_the
                JOIN dbo.san_pham sp ON sp.id_san_pham = bt.id_san_pham
                WHERE d.trang_thai = 'HOAN_THANH'
                    AND d.ngay_dat >= :fromUtc AND d.ngay_dat < :untilUtc
                GROUP BY sp.id_san_pham, sp.ten_san_pham
                ORDER BY quantity DESC, gross DESC
                """, p, (rs, rowNum) -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name", rs.getString("name"));
                    m.put("quantity", rs.getLong("quantity"));
                    m.put("gross", money(rs.getBigDecimal("gross")));
                    return m;
                });

        List<Map<String, Object>> paidDays = jdbc.query("""
                SELECT CAST(t.ngay_thanh_toan AT TIME ZONE 'UTC'
                                AT TIME ZONE 'SE Asia Standard Time' AS DATE) AS saleDay,
                    SUM(CASE WHEN t.loai_giao_dich = 'THANH_TOAN'
                        THEN t.so_tien ELSE -t.so_tien END) AS revenue
                FROM dbo.thanh_toan t
                WHERE t.trang_thai = 'THANH_CONG'
                  AND t.ngay_thanh_toan >= :fromUtc AND t.ngay_thanh_toan < :untilUtc
                GROUP BY CAST(t.ngay_thanh_toan AT TIME ZONE 'UTC'
                                AT TIME ZONE 'SE Asia Standard Time' AS DATE)
                ORDER BY saleDay
                """, p, (rs, rowNum) -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("day", rs.getDate("saleDay").toLocalDate());
                    m.put("revenue", rs.getBigDecimal("revenue"));
                    return m;
                });

        Map<LocalDate, BigDecimal> byDate = new HashMap<>();
        for (Map<String, Object> row : paidDays) {
            byDate.put((LocalDate) row.get("day"), (BigDecimal) row.get("revenue"));
        }
        BigDecimal peak = BigDecimal.ZERO;
        for (LocalDate day = fromDate; !day.isAfter(toDate); day = day.plusDays(1)) {
            BigDecimal amount = byDate.getOrDefault(day, BigDecimal.ZERO);
            if (amount.compareTo(peak) > 0) peak = amount;
        }
        List<Map<String, Object>> chart = new ArrayList<>();
        for (LocalDate day = fromDate; !day.isAfter(toDate); day = day.plusDays(1)) {
            BigDecimal amount = byDate.getOrDefault(day, BigDecimal.ZERO);
            int height = peak.signum() == 0 || amount.signum() <= 0 ? 0
                    : (int) Math.max(4, Math.round(100.0 * amount.doubleValue() / peak.doubleValue()));
            Map<String, Object> bar = new LinkedHashMap<>();
            bar.put("label", day.format(SHORT_DATE));
            bar.put("amount", money(amount));
            bar.put("height", height);
            chart.add(bar);
        }

        Map<String, Object> stats = new LinkedHashMap<>(orderStats);
        stats.put("revenue", money((BigDecimal) paymentStats.get("revenue")));
        stats.put("onlineRevenue", money((BigDecimal) paymentStats.get("onlineRevenue")));
        stats.put("offlineRevenue", money((BigDecimal) paymentStats.get("offlineRevenue")));
        stats.put("lowStock", lowStock == null ? 0 : lowStock);
        stats.put("newCustomers", newCustomers == null ? 0 : newCustomers);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stats", stats);
        result.put("recentOrders", orders);
        result.put("topProducts", topProducts);
        result.put("chart", chart);
        return result;
    }

    private static String money(BigDecimal value) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        formatter.setMaximumFractionDigits(0);
        return formatter.format(value == null ? BigDecimal.ZERO : value) + " ₫";
    }

    private static String utcDateText(ResultSet rs, String column) throws SQLException {
        return rs.getTimestamp(column).toLocalDateTime()
                .atOffset(ZoneOffset.UTC).atZoneSameInstant(VN_ZONE)
                .format(FULL_DATE);
    }

    private static String statusLabel(String status) {
        return switch (status) {
            case "CHUA_XU_LY" -> "Chưa xử lý";
            case "DANG_XU_LY" -> "Đang xử lý";
            case "DA_XU_LY" -> "Đã xử lý";
            case "HOAN_THANH" -> "Hoàn thành";
            case "DA_HUY" -> "Đã hủy";
            default -> status;
        };
    }
}
