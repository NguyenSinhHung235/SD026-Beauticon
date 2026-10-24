package com.example.vongtay.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/** Revokes active sessions after staff is disabled or their role changes. */
public class StaffSessionGuard extends OncePerRequestFilter {
    private final NamedParameterJdbcTemplate jdbc;
    public StaffSessionGuard(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                                FilterChain chain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof StaffPrincipal staff) {
            List<Map<String,Object>> rows = jdbc.queryForList("""
                    SELECT trang_thai AS active, chuc_danh AS title, mat_khau_hash AS hash, email AS email
                    FROM dbo.nhan_vien WHERE id_nhan_vien=:id
                    """, new MapSqlParameterSource("id", staff.id()));
            boolean valid = rows.size() == 1 && Boolean.TRUE.equals(rows.get(0).get("active"));
            if (valid) valid = staff.passwordHash().equals(rows.get(0).get("hash"))
                    && staff.email().equalsIgnoreCase((String)rows.get(0).get("email"));
            if (valid) {
                try { valid = StaffUserDetailsService.roleOf((String)rows.get(0).get("title")).equals(staff.role()); }
                catch (IllegalArgumentException ignored) { valid = false; }
            }
            if (!valid) {
                SecurityContextHolder.clearContext();
                if (request.getSession(false) != null) request.getSession(false).invalidate();
                response.sendRedirect(request.getContextPath() + "/login?expired");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
