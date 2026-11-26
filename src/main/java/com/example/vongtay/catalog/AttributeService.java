package com.example.vongtay.catalog;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class AttributeService {
    private final NamedParameterJdbcTemplate jdbc;

    public record Option(long id, String label, boolean active) {}
    public record AttributeRow(long id, String ten, String moTa, String quocGia,
                               String sdt, String email, String diaChi, String maMau, boolean active) {}

    public AttributeService(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Option> options(AttributeKind kind) {
        String sql = "SELECT " + kind.idColumn + " AS id, " + kind.nameColumn
                + " AS ten, trang_thai FROM dbo." + kind.table + " ORDER BY " + kind.nameColumn;
        return jdbc.query(sql, (rs, n) -> new Option(rs.getLong("id"), rs.getString("ten"), rs.getBoolean("trang_thai")));
    }

    public boolean exists(AttributeKind kind, Long id) {
        if (id == null) return false;
        Long count = jdbc.queryForObject("SELECT COUNT_BIG(*) FROM dbo." + kind.table
                + " WHERE " + kind.idColumn + "=:id", Map.of("id", id), Long.class);
        return count != null && count > 0;
    }

    public List<AttributeRow> list(AttributeKind kind, String keyword) {
        boolean filtered = keyword != null && !keyword.isBlank();
        String sql = select(kind) + (filtered ? " WHERE " + kind.nameColumn + " LIKE :keyword" : "")
                + " ORDER BY " + kind.nameColumn;
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (filtered) params.addValue("keyword", "%" + keyword.trim() + "%");
        return jdbc.query(sql, params, (rs, n) -> row(rs));
    }

    public AttributeRow get(AttributeKind kind, long id) {
        String sql = select(kind) + " WHERE " + kind.idColumn + "=:id";
        try {
            return jdbc.queryForObject(sql, Map.of("id", id), (rs, n) -> row(rs));
        } catch (EmptyResultDataAccessException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thuộc tính");
        }
    }

    public AttributeForm form(AttributeRow a) {
        AttributeForm f = new AttributeForm();
        f.setTen(a.ten()); f.setMoTa(a.moTa()); f.setQuocGia(a.quocGia());
        f.setSdt(a.sdt()); f.setEmail(a.email()); f.setDiaChi(a.diaChi());
        f.setMaMau(a.maMau()); f.setTrangThai(a.active());
        return f;
    }

    @Transactional
    public void save(AttributeKind kind, Long id, AttributeForm f) {
        if (id != null) get(kind, id);
        validate(kind, f);
        Map<String, Object> values = new LinkedHashMap<>();
        values.put(kind.nameColumn, f.getTen().trim());
        values.put("mo_ta", blankToNull(f.getMoTa()));
        values.put("trang_thai", f.isTrangThai());
        for (String extra : kind.extras) {
            values.put(extra, switch (extra) {
                case "quoc_gia" -> blankToNull(f.getQuocGia());
                case "sdt" -> blankToNull(f.getSdt());
                case "email" -> blankToNull(f.getEmail());
                case "dia_chi" -> blankToNull(f.getDiaChi());
                case "ma_mau" -> blankToNull(f.getMaMau());
                default -> throw new IllegalStateException("Unexpected extra column");
            });
        }
        MapSqlParameterSource p = new MapSqlParameterSource();
        values.forEach(p::addValue);
        String sql;
        if (id == null) {
            String columns = String.join(", ", values.keySet());
            String namedValues = values.keySet().stream().map(s -> ":" + s).collect(Collectors.joining(", "));
            sql = "INSERT INTO dbo." + kind.table + " (" + columns + ") VALUES (" + namedValues + ")";
        } else {
            p.addValue("id", id);
            String assignments = values.keySet().stream().map(s -> s + "=:" + s).collect(Collectors.joining(", "));
            sql = "UPDATE dbo." + kind.table + " SET " + assignments + " WHERE " + kind.idColumn + "=:id";
        }
        jdbc.update(sql, p);
    }

    @Transactional
    public void setActive(AttributeKind kind, long id, boolean active) {
        int changed = jdbc.update("UPDATE dbo." + kind.table + " SET trang_thai=:active WHERE "
                + kind.idColumn + "=:id", new MapSqlParameterSource().addValue("active", active).addValue("id", id));
        if (changed == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    private static String select(AttributeKind kind) {
        return "SELECT " + kind.idColumn + " AS id, " + kind.nameColumn + " AS ten, mo_ta, trang_thai, "
                + optional(kind, "quoc_gia") + ", " + optional(kind, "sdt") + ", "
                + optional(kind, "email") + ", " + optional(kind, "dia_chi") + ", "
                + optional(kind, "ma_mau") + " FROM dbo." + kind.table;
    }

    private static String optional(AttributeKind kind, String column) {
        return kind.has(column) ? column : "CAST(NULL AS NVARCHAR(500)) AS " + column;
    }

    private static AttributeRow row(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new AttributeRow(rs.getLong("id"), rs.getString("ten"), rs.getString("mo_ta"),
                rs.getString("quoc_gia"), rs.getString("sdt"), rs.getString("email"),
                rs.getString("dia_chi"), rs.getString("ma_mau"), rs.getBoolean("trang_thai"));
    }

    private static void validate(AttributeKind kind, AttributeForm f) {
        if (f.getTen() == null || f.getTen().isBlank() || f.getTen().trim().length() > 200)
            throw new IllegalArgumentException("Tên thuộc tính không được để trống hoặc quá 200 ký tự.");
        // Limits reflect individual SQL columns; prevent truncation before INSERT/UPDATE.
        int nameLimit = switch (kind) {
            case CATEGORY, BRAND -> 150;
            case MATERIAL, COLOR, SIZE -> 100;
            case SUPPLIER -> 200;
        };
        if (f.getTen().trim().length() > nameLimit)
            throw new IllegalArgumentException("Tên tối đa " + nameLimit + " ký tự.");
        if (f.getMoTa() != null && f.getMoTa().length() > (kind == AttributeKind.CATEGORY || kind == AttributeKind.BRAND ? 1000 : 500))
            throw new IllegalArgumentException("Mô tả quá dài.");
        if (kind.has("quoc_gia") && tooLong(f.getQuocGia(), 100)) throw new IllegalArgumentException("Quốc gia tối đa 100 ký tự.");
        if (kind.has("sdt") && tooLong(f.getSdt(), 20)) throw new IllegalArgumentException("SĐT tối đa 20 ký tự.");
        if (kind.has("email") && tooLong(f.getEmail(), 254)) throw new IllegalArgumentException("Email tối đa 254 ký tự.");
        if (kind.has("dia_chi") && tooLong(f.getDiaChi(), 500)) throw new IllegalArgumentException("Địa chỉ tối đa 500 ký tự.");
        if (kind.has("ma_mau") && f.getMaMau() != null && !f.getMaMau().isBlank()
                && !f.getMaMau().trim().matches("#[0-9a-fA-F]{6}"))
            throw new IllegalArgumentException("Mã màu phải có dạng #AABBCC.");
    }

    private static boolean tooLong(String str, int max) { return str != null && str.length() > max; }
    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
