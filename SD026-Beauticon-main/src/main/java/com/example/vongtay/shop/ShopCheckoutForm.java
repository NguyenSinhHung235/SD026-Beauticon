// src/main/java/com/example/vongtay/shop/ShopCheckoutForm.java
package com.example.vongtay.shop;

/** Never accept client-supplied prices, stock, customer IDs, or payment success flags. */
public class ShopCheckoutForm {
    private Long idDiaChi;
    private String tenNguoiNhan;
    private String soDienThoai;
    private String diaChi;
    private String phuongThucGiaoHang = "GIAO_TIEU_CHUAN";
    private String phuongThuc;
    private String maVoucher;
    private String ghiChu;
    private String maYeuCau;

    public Long getIdDiaChi() {return idDiaChi;}
    public void setIdDiaChi(Long v) {idDiaChi=v;}
    public String getTenNguoiNhan() {return tenNguoiNhan;}
    public void setTenNguoiNhan(String v) {tenNguoiNhan=v;}
    public String getSoDienThoai() {return soDienThoai;}
    public void setSoDienThoai(String v) {soDienThoai=v;}
    public String getDiaChi() {return diaChi;}
    public void setDiaChi(String v) {diaChi=v;}
    public String getPhuongThucGiaoHang() {return phuongThucGiaoHang;}
    public void setPhuongThucGiaoHang(String v) {phuongThucGiaoHang=v;}
    public String getPhuongThuc() {return phuongThuc;}
    public void setPhuongThuc(String v) {phuongThuc=v;}
    public String getMaVoucher() {return maVoucher;}
    public void setMaVoucher(String v) {maVoucher=v;}
    public String getGhiChu() {return ghiChu;}
    public void setGhiChu(String v) {ghiChu=v;}
    public String getMaYeuCau() {return maYeuCau;}
    public void setMaYeuCau(String v) {maYeuCau=v;}
}