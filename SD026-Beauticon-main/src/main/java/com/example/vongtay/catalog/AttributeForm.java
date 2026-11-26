package com.example.vongtay.catalog;

import java.math.BigDecimal;

public class AttributeForm {
    private String ten;
    private String moTa;
    private String quocGia;
    private String sdt;
    private String email;
    private String diaChi;
    private String maMau;
    private BigDecimal donGiaGram = BigDecimal.ZERO;
    private boolean trangThai = true;

    public String getTen() { return ten; }
    public void setTen(String v) { ten = v; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String v) { moTa = v; }
    public String getQuocGia() { return quocGia; }
    public void setQuocGia(String v) { quocGia = v; }
    public String getSdt() { return sdt; }
    public void setSdt(String v) { sdt = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { email = v; }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String v) { diaChi = v; }
    public String getMaMau() { return maMau; }
    public void setMaMau(String v) { maMau = v; }
    public BigDecimal getDonGiaGram() { return donGiaGram; }
    public void setDonGiaGram(BigDecimal v) { donGiaGram = v; }
    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean v) { trangThai = v; }
}
