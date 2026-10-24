package com.example.vongtay.catalog;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "san_pham_bien_the", schema = "dbo")
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bien_the")
    private Long idBienThe;
    @Column(name = "id_san_pham", nullable = false)
    private Long idSanPham;
    @Column(name = "id_mau_sac", nullable = false)
    private Long idMauSac;
    @Column(name = "id_kich_thuoc", nullable = false)
    private Long idKichThuoc;
    @Column(name = "gia_ban", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaBan;
    @Column(name = "so_luong_ton", nullable = false)
    private Integer soLuongTon;
    @Column(name = "trang_thai", nullable = false)
    private boolean trangThai = true;

    public Long getIdBienThe() { return idBienThe; }
    public Long getIdSanPham() { return idSanPham; }
    public void setIdSanPham(Long idSanPham) { this.idSanPham = idSanPham; }
    public Long getIdMauSac() { return idMauSac; }
    public void setIdMauSac(Long idMauSac) { this.idMauSac = idMauSac; }
    public Long getIdKichThuoc() { return idKichThuoc; }
    public void setIdKichThuoc(Long idKichThuoc) { this.idKichThuoc = idKichThuoc; }
    public BigDecimal getGiaBan() { return giaBan; }
    public void setGiaBan(BigDecimal giaBan) { this.giaBan = giaBan; }
    public Integer getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(Integer soLuongTon) { this.soLuongTon = soLuongTon; }
    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }
}
