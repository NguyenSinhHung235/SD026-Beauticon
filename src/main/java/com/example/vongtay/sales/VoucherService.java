package com.example.vongtay.sales;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Service
public class VoucherService {
    private final NamedParameterJdbcTemplate jdbc;
    public VoucherService(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Map<String, Object>> list() {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT TOP (300) id_ma_giam_gia AS id, code, loai_giam AS type,
                       gia_tri_giam AS value, gia_tri_don_toi_thieu AS minimum,
                       so_luong AS quantity, so_luong_da_dung AS used,
                       ngay_bat_dau AS startAt, ngay_ket_thuc AS endAt,
                       trang_thai AS active
                FROM dbo.ma_giam_gia ORDER BY id_ma_giam_gia DESC
                """, new MapSqlParameterSource());
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        for (Map<String, Object> r : rows) {
            r.put("startText", SaleSupport.toDisplay(r.get("startAt")));
            r.put("endText", SaleSupport.toDisplay(r.get("endAt")));
            LocalDateTime start = ((java.sql.Timestamp)r.get("startAt")).toLocalDateTime();
            LocalDateTime end = ((java.sql.Timestamp)r.get("endAt")).toLocalDateTime();
            r.put("inWindow", !now.isBefore(start) && now.isBefore(end));
        }
        return rows;
    }

    public VoucherForm find(long id) {
        try {
            Map<String, Object> r = jdbc.queryForMap("""
                    SELECT code, mo_ta, loai_giam, gia_tri_giam, gia_tri_don_toi_thieu,
                           giam_toi_da, so_luong, ngay_bat_dau, ngay_ket_thuc,
                           id_nhan_vien, trang_thai
                    FROM dbo.ma_giam_gia WHERE id_ma_giam_gia=:id
                    """, new MapSqlParameterSource("id", id));
            VoucherForm f = new VoucherForm();
            f.setCode((String)r.get("code"));
            f.setMoTa((String)r.get("mo_ta"));
            f.setLoaiGiam((String)r.get("loai_giam"));
            f.setGiaTriGiam((BigDecimal)r.get("gia_tri_giam"));
            f.setGiaTriDonToiThieu((BigDecimal)r.get("gia_tri_don_toi_thieu"));
            f.setGiamToiDa((BigDecimal)r.get("giam_toi_da"));
            f.setSoLuong((Integer)r.get("so_luong"));
            f.setNgayBatDau(SaleSupport.toHtml(r.get("ngay_bat_dau")));
            f.setNgayKetThuc(SaleSupport.toHtml(r.get("ngay_ket_thuc")));
            f.setIdNhanVien((Long)r.get("id_nhan_vien"));
            f.setTrangThai(Boolean.TRUE.equals(r.get("trang_thai")));
            return f;
        } catch (EmptyResultDataAccessException ex) {
            throw new IllegalArgumentException("Voucher khong ton tai.");
        }
    }

    public List<Map<String, Object>> staffOptions() {
        return jdbc.queryForList("""
                SELECT id_nhan_vien AS id, ho_ten AS name, trang_thai AS active
                FROM dbo.nhan_vien ORDER BY ho_ten
                """, new MapSqlParameterSource());
    }

    @Transactional
    public long save(Long id, VoucherForm f) {
        String code = SaleSupport.code(f.getCode());
        String type = SaleSupport.type(f.getLoaiGiam());
        SaleSupport.validateDiscount(type, f.getGiaTriGiam());
        BigDecimal min = SaleSupport.amount(f.getGiaTriDonToiThieu(), "Gia tri don toi thieu", false);
        BigDecimal cap = f.getGiamToiDa() == null ? null
                : SaleSupport.amount(f.getGiamToiDa(), "Giam toi da", true);
        LocalDateTime start = SaleSupport.utcFromForm(f.getNgayBatDau());
        LocalDateTime end = SaleSupport.utcFromForm(f.getNgayKetThuc());
        SaleSupport.validateWindow(start, end);
        if (f.getSoLuong() == null || f.getSoLuong() < 0)
            throw new IllegalArgumentException("So luong voucher phai >= 0.");
        if (f.getMoTa() != null && f.getMoTa().length() > 500)
            throw new IllegalArgumentException("Mo ta vuot 500 ky tu.");
        if (f.getIdNhanVien() != null) {
            Long count = jdbc.queryForObject("""
                    SELECT COUNT_BIG(*) FROM dbo.nhan_vien WHERE id_nhan_vien=:id
                    """, new MapSqlParameterSource("id", f.getIdNhanVien()), Long.class);
            if (count == null || count == 0) throw new IllegalArgumentException("Nhan vien khong ton tai.");
        }
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("id", id).addValue("code", code).addValue("description", f.getMoTa())
                .addValue("type", type).addValue("value", f.getGiaTriGiam())
                .addValue("min", min).addValue("cap", cap).addValue("qty", f.getSoLuong())
                .addValue("start", start).addValue("end", end).addValue("staff", f.getIdNhanVien())
                .addValue("active", f.isTrangThai());
        if (id == null) {
            Long created = jdbc.queryForObject("""
                    INSERT INTO dbo.ma_giam_gia(code,mo_ta,loai_giam,gia_tri_giam,giam_toi_da,
                        gia_tri_don_toi_thieu,so_luong,ngay_bat_dau,ngay_ket_thuc,id_nhan_vien,trang_thai)
                    OUTPUT INSERTED.id_ma_giam_gia
                    VALUES (:code,:description,:type,:value,:cap,:min,:qty,:start,:end,:staff,:active)
                    """, p, Long.class);
            if (created == null) throw new IllegalStateException("Khong tao duoc voucher.");
            return created;
        }
        int updated = jdbc.update("""
                UPDATE dbo.ma_giam_gia SET code=:code,mo_ta=:description,loai_giam=:type,
                    gia_tri_giam=:value,giam_toi_da=:cap,gia_tri_don_toi_thieu=:min,
                    so_luong=:qty,ngay_bat_dau=:start,ngay_ket_thuc=:end,
                    id_nhan_vien=:staff,trang_thai=:active
                WHERE id_ma_giam_gia=:id AND so_luong_da_dung <= :qty
                """, p);
        if (updated != 1) throw new IllegalArgumentException("Voucher khong ton tai hoac so luong moi nho hon so da dung.");
        return id;
    }

    @Transactional
    public void setActive(long id, boolean active) {
        int n = jdbc.update("UPDATE dbo.ma_giam_gia SET trang_thai=:active WHERE id_ma_giam_gia=:id",
                new MapSqlParameterSource("id", id).addValue("active", active));
        if (n != 1) throw new IllegalArgumentException("Voucher khong ton tai.");
    }

    public record Redemption(Long voucherId, BigDecimal discount) {}

    /** Preview does not reserve usage; revalidate in checkout. */
    public Redemption preview(String rawCode, BigDecimal subtotal, LocalDateTime nowUtc) {
        return inspect(rawCode, subtotal, nowUtc, false);
    }

    /** Caller must run in a single @Transactional checkout transaction. */
    public Redemption consume(String rawCode, BigDecimal subtotal, LocalDateTime nowUtc) {
        Redemption verified = inspect(rawCode, subtotal, nowUtc, true);
        if (verified.voucherId() == null) return verified;
        int n = jdbc.update("""
                UPDATE dbo.ma_giam_gia
                SET so_luong_da_dung = so_luong_da_dung+1
                WHERE id_ma_giam_gia=:id AND so_luong_da_dung < so_luong
                """, new MapSqlParameterSource("id", verified.voucherId()));
        if (n != 1) throw new IllegalArgumentException("Voucher da het luot su dung.");
        return verified;
    }

    private Redemption inspect(String rawCode, BigDecimal subtotal, LocalDateTime nowUtc, boolean lock) {
        if (rawCode == null || rawCode.isBlank())
            return new Redemption(null, BigDecimal.ZERO.setScale(2));
        String code = SaleSupport.code(rawCode);
        String sql = """
                SELECT id_ma_giam_gia AS id, loai_giam AS type, gia_tri_giam AS value,
                       gia_tri_don_toi_thieu AS minimum, giam_toi_da AS cap,
                       so_luong AS quantity, so_luong_da_dung AS used,
                       ngay_bat_dau AS startAt, ngay_ket_thuc AS endAt, trang_thai AS active
                FROM dbo.ma_giam_gia %s WHERE code=:code
                """.formatted(lock ? "WITH (UPDLOCK, HOLDLOCK)" : "");
        List<Map<String, Object>> rows = jdbc.queryForList(sql, new MapSqlParameterSource("code", code));
        if (rows.isEmpty()) throw new IllegalArgumentException("Ma voucher khong ton tai.");
        Map<String, Object> v = rows.get(0);
        LocalDateTime start = ((java.sql.Timestamp) v.get("startAt")).toLocalDateTime();
        LocalDateTime end = ((java.sql.Timestamp) v.get("endAt")).toLocalDateTime();
        if (!Boolean.TRUE.equals(v.get("active")) || nowUtc.isBefore(start) || !nowUtc.isBefore(end))
            throw new IllegalArgumentException("Voucher dang tat, chua bat dau hoac da het han.");
        if (((Number)v.get("used")).intValue() >= ((Number)v.get("quantity")).intValue())
            throw new IllegalArgumentException("Voucher da het luot su dung.");
        if (subtotal.compareTo((BigDecimal)v.get("minimum")) < 0)
            throw new IllegalArgumentException("Don hang chua dat gia tri toi thieu cua voucher.");
        BigDecimal discount = SaleSupport.discount(subtotal, (String)v.get("type"),
                (BigDecimal)v.get("value"), (BigDecimal)v.get("cap"));
        return new Redemption(((Number)v.get("id")).longValue(), discount);
    }
}
