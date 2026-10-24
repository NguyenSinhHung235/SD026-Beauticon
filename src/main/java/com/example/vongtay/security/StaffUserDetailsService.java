package com.example.vongtay.security;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class StaffUserDetailsService implements UserDetailsService {
    private final NamedParameterJdbcTemplate jdbc;
    public StaffUserDetailsService(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    public static String roleOf(String title) {
        return switch (title == null ? "" : title.trim().toUpperCase(Locale.ROOT)) {
            case "QUAN_LY" -> "ADMIN";
            case "NHAN_VIEN" -> "STAFF";
            default -> throw new IllegalArgumentException("Chuc danh phai la QUAN_LY hoac NHAN_VIEN.");
        };
    }

    @Override public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String email = username == null ? "" : username.strip().toLowerCase(Locale.ROOT);
        List<Map<String,Object>> rows = jdbc.queryForList("""
                SELECT id_nhan_vien AS id, ho_ten AS name, email, mat_khau_hash AS hash,
                       chuc_danh AS title, trang_thai AS active
                FROM dbo.nhan_vien WHERE LOWER(email)=:email
                """, new MapSqlParameterSource("email", email));
        if (rows.size() != 1) throw new UsernameNotFoundException("Tai khoan khong ton tai.");
        Map<String,Object> r = rows.get(0);
        try {
            return new StaffPrincipal(((Number)r.get("id")).longValue(), (String)r.get("name"),
                    (String)r.get("email"), (String)r.get("hash"),
                    roleOf((String)r.get("title")), Boolean.TRUE.equals(r.get("active")));
        } catch (IllegalArgumentException ex) {
            throw new UsernameNotFoundException("Tai khoan chua duoc phan quyen hop le.");
        }
    }
}
