package com.example.vongtay.shop;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShopAuthService {
    private final NamedParameterJdbcTemplate jdbc;
    private final PasswordEncoder passwords;

    public ShopAuthService(NamedParameterJdbcTemplate jdbc, PasswordEncoder passwords) {
        this.jdbc = jdbc;
        this.passwords = passwords;
    }

    public record Customer(long id, String name, String email) {}
    public record Credentials(Customer customer, String hash) {}

    public static String email(String raw) {
        String e = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        if (e.length() > 254 || !e.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new IllegalArgumentException("Email không hợp lệ.");
        return e;
    }

    public static String fullName(String raw) {
        String n = raw == null ? "" : raw.trim().replaceAll("\\s+", " ");
        if (n.length() < 2 || n.length() > 150)
            throw new IllegalArgumentException("Họ tên cần từ 2 đến 150 ký tự.");
        return n;
    }

    public static String phone(String raw, boolean required) {
        String s = raw == null ? "" : raw.trim();
        if (s.isEmpty() && !required) return null;
        if (!s.matches("0[0-9]{9,10}"))
            throw new IllegalArgumentException("Số điện thoại cần có 10-11 chữ số và bắt đầu bằng 0.");
        return s;
    }

    public static void password(String value) {
        if (value == null || value.length() < 12 || value.length() > 72
                || value.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("Mật khẩu cần từ 12 ký tự và không quá 72 byte.");
    }

    @Transactional
    public Credentials register(String rawName, String rawEmail, String rawPhone,
                                String rawPassword, String confirm) {
        String name = fullName(rawName);
        String e = email(rawEmail);
        String phone = phone(rawPhone, false);
        password(rawPassword);
        if (!rawPassword.equals(confirm))
            throw new IllegalArgumentException("Hai mật khẩu không khớp.");
        Long existing = jdbc.queryForObject("SELECT COUNT_BIG(*) FROM dbo.khach_hang WHERE email=:e",
                new MapSqlParameterSource("e", e), Long.class);
        if (existing != null && existing > 0)
            throw new IllegalArgumentException("Email đã được đăng ký. Hãy đăng nhập.");
        String hash = passwords.encode(rawPassword);
        try {
            Long id = jdbc.queryForObject("""
                    INSERT INTO dbo.khach_hang(ho_ten,email,sdt,mat_khau_hash,trang_thai)
                    OUTPUT INSERTED.id_khach_hang VALUES(:name,:email,:phone,:hash,1)
                    """, new MapSqlParameterSource().addValue("name", name).addValue("email", e)
                    .addValue("phone", phone).addValue("hash", hash), Long.class);
            if (id == null) throw new IllegalStateException("Khong tao duoc tai khoan.");
            return new Credentials(new Customer(id, name, e), hash);
        } catch (DataIntegrityViolationException duplicate) {
            throw new IllegalArgumentException("Email đã được sử dụng.");
        }
    }

    public Credentials login(String rawEmail, String rawPassword) {
        String e = email(rawEmail);
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT id_khach_hang AS id,ho_ten AS name,email,mat_khau_hash AS hash,trang_thai AS active
                FROM dbo.khach_hang WHERE email=:email
                """, new MapSqlParameterSource("email", e));
        if (rows.size() != 1 || !Boolean.TRUE.equals(rows.get(0).get("active")))
            throw new IllegalArgumentException("Email hoặc mật khẩu không đúng.");
        Map<String, Object> row = rows.get(0);
        String hash = (String) row.get("hash");
        if (hash == null || rawPassword == null || !passwords.matches(rawPassword, hash))
            throw new IllegalArgumentException("Email hoặc mật khẩu không đúng.");
        return new Credentials(new Customer(((Number)row.get("id")).longValue(),
                (String)row.get("name"), (String)row.get("email")), hash);
    }

    public Customer verifiedCustomer(long id, String hash) {
        List<Map<String,Object>> rows = jdbc.queryForList("""
                SELECT ho_ten AS name,email,mat_khau_hash AS hash,trang_thai AS active
                FROM dbo.khach_hang WHERE id_khach_hang=:id
                """, new MapSqlParameterSource("id",id));
        if (rows.size()!=1 || !Boolean.TRUE.equals(rows.get(0).get("active"))) return null;
        Map<String,Object> row = rows.get(0);
        if (hash == null || !hash.equals(row.get("hash"))) return null;
        return new Customer(id,(String)row.get("name"),(String)row.get("email"));
    }
}
