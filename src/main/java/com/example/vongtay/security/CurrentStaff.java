package com.example.vongtay.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentStaff {
    public StaffPrincipal required() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof StaffPrincipal staff))
            throw new AccessDeniedException("Can dang nhap bang tai khoan nhan vien.");
        return staff;
    }
    public long id() { return required().id(); }
    public boolean isAdmin() { return "ADMIN".equals(required().role()); }
}
