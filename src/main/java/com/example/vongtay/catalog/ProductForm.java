package com.example.vongtay.catalog;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

/** Form DTO: khong bind truc tiep Entity tu HTTP request. */
public class ProductForm {
    private String tenSanPham;
    private Long idLoai;
    private Long idThuongHieu;
    private Long idNhaCungCap;
    private Long idChatLieu;
    private BigDecimal trongLuongGram;
    private String moTa;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate ngaySanXuat;
    private boolean trangThai = true;

    public static ProductForm from(Product p) {
        ProductForm f = new ProductForm();
        f.tenSanPham = p.getTenSanPham();
        f.idLoai = p.getIdLoai();
        f.idThuongHieu = p.getIdThuongHieu();
        f.idNhaCungCap = p.getIdNhaCungCap();
        f.idChatLieu = p.getIdChatLieu();
        f.trongLuongGram = p.getTrongLuongGram();
        f.moTa = p.getMoTa();
        f.ngaySanXuat = p.getNgaySanXuat();
        f.trangThai = p.isTrangThai();
        return f;
    }
    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String v) { tenSanPham = v; }
    public Long getIdLoai() { return idLoai; }
    public void setIdLoai(Long v) { idLoai = v; }
    public Long getIdThuongHieu() { return idThuongHieu; }
    public void setIdThuongHieu(Long v) { idThuongHieu = v; }
    public Long getIdNhaCungCap() { return idNhaCungCap; }
    public void setIdNhaCungCap(Long v) { idNhaCungCap = v; }
    public Long getIdChatLieu() { return idChatLieu; }
    public void setIdChatLieu(Long v) { idChatLieu = v; }
    public BigDecimal getTrongLuongGram() { return trongLuongGram; }
    public void setTrongLuongGram(BigDecimal v) { trongLuongGram = v; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String v) { moTa = v; }
    public LocalDate getNgaySanXuat() { return ngaySanXuat; }
    public void setNgaySanXuat(LocalDate v) { ngaySanXuat = v; }
    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean v) { trangThai = v; }
}
