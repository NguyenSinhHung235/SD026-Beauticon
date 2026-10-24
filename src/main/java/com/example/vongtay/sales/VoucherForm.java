package com.example.vongtay.sales;

import java.math.BigDecimal;

public class VoucherForm {
    private String code;
    private String moTa;
    private String loaiGiam = "PHAN_TRAM";
    private BigDecimal giaTriGiam;
    private BigDecimal giaTriDonToiThieu = BigDecimal.ZERO;
    private BigDecimal giamToiDa;
    private Integer soLuong;
    private String ngayBatDau;
    private String ngayKetThuc;
    private Long idNhanVien;
    private boolean trangThai = true;

    public String getCode() { return code; }
    public void setCode(String v) { code = v; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String v) { moTa = v; }
    public String getLoaiGiam() { return loaiGiam; }
    public void setLoaiGiam(String v) { loaiGiam = v; }
    public BigDecimal getGiaTriGiam() { return giaTriGiam; }
    public void setGiaTriGiam(BigDecimal v) { giaTriGiam = v; }
    public BigDecimal getGiaTriDonToiThieu() { return giaTriDonToiThieu; }
    public void setGiaTriDonToiThieu(BigDecimal v) { giaTriDonToiThieu = v; }
    public BigDecimal getGiamToiDa() { return giamToiDa; }
    public void setGiamToiDa(BigDecimal v) { giamToiDa = v; }
    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer v) { soLuong = v; }
    public String getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(String v) { ngayBatDau = v; }
    public String getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(String v) { ngayKetThuc = v; }
    public Long getIdNhanVien() { return idNhanVien; }
    public void setIdNhanVien(Long v) { idNhanVien = v; }
    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean v) { trangThai = v; }
}
