package com.example.vongtay.people;

import java.time.LocalDate;
import java.util.Locale;

/** Shared, dependency-free input rules for admin forms. */
public final class PeopleValidation {
    private PeopleValidation() {}
    public static String name(String value, String field) {
        String v = value == null ? "" : value.strip();
        if (v.length() < 2 || v.length() > 150) throw new IllegalArgumentException(field + " phai tu 2 den 150 ky tu.");
        return v;
    }
    public static String email(String value, boolean required) {
        String v = value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
        if (v.isEmpty() && !required) return null;
        if (v.length() > 254 || !v.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            throw new IllegalArgumentException("Email khong hop le.");
        return v;
    }
    public static String phone(String value, boolean required) {
        String v = value == null ? "" : value.strip();
        if (v.isEmpty() && !required) return null;
        if (!v.matches("^\\+?[0-9][0-9 .-]{7,19}$"))
            throw new IllegalArgumentException("So dien thoai khong hop le.");
        return v;
    }
    public static String password(String password, boolean required) {
        String v = password == null ? "" : password;
        if (v.isBlank() && !required) return null;
        if (v.length() < 12 || v.length() > 128 || v.isBlank())
            throw new IllegalArgumentException("Mat khau moi phai tu 12 den 128 ky tu.");
        return v;
    }
    public static LocalDate birthday(LocalDate value) {
        if (value != null && (value.isAfter(LocalDate.now()) || value.isBefore(LocalDate.of(1900,1,1))))
            throw new IllegalArgumentException("Ngay sinh khong hop le.");
        return value;
    }
    public static String detail(String value) {
        String v = value == null ? "" : value.strip();
        if (v.length() < 5 || v.length() > 500)
            throw new IllegalArgumentException("Dia chi chi tiet phai tu 5 den 500 ky tu.");
        return v;
    }
}
