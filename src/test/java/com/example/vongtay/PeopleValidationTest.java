package com.example.vongtay;

import com.example.vongtay.people.PeopleValidation;
import com.example.vongtay.security.StaffUserDetailsService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PeopleValidationTest {
    @Test void requiredStaffInput() {
        assertEquals("admin@example.com", PeopleValidation.email(" ADMIN@example.com ",true));
        assertEquals("ADMIN",StaffUserDetailsService.roleOf("QUAN_LY"));
        assertEquals("STAFF",StaffUserDetailsService.roleOf("NHAN_VIEN"));
        assertThrows(IllegalArgumentException.class,()->StaffUserDetailsService.roleOf("SUPER_USER"));
    }
    @Test void sensitiveInputValidation() {
        assertThrows(IllegalArgumentException.class,()->PeopleValidation.password("short",true));
        assertNull(PeopleValidation.password("",false));
        assertThrows(IllegalArgumentException.class,()->PeopleValidation.birthday(LocalDate.now().plusDays(1)));
        assertThrows(IllegalArgumentException.class,()->PeopleValidation.phone("abc",true));
    }
}
