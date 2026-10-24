package com.example.vongtay.catalog;

import java.math.BigDecimal;

public class VariantForm {
    private Long idMauSac;
    private Long idKichThuoc;
    private BigDecimal giaBan;
    private Integer soLuongTon = 0;
    private boolean trangThai = true;
    private Integer expectedStock;

    public static VariantForm from(ProductVariant v) {
        VariantForm f = new VariantForm();
        f.idMauSac = v.getIdMauSac();
        f.idKichThuoc = v.getIdKichThuoc();
        f.giaBan = v.getGiaBan();
        f.soLuongTon = v.getSoLuongTon();
        f.expectedStock = v.getSoLuongTon();
        f.trangThai = v.isTrangThai();
        return f;
    }
    public Long getIdMauSac() { return idMauSac; }
    public void setIdMauSac(Long v) { idMauSac = v; }
    public Long getIdKichThuoc() { return idKichThuoc; }
    public void setIdKichThuoc(Long v) { idKichThuoc = v; }
    public BigDecimal getGiaBan() { return giaBan; }
    public void setGiaBan(BigDecimal v) { giaBan = v; }
    public Integer getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(Integer v) { soLuongTon = v; }
    public Integer getExpectedStock() { return expectedStock; }
    public void setExpectedStock(Integer v) { expectedStock = v; }
    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean v) { trangThai = v; }
}
