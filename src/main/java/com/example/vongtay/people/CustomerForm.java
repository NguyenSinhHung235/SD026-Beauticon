package com.example.vongtay.people;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class CustomerForm {
    private String hoTen, email, sdt;
    @DateTimeFormat(iso=DateTimeFormat.ISO.DATE)
    private LocalDate ngaySinh;
    public String getHoTen() { return hoTen; }
    public void setHoTen(String v) { hoTen=v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { email=v; }
    public String getSdt() { return sdt; }
    public void setSdt(String v) { sdt=v; }
    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate v) { ngaySinh=v; }
}
