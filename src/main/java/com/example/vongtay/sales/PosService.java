package com.example.vongtay.sales;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Service
public class PosService {
    private final NamedParameterJdbcTemplate jdbc;
    private final PromotionService promotions;
    private final VoucherService vouchers;

    public PosService(NamedParameterJdbcTemplate jdbc, PromotionService promotions, VoucherService vouchers) {
        this.jdbc = jdbc;
        this.promotions = promotions;
        this.vouchers = vouchers;
    }

    public List<Map<String, Object>> variants() {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT TOP (300) v.id_bien_the AS id, v.gia_ban AS original,
                       v.so_luong_ton AS stock, s.ten_san_pham AS name,
                       m.ten_mau_sac AS color, k.ten_kich_thuoc AS size
                FROM dbo.san_pham_bien_the v
                JOIN dbo.san_pham s ON s.id_san_pham=v.id_san_pham
                JOIN dbo.bien_the_mau_sac m ON m.id_mau_sac=v.id_mau_sac
                JOIN dbo.bien_the_kich_thuoc k ON k.id_kich_thuoc=v.id_kich_thuoc
                WHERE v.trang_thai=1 AND s.trang_thai=1 AND v.so_luong_ton > 0
                ORDER BY s.ten_san_pham, v.id_bien_the
                """, new MapSqlParameterSource());
        List<Long> ids = rows.stream().map(r -> ((Number)r.get("id")).longValue()).toList();
        Map<Long, List<PromotionService.Rule>> active = promotions.activeRules(ids, LocalDateTime.now(ZoneOffset.UTC));
        for (Map<String, Object> r : rows) {
            Long id = ((Number)r.get("id")).longValue();
            r.put("price", PromotionService.effectivePrice((BigDecimal)r.get("original"), active.get(id)));
            r.put("label", r.get("name") + " / " + r.get("color") + " / " + r.get("size"));
        }
        return rows;
    }

    public List<Map<String, Object>> customers() {
        return jdbc.queryForList("""
                SELECT TOP (300) id_khach_hang AS id, ho_ten AS name, sdt AS phone
                FROM dbo.khach_hang WHERE trang_thai=1 ORDER BY ho_ten
                """, new MapSqlParameterSource());
    }

    public List<Map<String, Object>> staff() {
        return jdbc.queryForList("""
                SELECT id_nhan_vien AS id, ho_ten AS name
                FROM dbo.nhan_vien WHERE trang_thai=1 ORDER BY ho_ten
                """, new MapSqlParameterSource());
    }

    private TreeMap<Long, Integer> normalizeItems(List<PosForm.PosItem> items) {
        if (items == null || items.isEmpty() || items.size() > 30)
            throw new IllegalArgumentException("Moi hoa don phai co 1-30 dong san pham.");
        TreeMap<Long, Integer> combined = new TreeMap<>();
        for (PosForm.PosItem item : items) {
            if (item == null || item.getVariantId() == null || item.getVariantId() <= 0
                    || item.getQuantity() == null || item.getQuantity() < 1 || item.getQuantity() > 1000)
                throw new IllegalArgumentException("Bien the hoac so luong khong hop le.");
            combined.merge(item.getVariantId(), item.getQuantity(), (a, b) -> Math.addExact(a, b));
        }
        if (combined.values().stream().anyMatch(q -> q > 1000))
            throw new IllegalArgumentException("Toi da 1000 san pham mot bien the.");
        return combined;
    }

    private record Line(long variantId, int quantity, BigDecimal original, BigDecimal actual) {}

    /** Read-only estimate. The checkout recalculates everything in a transaction. */
    public Map<String, Object> quote(PosForm f) {
        TreeMap<Long, Integer> items = normalizeItems(f.getItems());
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
        Map<Long, List<PromotionService.Rule>> active = promotions.activeRules(items.keySet(), nowUtc);
        BigDecimal subtotal = BigDecimal.ZERO.setScale(2);
        for (Map.Entry<Long, Integer> item : items.entrySet()) {
            List<Map<String, Object>> matches = jdbc.queryForList("""
                    SELECT b.gia_ban AS price, b.so_luong_ton AS stock,
                           b.trang_thai AS active, s.trang_thai AS productActive
                    FROM dbo.san_pham_bien_the b
                    JOIN dbo.san_pham s ON s.id_san_pham=b.id_san_pham
                    WHERE b.id_bien_the=:id
                    """, new MapSqlParameterSource("id", item.getKey()));
            if (matches.isEmpty()) throw new IllegalArgumentException("Bien the khong ton tai.");
            Map<String, Object> row = matches.get(0);
            if (!Boolean.TRUE.equals(row.get("active")) || !Boolean.TRUE.equals(row.get("productActive")))
                throw new IllegalArgumentException("Bien the dang ngung ban.");
            if (((Number)row.get("stock")).intValue() < item.getValue())
                throw new IllegalArgumentException("Bien the #" + item.getKey() + " khong du ton kho.");
            BigDecimal price = PromotionService.effectivePrice((BigDecimal)row.get("price"), active.get(item.getKey()));
            subtotal = subtotal.add(price.multiply(BigDecimal.valueOf(item.getValue())));
        }
        if (subtotal.signum() <= 0) throw new IllegalArgumentException("Don hang 0 dong chua duoc ho tro.");
        VoucherService.Redemption voucher = vouchers.preview(f.getVoucherCode(), subtotal, nowUtc);
        BigDecimal total = subtotal.subtract(voucher.discount());
        if (total.signum() <= 0) throw new IllegalArgumentException("Voucher khong the lam tong tien thanh 0 dong.");
        return Map.of("subtotal", subtotal, "voucherDiscount", voucher.discount(), "total", total);
    }

    @Transactional
    public long checkout(PosForm f) {
        if (f == null || f.getRequestToken() == null)
            throw new IllegalArgumentException("Thieu ma phien ban hang.");
        final String token;
        try { token = UUID.fromString(f.getRequestToken()).toString(); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("Ma phien ban hang khong hop le."); }
        List<Long> old = jdbc.queryForList("""
                SELECT id_don_hang FROM dbo.don_hang WHERE ma_yeu_cau_pos=:token AND kenh_ban='OFFLINE'
                """, new MapSqlParameterSource("token", token), Long.class);
        if (!old.isEmpty()) return old.get(0); // Safe retry, do not decrease stock again.

        if (f.getStaffId() == null || !existsActive("nhan_vien", "id_nhan_vien", f.getStaffId()))
            throw new IllegalArgumentException("Hay chon nhan vien dang hoat dong.");
        if (f.getCustomerId() != null && !existsActive("khach_hang", "id_khach_hang", f.getCustomerId()))
            throw new IllegalArgumentException("Khach hang khong ton tai hoac da an.");
        String payment = f.getPaymentMethod();
        if (!"TIEN_MAT".equals(payment) && !"CHUYEN_KHOAN".equals(payment))
            throw new IllegalArgumentException("Chi ho tro tien mat va chuyen khoan tai quay.");

        TreeMap<Long, Integer> items = normalizeItems(f.getItems());
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
        Map<Long, List<PromotionService.Rule>> rules = promotions.activeRules(items.keySet(), nowUtc);
        List<Line> lines = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO.setScale(2);
        for (Map.Entry<Long, Integer> entry : items.entrySet()) {
            Long id = entry.getKey();
            List<Map<String, Object>> matches = jdbc.queryForList("""
                    SELECT b.gia_ban AS price, b.so_luong_ton AS stock,
                           b.trang_thai AS active, s.trang_thai AS productActive
                    FROM dbo.san_pham_bien_the b WITH (UPDLOCK, ROWLOCK)
                    JOIN dbo.san_pham s ON s.id_san_pham=b.id_san_pham
                    WHERE b.id_bien_the=:id
                    """, new MapSqlParameterSource("id", id));
            if (matches.isEmpty()) throw new IllegalArgumentException("Bien the #" + id + " khong ton tai.");
            Map<String, Object> r = matches.get(0);
            if (!Boolean.TRUE.equals(r.get("active")) || !Boolean.TRUE.equals(r.get("productActive")))
                throw new IllegalArgumentException("Bien the #" + id + " da ngung ban.");
            if (((Number)r.get("stock")).intValue() < entry.getValue())
                throw new IllegalArgumentException("Ton kho cua bien the #" + id + " khong du.");
            BigDecimal original = (BigDecimal)r.get("price");
            BigDecimal effective = PromotionService.effectivePrice(original, rules.get(id));
            lines.add(new Line(id, entry.getValue(), original, effective));
            subtotal = subtotal.add(effective.multiply(BigDecimal.valueOf(entry.getValue())));
        }
        if (subtotal.signum() <= 0) throw new IllegalArgumentException("Don hang 0 dong chua duoc ho tro tai POS.");

        VoucherService.Redemption voucher = vouchers.consume(f.getVoucherCode(), subtotal, nowUtc);
        BigDecimal total = subtotal.subtract(voucher.discount()).setScale(2, RoundingMode.HALF_UP);
        if (total.signum() <= 0) throw new IllegalArgumentException("Voucher khong the lam tong tien thanh 0 dong.");
        BigDecimal given = null;
        BigDecimal change = null;
        if ("TIEN_MAT".equals(payment)) {
            given = SaleSupport.amount(f.getCashReceived(), "Tien khach dua", false);
            if (given.compareTo(total) < 0) throw new IllegalArgumentException("Tien khach dua nho hon tien can thanh toan.");
            change = given.subtract(total);
        } else if (!f.isConfirmedTransfer()) {
            throw new IllegalArgumentException("Chi xac nhan chuyen khoan sau khi da kiem tra tien vao tai khoan.");
        }

        Long orderId = jdbc.queryForObject("""
                INSERT INTO dbo.don_hang (id_khach_hang,id_ma_giam_gia,id_nhan_vien,
                    kenh_ban,trang_thai,tien_hang,tien_giam,phi_van_chuyen,
                    ma_yeu_cau_pos,tien_khach_dua,tien_thoi_lai,ton_kho_da_tru,voucher_da_su_dung,ngay_hoan_thanh)
                OUTPUT INSERTED.id_don_hang
                VALUES (:customer,:voucher,:staff,'OFFLINE','HOAN_THANH',:subtotal,:discount,0,
                        :token,:given,:change,1,:voucherUsed,SYSUTCDATETIME())
                """, new MapSqlParameterSource()
                .addValue("customer", f.getCustomerId()).addValue("voucher", voucher.voucherId())
                .addValue("staff", f.getStaffId()).addValue("subtotal", subtotal)
                .addValue("discount", voucher.discount()).addValue("token", token)
                .addValue("given", given).addValue("change", change)
                .addValue("voucherUsed", voucher.voucherId() != null), Long.class);
        if (orderId == null) throw new IllegalStateException("Khong tao duoc hoa don.");

        for (Line line : lines) {
            int updated = jdbc.update("""
                    UPDATE dbo.san_pham_bien_the
                    SET so_luong_ton=so_luong_ton-:quantity
                    WHERE id_bien_the=:id AND so_luong_ton>=:quantity AND trang_thai=1
                    """, new MapSqlParameterSource("quantity", line.quantity()).addValue("id", line.variantId()));
            if (updated != 1) throw new IllegalArgumentException("Ton kho vua thay doi. Hay kiem tra lai.");
            jdbc.update("""
                    INSERT INTO dbo.don_hang_chi_tiet(id_don_hang,id_bien_the,so_luong,gia_ban)
                    VALUES(:orderId,:variantId,:quantity,:price)
                    """, new MapSqlParameterSource("orderId", orderId)
                    .addValue("variantId", line.variantId()).addValue("quantity", line.quantity())
                    .addValue("price", line.actual()));
        }
        jdbc.update("""
                INSERT INTO dbo.thanh_toan(id_don_hang,phuong_thuc,loai_giao_dich,so_tien,
                                           trang_thai,ma_giao_dich,ngay_thanh_toan)
                VALUES(:orderId,:method,'THANH_TOAN',:total,'THANH_CONG',:transaction,SYSUTCDATETIME())
                """, new MapSqlParameterSource("orderId", orderId)
                .addValue("method", payment).addValue("total", total)
                .addValue("transaction", "POS-" + orderId));
        return orderId;
    }

    private boolean existsActive(String table, String key, Long id) {
        // Hardcoded internal arguments only, not user-controlled identifiers.
        Long count = jdbc.queryForObject("SELECT COUNT_BIG(*) FROM dbo." + table + " WHERE " + key + "=:id AND trang_thai=1",
                new MapSqlParameterSource("id", id), Long.class);
        return count != null && count > 0;
    }

    public Map<String, Object> receipt(long id) {
        Map<String, Object> order;
        try {
            order = jdbc.queryForMap("""
                    SELECT d.id_don_hang AS id, d.ngay_dat AS orderAt,
                        d.tien_hang AS subtotal, d.tien_giam AS discount, d.tong_tien AS total,
                        d.tien_khach_dua AS given, d.tien_thoi_lai AS changeAmount,
                        v.code AS voucher, n.ho_ten AS staffName,
                        COALESCE(k.ho_ten,N'Khach vang lai') AS customerName,
                        t.phuong_thuc AS paymentMethod
                    FROM dbo.don_hang d
                    JOIN dbo.thanh_toan t ON t.id_don_hang=d.id_don_hang
                      AND t.trang_thai='THANH_CONG' AND t.loai_giao_dich='THANH_TOAN'
                    LEFT JOIN dbo.khach_hang k ON k.id_khach_hang=d.id_khach_hang
                    LEFT JOIN dbo.nhan_vien n ON n.id_nhan_vien=d.id_nhan_vien
                    LEFT JOIN dbo.ma_giam_gia v ON v.id_ma_giam_gia=d.id_ma_giam_gia
                    WHERE d.id_don_hang=:id AND d.kenh_ban='OFFLINE' AND d.trang_thai='HOAN_THANH'
                    """, new MapSqlParameterSource("id", id));
        } catch (EmptyResultDataAccessException ex) {
            throw new IllegalArgumentException("Khong tim thay hoa don tai quay da thanh toan.");
        }
        order.put("orderText", SaleSupport.toDisplay(order.get("orderAt")));
        order.put("items", jdbc.queryForList("""
                SELECT s.ten_san_pham AS name, m.ten_mau_sac AS color,
                    k.ten_kich_thuoc AS size, c.so_luong AS quantity,
                    c.gia_ban AS price,
                    c.so_luong * c.gia_ban AS lineTotal
                FROM dbo.don_hang_chi_tiet c
                JOIN dbo.san_pham_bien_the b ON b.id_bien_the=c.id_bien_the
                JOIN dbo.san_pham s ON s.id_san_pham=b.id_san_pham
                JOIN dbo.bien_the_mau_sac m ON m.id_mau_sac=b.id_mau_sac
                JOIN dbo.bien_the_kich_thuoc k ON k.id_kich_thuoc=b.id_kich_thuoc
                WHERE c.id_don_hang=:id ORDER BY c.id_dh_chi_tiet
                """, new MapSqlParameterSource("id", id)));
        return order;
    }
}
