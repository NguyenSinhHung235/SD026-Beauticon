package com.example.vongtay.catalog;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "san_pham", schema = "dbo")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_san_pham")
    private Long idSanPham;

    @Column(name = "id_loai", nullable = false)
    private Long idLoai;
    @Column(name = "id_thuong_hieu")
    private Long idThuongHieu;
    @Column(name = "id_nha_cung_cap")
    private Long idNhaCungCap;
    @Column(name = "id_chat_lieu")
    private Long idChatLieu;
    @Column(name = "ten_san_pham", nullable = false, length = 200)
    private String tenSanPham;
    @Column(name = "trong_luong_gram", precision = 10, scale = 3)
    private BigDecimal trongLuongGram;
    @Column(name = "mo_ta", columnDefinition = "nvarchar(max)")
    private String moTa;
    @Column(name = "ngay_san_xuat")
    private LocalDate ngaySanXuat;
    @Column(name = "trang_thai", nullable = false)
    private boolean trangThai = true;
    @Column(name = "ngay_tao", nullable = false, updatable = false)
    private LocalDateTime ngayTao;

    @PrePersist
    void created() {
        if (ngayTao == null) ngayTao = LocalDateTime.now(java.time.Clock.systemUTC());
    }

    public Long getIdSanPham() { return idSanPham; }
    public Long getIdLoai() { return idLoai; }
    public void setIdLoai(Long idLoai) { this.idLoai = idLoai; }
    public Long getIdThuongHieu() { return idThuongHieu; }
    public void setIdThuongHieu(Long idThuongHieu) { this.idThuongHieu = idThuongHieu; }
    public Long getIdNhaCungCap() { return idNhaCungCap; }
    public void setIdNhaCungCap(Long idNhaCungCap) { this.idNhaCungCap = idNhaCungCap; }
    public Long getIdChatLieu() { return idChatLieu; }
    public void setIdChatLieu(Long idChatLieu) { this.idChatLieu = idChatLieu; }
    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }
    public BigDecimal getTrongLuongGram() { return trongLuongGram; }
    public void setTrongLuongGram(BigDecimal trongLuongGram) { this.trongLuongGram = trongLuongGram; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public LocalDate getNgaySanXuat() { return ngaySanXuat; }
    public void setNgaySanXuat(LocalDate ngaySanXuat) { this.ngaySanXuat = ngaySanXuat; }
    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }
    public LocalDateTime getNgayTao() { return ngayTao; }
}
