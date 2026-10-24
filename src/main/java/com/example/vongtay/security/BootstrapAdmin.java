package com.example.vongtay.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/** Optional one-time local bootstrap. Does not reset/override existing accounts. */
@Component
public class BootstrapAdmin implements ApplicationRunner {
    private final NamedParameterJdbcTemplate jdbc;
    private final PasswordEncoder encoder;
    @Value("${app.bootstrap-admin.email:}") private String bootstrapEmail;
    @Value("${app.bootstrap-admin.password:}") private String bootstrapPassword;
    public BootstrapAdmin(NamedParameterJdbcTemplate jdbc, PasswordEncoder encoder) {
        this.jdbc = jdbc; this.encoder = encoder;
    }
    @Override public void run(ApplicationArguments args) {
        String email = bootstrapEmail == null ? "" : bootstrapEmail.strip().toLowerCase(Locale.ROOT);
        String password = bootstrapPassword == null ? "" : bootstrapPassword;
        if (email.isEmpty() && password.isEmpty()) return;
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$") || email.length() > 254 ||
            password.length() < 12 || password.length() > 128)
            throw new IllegalStateException("BOOTSTRAP_ADMIN_EMAIL hop le va BOOTSTRAP_ADMIN_PASSWORD tu 12 den 128 ky tu.");
        Long existing = jdbc.queryForObject("SELECT COUNT_BIG(*) FROM dbo.nhan_vien WHERE LOWER(email)=:email",
                new MapSqlParameterSource("email", email), Long.class);
        if (existing != null && existing > 0) return; // Deliberately never overwrite/reset credentials.
        jdbc.update("""
                INSERT INTO dbo.nhan_vien(ho_ten,email,mat_khau_hash,chuc_danh,trang_thai)
                VALUES(N'Quan tri he thong',:email,:hash,N'QUAN_LY',1)
                """, new MapSqlParameterSource("email", email).addValue("hash",encoder.encode(password)));
        System.out.println("Da tao tai khoan admin moi: " + email + ". Hay xoa bien bootstrap sau lan dau.");
    }
}
