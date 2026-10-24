package com.example.vongtay.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class SignedInModel {
    @ModelAttribute("signedInStaff")
    public StaffPrincipal staff() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getPrincipal() instanceof StaffPrincipal p ? p : null;
    }
    @ModelAttribute("isAdmin")
    public boolean isAdmin() {
        StaffPrincipal staff = staff();
        return staff != null && "ADMIN".equals(staff.role());
    }
}
