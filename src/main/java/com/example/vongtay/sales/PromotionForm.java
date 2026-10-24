package com.example.vongtay.sales;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PromotionForm {
    private String tenKhuyenMai;
    private String moTa;
    private String loaiGiam = "PHAN_TRAM";
    private BigDecimal giaTriGiam;
    private String ngayBatDau;
    private String ngayKetThuc;
    private boolean trangThai = true;
    private List<Long> variantIds = new ArrayList<>();

    public String getTenKhuyenMai() { return tenKhuyenMai; }
    public void setTenKhuyenMai(String v) { tenKhuyenMai = v; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String v) { moTa = v; }
    public String getLoaiGiam() { return loaiGiam; }
    public void setLoaiGiam(String v) { loaiGiam = v; }
    public BigDecimal getGiaTriGiam() { return giaTriGiam; }
    public void setGiaTriGiam(BigDecimal v) { giaTriGiam = v; }
    public String getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(String v) { ngayBatDau = v; }
    public String getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(String v) { ngayKetThuc = v; }
    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean v) { trangThai = v; }
    public List<Long> getVariantIds() { return variantIds; }
    public void setVariantIds(List<Long> v) { variantIds = v == null ? new ArrayList<>() : v; }
}
