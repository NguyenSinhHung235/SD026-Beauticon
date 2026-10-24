package com.example.vongtay.sales;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PromotionService {
    private final NamedParameterJdbcTemplate jdbc;

    public PromotionService(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Map<String, Object>> list() {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT TOP (300) k.id_khuyen_mai AS id, k.ten_khuyen_mai AS name,
                    k.loai_giam AS type, k.gia_tri_giam AS value,
                    k.ngay_bat_dau AS startAt, k.ngay_ket_thuc AS endAt,
                    k.trang_thai AS active,
                    (SELECT COUNT(*) FROM dbo.khuyen_mai_bien_the x WHERE x.id_khuyen_mai = k.id_khuyen_mai) AS variantCount
                FROM dbo.khuyen_mai k ORDER BY k.id_khuyen_mai DESC
                """, new MapSqlParameterSource());
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        for (Map<String, Object> r : rows) {
            LocalDateTime start = ((java.sql.Timestamp) r.get("startAt")).toLocalDateTime();
            LocalDateTime end = ((java.sql.Timestamp) r.get("endAt")).toLocalDateTime();
            r.put("startText", SaleSupport.toDisplay(r.get("startAt")));
            r.put("endText", SaleSupport.toDisplay(r.get("endAt")));
            r.put("inWindow", !now.isBefore(start) && now.isBefore(end));
        }
        return rows;
    }

    public PromotionForm find(long id) {
        try {
            Map<String, Object> r = jdbc.queryForMap("""
                    SELECT ten_khuyen_mai, mo_ta, loai_giam, gia_tri_giam,
                           ngay_bat_dau, ngay_ket_thuc, trang_thai
                    FROM dbo.khuyen_mai WHERE id_khuyen_mai = :id
                    """, new MapSqlParameterSource("id", id));
            PromotionForm f = new PromotionForm();
            f.setTenKhuyenMai((String) r.get("ten_khuyen_mai"));
            f.setMoTa((String) r.get("mo_ta"));
            f.setLoaiGiam((String) r.get("loai_giam"));
            f.setGiaTriGiam((BigDecimal) r.get("gia_tri_giam"));
            f.setNgayBatDau(SaleSupport.toHtml(r.get("ngay_bat_dau")));
            f.setNgayKetThuc(SaleSupport.toHtml(r.get("ngay_ket_thuc")));
            f.setTrangThai(Boolean.TRUE.equals(r.get("trang_thai")));
            f.setVariantIds(jdbc.queryForList("""
                    SELECT id_bien_the FROM dbo.khuyen_mai_bien_the WHERE id_khuyen_mai=:id
                    """, new MapSqlParameterSource("id", id), Long.class));
            return f;
        } catch (EmptyResultDataAccessException ex) {
            throw new IllegalArgumentException("Khong tim thay khuyen mai.");
        }
    }

    @Transactional
    public long save(Long id, PromotionForm f) {
        String name = f.getTenKhuyenMai() == null ? "" : f.getTenKhuyenMai().trim();
        if (name.isEmpty() || name.length() > 200) throw new IllegalArgumentException("Ten khuyen mai phai co 1-200 ky tu.");
        String type = SaleSupport.type(f.getLoaiGiam());
        SaleSupport.validateDiscount(type, f.getGiaTriGiam());
        LocalDateTime start = SaleSupport.utcFromForm(f.getNgayBatDau());
        LocalDateTime end = SaleSupport.utcFromForm(f.getNgayKetThuc());
        SaleSupport.validateWindow(start, end);
        if (f.getMoTa() != null && f.getMoTa().length() > 500)
            throw new IllegalArgumentException("Mo ta vuot 500 ky tu.");
        Set<Long> ids = new HashSet<>(f.getVariantIds());
        if (ids.contains(null) || ids.size() > 500 || (f.isTrangThai() && ids.isEmpty()))
            throw new IllegalArgumentException("Khuyen mai dang bat phai chon tu 1 den 500 bien the.");
        if (!ids.isEmpty()) {
            Long count = jdbc.queryForObject("""
                    SELECT COUNT_BIG(*) FROM dbo.san_pham_bien_the WHERE id_bien_the IN (:ids)
                    """, new MapSqlParameterSource("ids", ids), Long.class);
            if (count == null || count != ids.size()) throw new IllegalArgumentException("Bien the khong ton tai.");
        }
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("id", id).addValue("name", name).addValue("description", f.getMoTa())
                .addValue("type", type).addValue("value", f.getGiaTriGiam())
                .addValue("start", start).addValue("end", end).addValue("active", f.isTrangThai());
        long savedId;
        if (id == null) {
            Long newId = jdbc.queryForObject("""
                    INSERT INTO dbo.khuyen_mai (ten_khuyen_mai, mo_ta, loai_giam, gia_tri_giam,
                                                ngay_bat_dau, ngay_ket_thuc, trang_thai)
                    OUTPUT INSERTED.id_khuyen_mai
                    VALUES (:name, :description, :type, :value, :start, :end, :active)
                    """, p, Long.class);
            if (newId == null) throw new IllegalStateException("Khong tao duoc khuyen mai.");
            savedId = newId;
        } else {
            int n = jdbc.update("""
                    UPDATE dbo.khuyen_mai SET ten_khuyen_mai=:name, mo_ta=:description,
                        loai_giam=:type, gia_tri_giam=:value, ngay_bat_dau=:start,
                        ngay_ket_thuc=:end, trang_thai=:active WHERE id_khuyen_mai=:id
                    """, p);
            if (n != 1) throw new IllegalArgumentException("Khong tim thay khuyen mai.");
            savedId = id;
            jdbc.update("DELETE FROM dbo.khuyen_mai_bien_the WHERE id_khuyen_mai=:id", p);
        }
        for (Long variantId : ids) {
            jdbc.update("""
                    INSERT INTO dbo.khuyen_mai_bien_the(id_khuyen_mai,id_bien_the) VALUES(:id,:variant)
                    """, new MapSqlParameterSource("id", savedId).addValue("variant", variantId));
        }
        return savedId;
    }

    @Transactional
    public void setActive(long id, boolean active) {
        int updated = jdbc.update("""
                UPDATE dbo.khuyen_mai SET trang_thai=:active WHERE id_khuyen_mai=:id
                """, new MapSqlParameterSource("id", id).addValue("active", active));
        if (updated != 1) throw new IllegalArgumentException("Khong tim thay khuyen mai.");
        if (active) {
            Long count = jdbc.queryForObject("""
                    SELECT COUNT_BIG(*) FROM dbo.khuyen_mai_bien_the WHERE id_khuyen_mai=:id
                    """, new MapSqlParameterSource("id", id), Long.class);
            if (count == null || count == 0) throw new IllegalArgumentException("Hay gan bien the truoc khi bat khuyen mai.");
        }
    }

    public List<Map<String, Object>> variantOptions() {
        return jdbc.queryForList("""
                SELECT TOP (500) b.id_bien_the AS id, b.gia_ban AS price, b.trang_thai AS active,
                    s.ten_san_pham AS product, m.ten_mau_sac AS color, k.ten_kich_thuoc AS size
                FROM dbo.san_pham_bien_the b
                JOIN dbo.san_pham s ON s.id_san_pham=b.id_san_pham
                JOIN dbo.bien_the_mau_sac m ON m.id_mau_sac=b.id_mau_sac
                JOIN dbo.bien_the_kich_thuoc k ON k.id_kich_thuoc=b.id_kich_thuoc
                ORDER BY b.id_bien_the DESC
                """, new MapSqlParameterSource());
    }

    public record Rule(String type, BigDecimal value) {}

    public Map<Long, List<Rule>> activeRules(Collection<Long> variantIds, LocalDateTime nowUtc) {
        if (variantIds == null || variantIds.isEmpty()) return Map.of();
        Map<Long, List<Rule>> rules = new HashMap<>();
        jdbc.query("""
                SELECT link.id_bien_the, k.loai_giam, k.gia_tri_giam
                FROM dbo.khuyen_mai_bien_the link
                JOIN dbo.khuyen_mai k ON k.id_khuyen_mai=link.id_khuyen_mai
                WHERE link.id_bien_the IN (:ids) AND k.trang_thai=1
                  AND k.ngay_bat_dau <= :now AND k.ngay_ket_thuc > :now
                """, new MapSqlParameterSource("ids", variantIds).addValue("now", nowUtc), rs -> {
            rules.computeIfAbsent(rs.getLong(1), ignored -> new ArrayList<>())
                    .add(new Rule(rs.getString(2), rs.getBigDecimal(3)));
        });
        return rules;
    }

    public static BigDecimal effectivePrice(BigDecimal original, List<Rule> rules) {
        BigDecimal bestDiscount = BigDecimal.ZERO.setScale(2);
        if (rules != null) for (Rule r : rules) {
            BigDecimal d = SaleSupport.discount(original, r.type(), r.value(), null);
            if (d.compareTo(bestDiscount) > 0) bestDiscount = d;
        }
        return original.subtract(bestDiscount).max(BigDecimal.ZERO).setScale(2);
    }
}
