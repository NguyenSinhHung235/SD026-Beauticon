package com.example.vongtay.catalog;

/** Chung cho 6 loai thuoc tinh; field khong phu hop se khong duoc ghi vao SQL. */
public class AttributeForm {
    private String ten;
    private String moTa;
    private String quocGia;
    private String sdt;
    private String email;
    private String diaChi;
    private String maMau;
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
    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean v) { trangThai = v; }
}
