/* BEAUTICON GD8 - 10 BAN GHI MOI MOI BANG, 22 BANG, SQL SERVER 2019+
   Chay tren database da migrate den GD8. Du lieu HOAN TOAN GIA LAP.
   Khong xoa, sua ban ghi that. Chay lai se tu choi neu da seed.
   Nhan vien demo bi khoa; khach demo khong co mat khau. So dien thoai chi la du lieu gia lap.
   Anh SVG di kem phai copy vao uploads/beauticon-demo-01.svg ... 10.svg.
*/
USE VongTayNu;
GO
SET XACT_ABORT ON;
BEGIN TRY
BEGIN TRANSACTION;
IF EXISTS (SELECT 1 FROM dbo.ma_giam_gia WHERE code='BCDEMO2026-01')
    THROW 51090, N'Da nap bo fake data nay. Khong chay lai.', 1;
IF OBJECT_ID('dbo.lich_su_don_hang','U') IS NULL OR COL_LENGTH('dbo.don_hang','ma_yeu_cau_online') IS NULL
    THROW 51091, N'Can migrate database den GD8 truoc.', 1;
DECLARE @ids TABLE (n INT PRIMARY KEY, loai BIGINT, brand BIGINT, ncc BIGINT, chatlieu BIGINT, mau BIGINT, sizeid BIGINT, nv BIGINT, kh BIGINT, dc BIGINT, sp BIGINT, bt BIGINT, voucher BIGINT, km BIGINT, gio BIGINT, ctgio BIGINT, dh BIGINT, ctdh BIGINT, dg BIGINT, tt BIGINT, ls BIGINT, anh BIGINT);
DECLARE @i INT=1, @id BIGINT, @loai BIGINT, @brand BIGINT, @ncc BIGINT, @chatlieu BIGINT, @mau BIGINT, @sizeid BIGINT, @nv BIGINT, @kh BIGINT, @dc BIGINT, @sp BIGINT, @bt BIGINT, @voucher BIGINT, @km BIGINT, @gio BIGINT, @ctgio BIGINT, @dh BIGINT, @ctdh BIGINT, @dg BIGINT, @tt BIGINT, @ls BIGINT, @anh BIGINT;
WHILE @i<=10
BEGIN
 IF @i=1 BEGIN
  INSERT dbo.loai_san_pham(ten_loai,mo_ta) VALUES(N'Vòng tay charm',N'Phân loại vòng tay nữ – bộ dữ liệu minh họa'); SET @loai=SCOPE_IDENTITY();
  INSERT dbo.thuong_hieu(ten_thuong_hieu,quoc_gia,mo_ta) VALUES(N'BeautIcon Studio',N'Việt Nam',N'Thương hiệu minh họa, không đại diện đơn vị thực'); SET @brand=SCOPE_IDENTITY();
  INSERT dbo.nha_cung_cap(ten_nha_cung_cap,sdt,email,dia_chi) VALUES(N'Xưởng phụ kiện An Nhiên', '0002547092', 'nhacungcap01@example.invalid', N'Hà Nội (địa chỉ minh họa)'); SET @ncc=SCOPE_IDENTITY();
  INSERT dbo.chat_lieu(ten_chat_lieu,mo_ta) VALUES(N'Bạc 925',N'Chất liệu tham khảo cho dữ liệu thử nghiệm'); SET @chatlieu=SCOPE_IDENTITY();
  INSERT dbo.bien_the_mau_sac(ten_mau_sac,ma_mau,mo_ta) VALUES(N'Bạc ánh trăng','#C0C0C0',N'Màu minh họa'); SET @mau=SCOPE_IDENTITY();
  INSERT dbo.bien_the_kich_thuoc(ten_kich_thuoc,mo_ta) VALUES(N'Freesize',N'Chu vi cổ tay tham khảo'); SET @sizeid=SCOPE_IDENTITY();
  INSERT dbo.nhan_vien(ho_ten,email,mat_khau_hash,chuc_danh,trang_thai) VALUES(N'Phạm Gia Hân','demo.nv01@example.invalid','DEMO_DISABLED_NO_LOGIN',N'NHAN_VIEN',0); SET @nv=SCOPE_IDENTITY();
  INSERT dbo.khach_hang(ho_ten,email,sdt,ngay_sinh,mat_khau_hash,trang_thai) VALUES(N'Nguyễn Khánh Linh','demo.kh01@example.invalid','0002547092',DATEADD(YEAR,-(22+0),CAST(SYSUTCDATETIME() AS DATE)),NULL,1); SET @kh=SCOPE_IDENTITY();
  INSERT dbo.dia_chi_giao_hang(id_khach_hang,ten_nguoi_nhan,sdt,dia_chi_chi_tiet,mac_dinh) VALUES(@kh,N'Nguyễn Khánh Linh','0002547092',N'Số 12, Đường Hoa Mai, Hà Nội (giả lập)',1); SET @dc=SCOPE_IDENTITY();
  INSERT dbo.san_pham(id_loai,id_thuong_hieu,id_nha_cung_cap,id_chat_lieu,ten_san_pham,trong_luong_gram,mo_ta,ngay_san_xuat) VALUES(@loai,@brand,@ncc,@chatlieu,N'Vòng tay Ánh Sao',8.0,N'Thiết kế thanh lịch, phù hợp làm quà tặng. Nội dung và thông số sản phẩm chỉ để demo.',DATEADD(DAY,-30,CAST(SYSUTCDATETIME() AS DATE))); SET @sp=SCOPE_IDENTITY();
  INSERT dbo.san_pham_bien_the(id_san_pham,id_mau_sac,id_kich_thuoc,gia_ban,so_luong_ton) VALUES(@sp,@mau,@sizeid,129000,6); SET @bt=SCOPE_IDENTITY();
  INSERT dbo.hinh_anh_sp(id_san_pham,duong_dan_anh,anh_chinh) VALUES(@sp,'/uploads/beauticon-demo-01.svg',1); SET @anh=SCOPE_IDENTITY();
  INSERT dbo.ma_giam_gia(code,mo_ta,loai_giam,gia_tri_giam,gia_tri_don_toi_thieu,giam_toi_da,so_luong,so_luong_da_dung,ngay_bat_dau,ngay_ket_thuc,id_nhan_vien,trang_thai) VALUES('BCDEMO2026-01',N'Voucher giả lập để kiểm thử','PHAN_TRAM',5,100000,50000,50,0,DATEADD(DAY,-15,SYSUTCDATETIME()),DATEADD(DAY,60,SYSUTCDATETIME()),@nv,1); SET @voucher=SCOPE_IDENTITY();
  INSERT dbo.khuyen_mai(ten_khuyen_mai,mo_ta,loai_giam,gia_tri_giam,ngay_bat_dau,ngay_ket_thuc,trang_thai) VALUES(N'Ưu đãi mùa thu 01 (demo)',N'Khuyến mãi giả lập','PHAN_TRAM',5,DATEADD(DAY,-10,SYSUTCDATETIME()),DATEADD(DAY,45,SYSUTCDATETIME()),1); SET @km=SCOPE_IDENTITY();
  INSERT dbo.khuyen_mai_bien_the(id_khuyen_mai,id_bien_the) VALUES(@km,@bt);
  INSERT dbo.gio_hang(id_khach_hang) VALUES(@kh); SET @gio=SCOPE_IDENTITY();
  INSERT dbo.chi_tiet_gio_hang(id_gio_hang,id_bien_the,so_luong) VALUES(@gio,@bt,1); SET @ctgio=SCOPE_IDENTITY();
  INSERT dbo.don_hang(id_khach_hang,id_nhan_vien,id_dia_chi_giao_hang,kenh_ban,trang_thai,ngay_dat,tien_hang,tien_giam,phi_van_chuyen,ten_nguoi_nhan,sdt_nguoi_nhan,dia_chi_giao_snapshot,ghi_chu,ton_kho_da_tru,voucher_da_su_dung,ma_yeu_cau_online) VALUES(@kh,@nv,@dc,'ONLINE','HOAN_THANH',DATEADD(DAY,-1,SYSUTCDATETIME()),129000,0,0,N'Nguyễn Khánh Linh','0002547092',N'Số 12, Đường Hoa Mai (giả lập)',N'Đơn hàng minh họa - không giao thực tế',1,0,'bc-demo-online-01'); SET @dh=SCOPE_IDENTITY();
  INSERT dbo.don_hang_chi_tiet(id_don_hang,id_bien_the,so_luong,gia_ban) VALUES(@dh,@bt,1,129000); SET @ctdh=SCOPE_IDENTITY();
  INSERT dbo.danh_gia(id_dh_chi_tiet,diem,binh_luan) VALUES(@ctdh,4,N'Thiết kế tinh tế, đeo nhẹ tay. (đánh giá giả lập)'); SET @dg=SCOPE_IDENTITY();
  INSERT dbo.thanh_toan(id_don_hang,phuong_thuc,loai_giao_dich,so_tien,trang_thai,ma_giao_dich,ngay_thanh_toan) VALUES(@dh,'CHUYEN_KHOAN','THANH_TOAN',129000,'THANH_CONG','BC-DEMO-PAY-01',DATEADD(DAY,-1,SYSUTCDATETIME())); SET @tt=SCOPE_IDENTITY();
  INSERT dbo.lich_su_don_hang(id_don_hang,id_nhan_vien,su_kien,trang_thai_cu,trang_thai_moi,ghi_chu) VALUES(@dh,@nv,'CHUYEN_TRANG_THAI','HOAN_THANH','HOAN_THANH',N'Nhật ký tạo bởi dữ liệu giả lập'); SET @ls=SCOPE_IDENTITY();
 END
 IF @i=1 INSERT @ids(n,loai,brand,ncc,chatlieu,mau,sizeid,nv,kh,dc,sp,bt,voucher,km,gio,ctgio,dh,ctdh,dg,tt,ls,anh) VALUES(@i,@loai,@brand,@ncc,@chatlieu,@mau,@sizeid,@nv,@kh,@dc,@sp,@bt,@voucher,@km,@gio,@ctgio,@dh,@ctdh,@dg,@tt,@ls,@anh);
 IF @i=2 BEGIN
  INSERT dbo.loai_san_pham(ten_loai,mo_ta) VALUES(N'Vòng tay bạc',N'Phân loại vòng tay nữ – bộ dữ liệu minh họa'); SET @loai=SCOPE_IDENTITY();
  INSERT dbo.thuong_hieu(ten_thuong_hieu,quoc_gia,mo_ta) VALUES(N'Luna Jewelry',N'Việt Nam',N'Thương hiệu minh họa, không đại diện đơn vị thực'); SET @brand=SCOPE_IDENTITY();
  INSERT dbo.nha_cung_cap(ten_nha_cung_cap,sdt,email,dia_chi) VALUES(N'Xưởng phụ kiện Lam Ngọc', '0009760300', 'nhacungcap02@example.invalid', N'Đà Nẵng (địa chỉ minh họa)'); SET @ncc=SCOPE_IDENTITY();
  INSERT dbo.chat_lieu(ten_chat_lieu,mo_ta) VALUES(N'Thép không gỉ',N'Chất liệu tham khảo cho dữ liệu thử nghiệm'); SET @chatlieu=SCOPE_IDENTITY();
  INSERT dbo.bien_the_mau_sac(ten_mau_sac,ma_mau,mo_ta) VALUES(N'Vàng champagne','#E3C78F',N'Màu minh họa'); SET @mau=SCOPE_IDENTITY();
  INSERT dbo.bien_the_kich_thuoc(ten_kich_thuoc,mo_ta) VALUES(N'14 cm',N'Chu vi cổ tay tham khảo'); SET @sizeid=SCOPE_IDENTITY();
  INSERT dbo.nhan_vien(ho_ten,email,mat_khau_hash,chuc_danh,trang_thai) VALUES(N'Đỗ Minh Châu','demo.nv02@example.invalid','DEMO_DISABLED_NO_LOGIN',N'NHAN_VIEN',0); SET @nv=SCOPE_IDENTITY();
  INSERT dbo.khach_hang(ho_ten,email,sdt,ngay_sinh,mat_khau_hash,trang_thai) VALUES(N'Trần Ngọc Hà','demo.kh02@example.invalid','0009760300',DATEADD(YEAR,-(22+1),CAST(SYSUTCDATETIME() AS DATE)),NULL,1); SET @kh=SCOPE_IDENTITY();
  INSERT dbo.dia_chi_giao_hang(id_khach_hang,ten_nguoi_nhan,sdt,dia_chi_chi_tiet,mac_dinh) VALUES(@kh,N'Trần Ngọc Hà','0009760300',N'Số 19, Đường Hoa Mai, Đà Nẵng (giả lập)',1); SET @dc=SCOPE_IDENTITY();
  INSERT dbo.san_pham(id_loai,id_thuong_hieu,id_nha_cung_cap,id_chat_lieu,ten_san_pham,trong_luong_gram,mo_ta,ngay_san_xuat) VALUES(@loai,@brand,@ncc,@chatlieu,N'Vòng tay Sương Mai',9.25,N'Thiết kế thanh lịch, phù hợp làm quà tặng. Nội dung và thông số sản phẩm chỉ để demo.',DATEADD(DAY,-35,CAST(SYSUTCDATETIME() AS DATE))); SET @sp=SCOPE_IDENTITY();
  INSERT dbo.san_pham_bien_the(id_san_pham,id_mau_sac,id_kich_thuoc,gia_ban,so_luong_ton) VALUES(@sp,@mau,@sizeid,161000,10); SET @bt=SCOPE_IDENTITY();
  INSERT dbo.hinh_anh_sp(id_san_pham,duong_dan_anh,anh_chinh) VALUES(@sp,'/uploads/beauticon-demo-02.svg',1); SET @anh=SCOPE_IDENTITY();
  INSERT dbo.ma_giam_gia(code,mo_ta,loai_giam,gia_tri_giam,gia_tri_don_toi_thieu,giam_toi_da,so_luong,so_luong_da_dung,ngay_bat_dau,ngay_ket_thuc,id_nhan_vien,trang_thai) VALUES('BCDEMO2026-02',N'Voucher giả lập để kiểm thử','PHAN_TRAM',6,100000,50000,50,0,DATEADD(DAY,-15,SYSUTCDATETIME()),DATEADD(DAY,60,SYSUTCDATETIME()),@nv,1); SET @voucher=SCOPE_IDENTITY();
  INSERT dbo.khuyen_mai(ten_khuyen_mai,mo_ta,loai_giam,gia_tri_giam,ngay_bat_dau,ngay_ket_thuc,trang_thai) VALUES(N'Ưu đãi mùa thu 02 (demo)',N'Khuyến mãi giả lập','PHAN_TRAM',6,DATEADD(DAY,-10,SYSUTCDATETIME()),DATEADD(DAY,45,SYSUTCDATETIME()),1); SET @km=SCOPE_IDENTITY();
  INSERT dbo.khuyen_mai_bien_the(id_khuyen_mai,id_bien_the) VALUES(@km,@bt);
  INSERT dbo.gio_hang(id_khach_hang) VALUES(@kh); SET @gio=SCOPE_IDENTITY();
  INSERT dbo.chi_tiet_gio_hang(id_gio_hang,id_bien_the,so_luong) VALUES(@gio,@bt,2); SET @ctgio=SCOPE_IDENTITY();
  INSERT dbo.don_hang(id_khach_hang,id_nhan_vien,id_dia_chi_giao_hang,kenh_ban,trang_thai,ngay_dat,tien_hang,tien_giam,phi_van_chuyen,ten_nguoi_nhan,sdt_nguoi_nhan,dia_chi_giao_snapshot,ghi_chu,ton_kho_da_tru,voucher_da_su_dung,ma_yeu_cau_online) VALUES(@kh,@nv,@dc,'OFFLINE','HOAN_THANH',DATEADD(DAY,-2,SYSUTCDATETIME()),161000,0,0,N'Trần Ngọc Hà','0009760300',N'Số 19, Đường Hoa Mai (giả lập)',N'Đơn hàng minh họa - không giao thực tế',1,0,NULL); SET @dh=SCOPE_IDENTITY();
  INSERT dbo.don_hang_chi_tiet(id_don_hang,id_bien_the,so_luong,gia_ban) VALUES(@dh,@bt,1,161000); SET @ctdh=SCOPE_IDENTITY();
  INSERT dbo.danh_gia(id_dh_chi_tiet,diem,binh_luan) VALUES(@ctdh,5,N'Màu sắc hài hòa, dễ phối đồ. (đánh giá giả lập)'); SET @dg=SCOPE_IDENTITY();
  INSERT dbo.thanh_toan(id_don_hang,phuong_thuc,loai_giao_dich,so_tien,trang_thai,ma_giao_dich,ngay_thanh_toan) VALUES(@dh,'TIEN_MAT','THANH_TOAN',161000,'THANH_CONG','BC-DEMO-PAY-02',DATEADD(DAY,-1,SYSUTCDATETIME())); SET @tt=SCOPE_IDENTITY();
  INSERT dbo.lich_su_don_hang(id_don_hang,id_nhan_vien,su_kien,trang_thai_cu,trang_thai_moi,ghi_chu) VALUES(@dh,@nv,'CHUYEN_TRANG_THAI','HOAN_THANH','HOAN_THANH',N'Nhật ký tạo bởi dữ liệu giả lập'); SET @ls=SCOPE_IDENTITY();
 END
 IF @i=2 INSERT @ids(n,loai,brand,ncc,chatlieu,mau,sizeid,nv,kh,dc,sp,bt,voucher,km,gio,ctgio,dh,ctdh,dg,tt,ls,anh) VALUES(@i,@loai,@brand,@ncc,@chatlieu,@mau,@sizeid,@nv,@kh,@dc,@sp,@bt,@voucher,@km,@gio,@ctgio,@dh,@ctdh,@dg,@tt,@ls,@anh);
 IF @i=3 BEGIN
  INSERT dbo.loai_san_pham(ten_loai,mo_ta) VALUES(N'Vòng tay đá',N'Phân loại vòng tay nữ – bộ dữ liệu minh họa'); SET @loai=SCOPE_IDENTITY();
  INSERT dbo.thuong_hieu(ten_thuong_hieu,quoc_gia,mo_ta) VALUES(N'Mira Accessories',N'Việt Nam',N'Thương hiệu minh họa, không đại diện đơn vị thực'); SET @brand=SCOPE_IDENTITY();
  INSERT dbo.nha_cung_cap(ten_nha_cung_cap,sdt,email,dia_chi) VALUES(N'Xưởng phụ kiện Hương Mây', '0000315729', 'nhacungcap03@example.invalid', N'TP. Hồ Chí Minh (địa chỉ minh họa)'); SET @ncc=SCOPE_IDENTITY();
  INSERT dbo.chat_lieu(ten_chat_lieu,mo_ta) VALUES(N'Đá thạch anh',N'Chất liệu tham khảo cho dữ liệu thử nghiệm'); SET @chatlieu=SCOPE_IDENTITY();
  INSERT dbo.bien_the_mau_sac(ten_mau_sac,ma_mau,mo_ta) VALUES(N'Hồng phấn','#EAB6C6',N'Màu minh họa'); SET @mau=SCOPE_IDENTITY();
  INSERT dbo.bien_the_kich_thuoc(ten_kich_thuoc,mo_ta) VALUES(N'15 cm',N'Chu vi cổ tay tham khảo'); SET @sizeid=SCOPE_IDENTITY();
  INSERT dbo.nhan_vien(ho_ten,email,mat_khau_hash,chuc_danh,trang_thai) VALUES(N'Vũ Thanh Mai','demo.nv03@example.invalid','DEMO_DISABLED_NO_LOGIN',N'NHAN_VIEN',0); SET @nv=SCOPE_IDENTITY();
  INSERT dbo.khach_hang(ho_ten,email,sdt,ngay_sinh,mat_khau_hash,trang_thai) VALUES(N'Lê Phương Thảo','demo.kh03@example.invalid','0000315729',DATEADD(YEAR,-(22+2),CAST(SYSUTCDATETIME() AS DATE)),NULL,1); SET @kh=SCOPE_IDENTITY();
  INSERT dbo.dia_chi_giao_hang(id_khach_hang,ten_nguoi_nhan,sdt,dia_chi_chi_tiet,mac_dinh) VALUES(@kh,N'Lê Phương Thảo','0000315729',N'Số 26, Đường Hoa Mai, TP. Hồ Chí Minh (giả lập)',1); SET @dc=SCOPE_IDENTITY();
  INSERT dbo.san_pham(id_loai,id_thuong_hieu,id_nha_cung_cap,id_chat_lieu,ten_san_pham,trong_luong_gram,mo_ta,ngay_san_xuat) VALUES(@loai,@brand,@ncc,@chatlieu,N'Vòng tay Hạ Vàng',10.5,N'Thiết kế thanh lịch, phù hợp làm quà tặng. Nội dung và thông số sản phẩm chỉ để demo.',DATEADD(DAY,-40,CAST(SYSUTCDATETIME() AS DATE))); SET @sp=SCOPE_IDENTITY();
  INSERT dbo.san_pham_bien_the(id_san_pham,id_mau_sac,id_kich_thuoc,gia_ban,so_luong_ton) VALUES(@sp,@mau,@sizeid,193000,14); SET @bt=SCOPE_IDENTITY();
  INSERT dbo.hinh_anh_sp(id_san_pham,duong_dan_anh,anh_chinh) VALUES(@sp,'/uploads/beauticon-demo-03.svg',1); SET @anh=SCOPE_IDENTITY();
  INSERT dbo.ma_giam_gia(code,mo_ta,loai_giam,gia_tri_giam,gia_tri_don_toi_thieu,giam_toi_da,so_luong,so_luong_da_dung,ngay_bat_dau,ngay_ket_thuc,id_nhan_vien,trang_thai) VALUES('BCDEMO2026-03',N'Voucher giả lập để kiểm thử','PHAN_TRAM',7,100000,50000,50,0,DATEADD(DAY,-15,SYSUTCDATETIME()),DATEADD(DAY,60,SYSUTCDATETIME()),@nv,1); SET @voucher=SCOPE_IDENTITY();
  INSERT dbo.khuyen_mai(ten_khuyen_mai,mo_ta,loai_giam,gia_tri_giam,ngay_bat_dau,ngay_ket_thuc,trang_thai) VALUES(N'Ưu đãi mùa thu 03 (demo)',N'Khuyến mãi giả lập','PHAN_TRAM',7,DATEADD(DAY,-10,SYSUTCDATETIME()),DATEADD(DAY,45,SYSUTCDATETIME()),1); SET @km=SCOPE_IDENTITY();
  INSERT dbo.khuyen_mai_bien_the(id_khuyen_mai,id_bien_the) VALUES(@km,@bt);
  INSERT dbo.gio_hang(id_khach_hang) VALUES(@kh); SET @gio=SCOPE_IDENTITY();
  INSERT dbo.chi_tiet_gio_hang(id_gio_hang,id_bien_the,so_luong) VALUES(@gio,@bt,3); SET @ctgio=SCOPE_IDENTITY();
  INSERT dbo.don_hang(id_khach_hang,id_nhan_vien,id_dia_chi_giao_hang,kenh_ban,trang_thai,ngay_dat,tien_hang,tien_giam,phi_van_chuyen,ten_nguoi_nhan,sdt_nguoi_nhan,dia_chi_giao_snapshot,ghi_chu,ton_kho_da_tru,voucher_da_su_dung,ma_yeu_cau_online) VALUES(@kh,@nv,@dc,'ONLINE','HOAN_THANH',DATEADD(DAY,-3,SYSUTCDATETIME()),193000,0,0,N'Lê Phương Thảo','0000315729',N'Số 26, Đường Hoa Mai (giả lập)',N'Đơn hàng minh họa - không giao thực tế',1,0,'bc-demo-online-03'); SET @dh=SCOPE_IDENTITY();
  INSERT dbo.don_hang_chi_tiet(id_don_hang,id_bien_the,so_luong,gia_ban) VALUES(@dh,@bt,1,193000); SET @ctdh=SCOPE_IDENTITY();
  INSERT dbo.danh_gia(id_dh_chi_tiet,diem,binh_luan) VALUES(@ctdh,4,N'Đóng gói gọn gàng và đẹp mắt. (đánh giá giả lập)'); SET @dg=SCOPE_IDENTITY();
  INSERT dbo.thanh_toan(id_don_hang,phuong_thuc,loai_giao_dich,so_tien,trang_thai,ma_giao_dich,ngay_thanh_toan) VALUES(@dh,'CHUYEN_KHOAN','THANH_TOAN',193000,'THANH_CONG','BC-DEMO-PAY-03',DATEADD(DAY,-1,SYSUTCDATETIME())); SET @tt=SCOPE_IDENTITY();
  INSERT dbo.lich_su_don_hang(id_don_hang,id_nhan_vien,su_kien,trang_thai_cu,trang_thai_moi,ghi_chu) VALUES(@dh,@nv,'CHUYEN_TRANG_THAI','HOAN_THANH','HOAN_THANH',N'Nhật ký tạo bởi dữ liệu giả lập'); SET @ls=SCOPE_IDENTITY();
 END
 IF @i=3 INSERT @ids(n,loai,brand,ncc,chatlieu,mau,sizeid,nv,kh,dc,sp,bt,voucher,km,gio,ctgio,dh,ctdh,dg,tt,ls,anh) VALUES(@i,@loai,@brand,@ncc,@chatlieu,@mau,@sizeid,@nv,@kh,@dc,@sp,@bt,@voucher,@km,@gio,@ctgio,@dh,@ctdh,@dg,@tt,@ls,@anh);
 IF @i=4 BEGIN
  INSERT dbo.loai_san_pham(ten_loai,mo_ta) VALUES(N'Vòng tay ngọc trai',N'Phân loại vòng tay nữ – bộ dữ liệu minh họa'); SET @loai=SCOPE_IDENTITY();
  INSERT dbo.thuong_hieu(ten_thuong_hieu,quoc_gia,mo_ta) VALUES(N'Élan Charm',N'Việt Nam',N'Thương hiệu minh họa, không đại diện đơn vị thực'); SET @brand=SCOPE_IDENTITY();
  INSERT dbo.nha_cung_cap(ten_nha_cung_cap,sdt,email,dia_chi) VALUES(N'Xưởng phụ kiện Hạ Vy', '0000150234', 'nhacungcap04@example.invalid', N'Hải Phòng (địa chỉ minh họa)'); SET @ncc=SCOPE_IDENTITY();
  INSERT dbo.chat_lieu(ten_chat_lieu,mo_ta) VALUES(N'Ngọc trai nhân tạo',N'Chất liệu tham khảo cho dữ liệu thử nghiệm'); SET @chatlieu=SCOPE_IDENTITY();
  INSERT dbo.bien_the_mau_sac(ten_mau_sac,ma_mau,mo_ta) VALUES(N'Trắng ngọc trai','#F3F0E9',N'Màu minh họa'); SET @mau=SCOPE_IDENTITY();
  INSERT dbo.bien_the_kich_thuoc(ten_kich_thuoc,mo_ta) VALUES(N'16 cm',N'Chu vi cổ tay tham khảo'); SET @sizeid=SCOPE_IDENTITY();
  INSERT dbo.nhan_vien(ho_ten,email,mat_khau_hash,chuc_danh,trang_thai) VALUES(N'Bùi Quỳnh Anh','demo.nv04@example.invalid','DEMO_DISABLED_NO_LOGIN',N'NHAN_VIEN',0); SET @nv=SCOPE_IDENTITY();
  INSERT dbo.khach_hang(ho_ten,email,sdt,ngay_sinh,mat_khau_hash,trang_thai) VALUES(N'Phạm Gia Hân','demo.kh04@example.invalid','0000150234',DATEADD(YEAR,-(22+3),CAST(SYSUTCDATETIME() AS DATE)),NULL,1); SET @kh=SCOPE_IDENTITY();
  INSERT dbo.dia_chi_giao_hang(id_khach_hang,ten_nguoi_nhan,sdt,dia_chi_chi_tiet,mac_dinh) VALUES(@kh,N'Phạm Gia Hân','0000150234',N'Số 33, Đường Hoa Mai, Hải Phòng (giả lập)',1); SET @dc=SCOPE_IDENTITY();
  INSERT dbo.san_pham(id_loai,id_thuong_hieu,id_nha_cung_cap,id_chat_lieu,ten_san_pham,trong_luong_gram,mo_ta,ngay_san_xuat) VALUES(@loai,@brand,@ncc,@chatlieu,N'Vòng tay Nắng Hồng',11.75,N'Thiết kế thanh lịch, phù hợp làm quà tặng. Nội dung và thông số sản phẩm chỉ để demo.',DATEADD(DAY,-45,CAST(SYSUTCDATETIME() AS DATE))); SET @sp=SCOPE_IDENTITY();
  INSERT dbo.san_pham_bien_the(id_san_pham,id_mau_sac,id_kich_thuoc,gia_ban,so_luong_ton) VALUES(@sp,@mau,@sizeid,225000,18); SET @bt=SCOPE_IDENTITY();
  INSERT dbo.hinh_anh_sp(id_san_pham,duong_dan_anh,anh_chinh) VALUES(@sp,'/uploads/beauticon-demo-04.svg',1); SET @anh=SCOPE_IDENTITY();
  INSERT dbo.ma_giam_gia(code,mo_ta,loai_giam,gia_tri_giam,gia_tri_don_toi_thieu,giam_toi_da,so_luong,so_luong_da_dung,ngay_bat_dau,ngay_ket_thuc,id_nhan_vien,trang_thai) VALUES('BCDEMO2026-04',N'Voucher giả lập để kiểm thử','PHAN_TRAM',8,100000,50000,50,0,DATEADD(DAY,-15,SYSUTCDATETIME()),DATEADD(DAY,60,SYSUTCDATETIME()),@nv,1); SET @voucher=SCOPE_IDENTITY();
  INSERT dbo.khuyen_mai(ten_khuyen_mai,mo_ta,loai_giam,gia_tri_giam,ngay_bat_dau,ngay_ket_thuc,trang_thai) VALUES(N'Ưu đãi mùa thu 04 (demo)',N'Khuyến mãi giả lập','PHAN_TRAM',8,DATEADD(DAY,-10,SYSUTCDATETIME()),DATEADD(DAY,45,SYSUTCDATETIME()),1); SET @km=SCOPE_IDENTITY();
  INSERT dbo.khuyen_mai_bien_the(id_khuyen_mai,id_bien_the) VALUES(@km,@bt);
  INSERT dbo.gio_hang(id_khach_hang) VALUES(@kh); SET @gio=SCOPE_IDENTITY();
  INSERT dbo.chi_tiet_gio_hang(id_gio_hang,id_bien_the,so_luong) VALUES(@gio,@bt,1); SET @ctgio=SCOPE_IDENTITY();
  INSERT dbo.don_hang(id_khach_hang,id_nhan_vien,id_dia_chi_giao_hang,kenh_ban,trang_thai,ngay_dat,tien_hang,tien_giam,phi_van_chuyen,ten_nguoi_nhan,sdt_nguoi_nhan,dia_chi_giao_snapshot,ghi_chu,ton_kho_da_tru,voucher_da_su_dung,ma_yeu_cau_online) VALUES(@kh,@nv,@dc,'OFFLINE','HOAN_THANH',DATEADD(DAY,-4,SYSUTCDATETIME()),225000,0,0,N'Phạm Gia Hân','0000150234',N'Số 33, Đường Hoa Mai (giả lập)',N'Đơn hàng minh họa - không giao thực tế',1,0,NULL); SET @dh=SCOPE_IDENTITY();
  INSERT dbo.don_hang_chi_tiet(id_don_hang,id_bien_the,so_luong,gia_ban) VALUES(@dh,@bt,1,225000); SET @ctdh=SCOPE_IDENTITY();
  INSERT dbo.danh_gia(id_dh_chi_tiet,diem,binh_luan) VALUES(@ctdh,5,N'Kiểu dáng phù hợp sử dụng hằng ngày. (đánh giá giả lập)'); SET @dg=SCOPE_IDENTITY();
  INSERT dbo.thanh_toan(id_don_hang,phuong_thuc,loai_giao_dich,so_tien,trang_thai,ma_giao_dich,ngay_thanh_toan) VALUES(@dh,'TIEN_MAT','THANH_TOAN',225000,'THANH_CONG','BC-DEMO-PAY-04',DATEADD(DAY,-1,SYSUTCDATETIME())); SET @tt=SCOPE_IDENTITY();
  INSERT dbo.lich_su_don_hang(id_don_hang,id_nhan_vien,su_kien,trang_thai_cu,trang_thai_moi,ghi_chu) VALUES(@dh,@nv,'CHUYEN_TRANG_THAI','HOAN_THANH','HOAN_THANH',N'Nhật ký tạo bởi dữ liệu giả lập'); SET @ls=SCOPE_IDENTITY();
 END
 IF @i=4 INSERT @ids(n,loai,brand,ncc,chatlieu,mau,sizeid,nv,kh,dc,sp,bt,voucher,km,gio,ctgio,dh,ctdh,dg,tt,ls,anh) VALUES(@i,@loai,@brand,@ncc,@chatlieu,@mau,@sizeid,@nv,@kh,@dc,@sp,@bt,@voucher,@km,@gio,@ctgio,@dh,@ctdh,@dg,@tt,@ls,@anh);
 IF @i=5 BEGIN
  INSERT dbo.loai_san_pham(ten_loai,mo_ta) VALUES(N'Vòng tay dây da',N'Phân loại vòng tay nữ – bộ dữ liệu minh họa'); SET @loai=SCOPE_IDENTITY();
  INSERT dbo.thuong_hieu(ten_thuong_hieu,quoc_gia,mo_ta) VALUES(N'Aster Atelier',N'Việt Nam',N'Thương hiệu minh họa, không đại diện đơn vị thực'); SET @brand=SCOPE_IDENTITY();
  INSERT dbo.nha_cung_cap(ten_nha_cung_cap,sdt,email,dia_chi) VALUES(N'Xưởng phụ kiện Minh Châu', '0009837788', 'nhacungcap05@example.invalid', N'Huế (địa chỉ minh họa)'); SET @ncc=SCOPE_IDENTITY();
  INSERT dbo.chat_lieu(ten_chat_lieu,mo_ta) VALUES(N'Dây da',N'Chất liệu tham khảo cho dữ liệu thử nghiệm'); SET @chatlieu=SCOPE_IDENTITY();
  INSERT dbo.bien_the_mau_sac(ten_mau_sac,ma_mau,mo_ta) VALUES(N'Xanh biển','#397DA7',N'Màu minh họa'); SET @mau=SCOPE_IDENTITY();
  INSERT dbo.bien_the_kich_thuoc(ten_kich_thuoc,mo_ta) VALUES(N'17 cm',N'Chu vi cổ tay tham khảo'); SET @sizeid=SCOPE_IDENTITY();
  INSERT dbo.nhan_vien(ho_ten,email,mat_khau_hash,chuc_danh,trang_thai) VALUES(N'Hoàng Bảo Trâm','demo.nv05@example.invalid','DEMO_DISABLED_NO_LOGIN',N'NHAN_VIEN',0); SET @nv=SCOPE_IDENTITY();
  INSERT dbo.khach_hang(ho_ten,email,sdt,ngay_sinh,mat_khau_hash,trang_thai) VALUES(N'Đỗ Minh Châu','demo.kh05@example.invalid','0009837788',DATEADD(YEAR,-(22+4),CAST(SYSUTCDATETIME() AS DATE)),NULL,1); SET @kh=SCOPE_IDENTITY();
  INSERT dbo.dia_chi_giao_hang(id_khach_hang,ten_nguoi_nhan,sdt,dia_chi_chi_tiet,mac_dinh) VALUES(@kh,N'Đỗ Minh Châu','0009837788',N'Số 40, Đường Hoa Mai, Huế (giả lập)',1); SET @dc=SCOPE_IDENTITY();
  INSERT dbo.san_pham(id_loai,id_thuong_hieu,id_nha_cung_cap,id_chat_lieu,ten_san_pham,trong_luong_gram,mo_ta,ngay_san_xuat) VALUES(@loai,@brand,@ncc,@chatlieu,N'Vòng tay Ngọc Lam',13.0,N'Thiết kế thanh lịch, phù hợp làm quà tặng. Nội dung và thông số sản phẩm chỉ để demo.',DATEADD(DAY,-50,CAST(SYSUTCDATETIME() AS DATE))); SET @sp=SCOPE_IDENTITY();
  INSERT dbo.san_pham_bien_the(id_san_pham,id_mau_sac,id_kich_thuoc,gia_ban,so_luong_ton) VALUES(@sp,@mau,@sizeid,257000,22); SET @bt=SCOPE_IDENTITY();
  INSERT dbo.hinh_anh_sp(id_san_pham,duong_dan_anh,anh_chinh) VALUES(@sp,'/uploads/beauticon-demo-05.svg',1); SET @anh=SCOPE_IDENTITY();
  INSERT dbo.ma_giam_gia(code,mo_ta,loai_giam,gia_tri_giam,gia_tri_don_toi_thieu,giam_toi_da,so_luong,so_luong_da_dung,ngay_bat_dau,ngay_ket_thuc,id_nhan_vien,trang_thai) VALUES('BCDEMO2026-05',N'Voucher giả lập để kiểm thử','PHAN_TRAM',9,100000,50000,50,0,DATEADD(DAY,-15,SYSUTCDATETIME()),DATEADD(DAY,60,SYSUTCDATETIME()),@nv,1); SET @voucher=SCOPE_IDENTITY();
  INSERT dbo.khuyen_mai(ten_khuyen_mai,mo_ta,loai_giam,gia_tri_giam,ngay_bat_dau,ngay_ket_thuc,trang_thai) VALUES(N'Ưu đãi mùa thu 05 (demo)',N'Khuyến mãi giả lập','PHAN_TRAM',9,DATEADD(DAY,-10,SYSUTCDATETIME()),DATEADD(DAY,45,SYSUTCDATETIME()),1); SET @km=SCOPE_IDENTITY();
  INSERT dbo.khuyen_mai_bien_the(id_khuyen_mai,id_bien_the) VALUES(@km,@bt);
  INSERT dbo.gio_hang(id_khach_hang) VALUES(@kh); SET @gio=SCOPE_IDENTITY();
  INSERT dbo.chi_tiet_gio_hang(id_gio_hang,id_bien_the,so_luong) VALUES(@gio,@bt,2); SET @ctgio=SCOPE_IDENTITY();
  INSERT dbo.don_hang(id_khach_hang,id_nhan_vien,id_dia_chi_giao_hang,kenh_ban,trang_thai,ngay_dat,tien_hang,tien_giam,phi_van_chuyen,ten_nguoi_nhan,sdt_nguoi_nhan,dia_chi_giao_snapshot,ghi_chu,ton_kho_da_tru,voucher_da_su_dung,ma_yeu_cau_online) VALUES(@kh,@nv,@dc,'ONLINE','HOAN_THANH',DATEADD(DAY,-5,SYSUTCDATETIME()),257000,0,0,N'Đỗ Minh Châu','0009837788',N'Số 40, Đường Hoa Mai (giả lập)',N'Đơn hàng minh họa - không giao thực tế',1,0,'bc-demo-online-05'); SET @dh=SCOPE_IDENTITY();
  INSERT dbo.don_hang_chi_tiet(id_don_hang,id_bien_the,so_luong,gia_ban) VALUES(@dh,@bt,1,257000); SET @ctdh=SCOPE_IDENTITY();
  INSERT dbo.danh_gia(id_dh_chi_tiet,diem,binh_luan) VALUES(@ctdh,4,N'Chi tiết vòng tay nhìn khá chỉn chu. (đánh giá giả lập)'); SET @dg=SCOPE_IDENTITY();
  INSERT dbo.thanh_toan(id_don_hang,phuong_thuc,loai_giao_dich,so_tien,trang_thai,ma_giao_dich,ngay_thanh_toan) VALUES(@dh,'CHUYEN_KHOAN','THANH_TOAN',257000,'THANH_CONG','BC-DEMO-PAY-05',DATEADD(DAY,-1,SYSUTCDATETIME())); SET @tt=SCOPE_IDENTITY();
  INSERT dbo.lich_su_don_hang(id_don_hang,id_nhan_vien,su_kien,trang_thai_cu,trang_thai_moi,ghi_chu) VALUES(@dh,@nv,'CHUYEN_TRANG_THAI','HOAN_THANH','HOAN_THANH',N'Nhật ký tạo bởi dữ liệu giả lập'); SET @ls=SCOPE_IDENTITY();
 END
 IF @i=5 INSERT @ids(n,loai,brand,ncc,chatlieu,mau,sizeid,nv,kh,dc,sp,bt,voucher,km,gio,ctgio,dh,ctdh,dg,tt,ls,anh) VALUES(@i,@loai,@brand,@ncc,@chatlieu,@mau,@sizeid,@nv,@kh,@dc,@sp,@bt,@voucher,@km,@gio,@ctgio,@dh,@ctdh,@dg,@tt,@ls,@anh);
 IF @i=6 BEGIN
  INSERT dbo.loai_san_pham(ten_loai,mo_ta) VALUES(N'Vòng tay đôi',N'Phân loại vòng tay nữ – bộ dữ liệu minh họa'); SET @loai=SCOPE_IDENTITY();
  INSERT dbo.thuong_hieu(ten_thuong_hieu,quoc_gia,mo_ta) VALUES(N'Mây Jewelry',N'Việt Nam',N'Thương hiệu minh họa, không đại diện đơn vị thực'); SET @brand=SCOPE_IDENTITY();
  INSERT dbo.nha_cung_cap(ten_nha_cung_cap,sdt,email,dia_chi) VALUES(N'Xưởng phụ kiện Trúc Mai', '0005848572', 'nhacungcap06@example.invalid', N'Cần Thơ (địa chỉ minh họa)'); SET @ncc=SCOPE_IDENTITY();
  INSERT dbo.chat_lieu(ten_chat_lieu,mo_ta) VALUES(N'Hợp kim mạ vàng',N'Chất liệu tham khảo cho dữ liệu thử nghiệm'); SET @chatlieu=SCOPE_IDENTITY();
  INSERT dbo.bien_the_mau_sac(ten_mau_sac,ma_mau,mo_ta) VALUES(N'Tím lavender','#A78ABF',N'Màu minh họa'); SET @mau=SCOPE_IDENTITY();
  INSERT dbo.bien_the_kich_thuoc(ten_kich_thuoc,mo_ta) VALUES(N'18 cm',N'Chu vi cổ tay tham khảo'); SET @sizeid=SCOPE_IDENTITY();
  INSERT dbo.nhan_vien(ho_ten,email,mat_khau_hash,chuc_danh,trang_thai) VALUES(N'Đặng Thùy Dương','demo.nv06@example.invalid','DEMO_DISABLED_NO_LOGIN',N'NHAN_VIEN',0); SET @nv=SCOPE_IDENTITY();
  INSERT dbo.khach_hang(ho_ten,email,sdt,ngay_sinh,mat_khau_hash,trang_thai) VALUES(N'Vũ Thanh Mai','demo.kh06@example.invalid','0005848572',DATEADD(YEAR,-(22+5),CAST(SYSUTCDATETIME() AS DATE)),NULL,1); SET @kh=SCOPE_IDENTITY();
  INSERT dbo.dia_chi_giao_hang(id_khach_hang,ten_nguoi_nhan,sdt,dia_chi_chi_tiet,mac_dinh) VALUES(@kh,N'Vũ Thanh Mai','0005848572',N'Số 47, Đường Hoa Mai, Cần Thơ (giả lập)',1); SET @dc=SCOPE_IDENTITY();
  INSERT dbo.san_pham(id_loai,id_thuong_hieu,id_nha_cung_cap,id_chat_lieu,ten_san_pham,trong_luong_gram,mo_ta,ngay_san_xuat) VALUES(@loai,@brand,@ncc,@chatlieu,N'Vòng tay Dải Ngân Hà',14.25,N'Thiết kế thanh lịch, phù hợp làm quà tặng. Nội dung và thông số sản phẩm chỉ để demo.',DATEADD(DAY,-55,CAST(SYSUTCDATETIME() AS DATE))); SET @sp=SCOPE_IDENTITY();
  INSERT dbo.san_pham_bien_the(id_san_pham,id_mau_sac,id_kich_thuoc,gia_ban,so_luong_ton) VALUES(@sp,@mau,@sizeid,289000,26); SET @bt=SCOPE_IDENTITY();
  INSERT dbo.hinh_anh_sp(id_san_pham,duong_dan_anh,anh_chinh) VALUES(@sp,'/uploads/beauticon-demo-06.svg',1); SET @anh=SCOPE_IDENTITY();
  INSERT dbo.ma_giam_gia(code,mo_ta,loai_giam,gia_tri_giam,gia_tri_don_toi_thieu,giam_toi_da,so_luong,so_luong_da_dung,ngay_bat_dau,ngay_ket_thuc,id_nhan_vien,trang_thai) VALUES('BCDEMO2026-06',N'Voucher giả lập để kiểm thử','PHAN_TRAM',10,100000,50000,50,0,DATEADD(DAY,-15,SYSUTCDATETIME()),DATEADD(DAY,60,SYSUTCDATETIME()),@nv,1); SET @voucher=SCOPE_IDENTITY();
  INSERT dbo.khuyen_mai(ten_khuyen_mai,mo_ta,loai_giam,gia_tri_giam,ngay_bat_dau,ngay_ket_thuc,trang_thai) VALUES(N'Ưu đãi mùa thu 06 (demo)',N'Khuyến mãi giả lập','PHAN_TRAM',10,DATEADD(DAY,-10,SYSUTCDATETIME()),DATEADD(DAY,45,SYSUTCDATETIME()),1); SET @km=SCOPE_IDENTITY();
  INSERT dbo.khuyen_mai_bien_the(id_khuyen_mai,id_bien_the) VALUES(@km,@bt);
  INSERT dbo.gio_hang(id_khach_hang) VALUES(@kh); SET @gio=SCOPE_IDENTITY();
  INSERT dbo.chi_tiet_gio_hang(id_gio_hang,id_bien_the,so_luong) VALUES(@gio,@bt,3); SET @ctgio=SCOPE_IDENTITY();
  INSERT dbo.don_hang(id_khach_hang,id_nhan_vien,id_dia_chi_giao_hang,kenh_ban,trang_thai,ngay_dat,tien_hang,tien_giam,phi_van_chuyen,ten_nguoi_nhan,sdt_nguoi_nhan,dia_chi_giao_snapshot,ghi_chu,ton_kho_da_tru,voucher_da_su_dung,ma_yeu_cau_online) VALUES(@kh,@nv,@dc,'OFFLINE','HOAN_THANH',DATEADD(DAY,-6,SYSUTCDATETIME()),289000,0,0,N'Vũ Thanh Mai','0005848572',N'Số 47, Đường Hoa Mai (giả lập)',N'Đơn hàng minh họa - không giao thực tế',1,0,NULL); SET @dh=SCOPE_IDENTITY();
  INSERT dbo.don_hang_chi_tiet(id_don_hang,id_bien_the,so_luong,gia_ban) VALUES(@dh,@bt,1,289000); SET @ctdh=SCOPE_IDENTITY();
  INSERT dbo.danh_gia(id_dh_chi_tiet,diem,binh_luan) VALUES(@ctdh,5,N'Thiết kế tinh tế, đeo nhẹ tay. (đánh giá giả lập)'); SET @dg=SCOPE_IDENTITY();
  INSERT dbo.thanh_toan(id_don_hang,phuong_thuc,loai_giao_dich,so_tien,trang_thai,ma_giao_dich,ngay_thanh_toan) VALUES(@dh,'TIEN_MAT','THANH_TOAN',289000,'THANH_CONG','BC-DEMO-PAY-06',DATEADD(DAY,-1,SYSUTCDATETIME())); SET @tt=SCOPE_IDENTITY();
  INSERT dbo.lich_su_don_hang(id_don_hang,id_nhan_vien,su_kien,trang_thai_cu,trang_thai_moi,ghi_chu) VALUES(@dh,@nv,'CHUYEN_TRANG_THAI','HOAN_THANH','HOAN_THANH',N'Nhật ký tạo bởi dữ liệu giả lập'); SET @ls=SCOPE_IDENTITY();
 END
 IF @i=6 INSERT @ids(n,loai,brand,ncc,chatlieu,mau,sizeid,nv,kh,dc,sp,bt,voucher,km,gio,ctgio,dh,ctdh,dg,tt,ls,anh) VALUES(@i,@loai,@brand,@ncc,@chatlieu,@mau,@sizeid,@nv,@kh,@dc,@sp,@bt,@voucher,@km,@gio,@ctgio,@dh,@ctdh,@dg,@tt,@ls,@anh);
 IF @i=7 BEGIN
  INSERT dbo.loai_san_pham(ten_loai,mo_ta) VALUES(N'Vòng tay handmade',N'Phân loại vòng tay nữ – bộ dữ liệu minh họa'); SET @loai=SCOPE_IDENTITY();
  INSERT dbo.thuong_hieu(ten_thuong_hieu,quoc_gia,mo_ta) VALUES(N'Serein Design',N'Việt Nam',N'Thương hiệu minh họa, không đại diện đơn vị thực'); SET @brand=SCOPE_IDENTITY();
  INSERT dbo.nha_cung_cap(ten_nha_cung_cap,sdt,email,dia_chi) VALUES(N'Xưởng phụ kiện Thảo Nguyên', '0001166842', 'nhacungcap07@example.invalid', N'Nha Trang (địa chỉ minh họa)'); SET @ncc=SCOPE_IDENTITY();
  INSERT dbo.chat_lieu(ten_chat_lieu,mo_ta) VALUES(N'Đá mắt hổ',N'Chất liệu tham khảo cho dữ liệu thử nghiệm'); SET @chatlieu=SCOPE_IDENTITY();
  INSERT dbo.bien_the_mau_sac(ten_mau_sac,ma_mau,mo_ta) VALUES(N'Đỏ ruby','#A52A40',N'Màu minh họa'); SET @mau=SCOPE_IDENTITY();
  INSERT dbo.bien_the_kich_thuoc(ten_kich_thuoc,mo_ta) VALUES(N'19 cm',N'Chu vi cổ tay tham khảo'); SET @sizeid=SCOPE_IDENTITY();
  INSERT dbo.nhan_vien(ho_ten,email,mat_khau_hash,chuc_danh,trang_thai) VALUES(N'Phan Nhật Vy','demo.nv07@example.invalid','DEMO_DISABLED_NO_LOGIN',N'NHAN_VIEN',0); SET @nv=SCOPE_IDENTITY();
  INSERT dbo.khach_hang(ho_ten,email,sdt,ngay_sinh,mat_khau_hash,trang_thai) VALUES(N'Bùi Quỳnh Anh','demo.kh07@example.invalid','0001166842',DATEADD(YEAR,-(22+6),CAST(SYSUTCDATETIME() AS DATE)),NULL,1); SET @kh=SCOPE_IDENTITY();
  INSERT dbo.dia_chi_giao_hang(id_khach_hang,ten_nguoi_nhan,sdt,dia_chi_chi_tiet,mac_dinh) VALUES(@kh,N'Bùi Quỳnh Anh','0001166842',N'Số 54, Đường Hoa Mai, Nha Trang (giả lập)',1); SET @dc=SCOPE_IDENTITY();
  INSERT dbo.san_pham(id_loai,id_thuong_hieu,id_nha_cung_cap,id_chat_lieu,ten_san_pham,trong_luong_gram,mo_ta,ngay_san_xuat) VALUES(@loai,@brand,@ncc,@chatlieu,N'Vòng tay Hoa Trà',15.5,N'Thiết kế thanh lịch, phù hợp làm quà tặng. Nội dung và thông số sản phẩm chỉ để demo.',DATEADD(DAY,-60,CAST(SYSUTCDATETIME() AS DATE))); SET @sp=SCOPE_IDENTITY();
  INSERT dbo.san_pham_bien_the(id_san_pham,id_mau_sac,id_kich_thuoc,gia_ban,so_luong_ton) VALUES(@sp,@mau,@sizeid,321000,30); SET @bt=SCOPE_IDENTITY();
  INSERT dbo.hinh_anh_sp(id_san_pham,duong_dan_anh,anh_chinh) VALUES(@sp,'/uploads/beauticon-demo-07.svg',1); SET @anh=SCOPE_IDENTITY();
  INSERT dbo.ma_giam_gia(code,mo_ta,loai_giam,gia_tri_giam,gia_tri_don_toi_thieu,giam_toi_da,so_luong,so_luong_da_dung,ngay_bat_dau,ngay_ket_thuc,id_nhan_vien,trang_thai) VALUES('BCDEMO2026-07',N'Voucher giả lập để kiểm thử','PHAN_TRAM',11,100000,50000,50,0,DATEADD(DAY,-15,SYSUTCDATETIME()),DATEADD(DAY,60,SYSUTCDATETIME()),@nv,1); SET @voucher=SCOPE_IDENTITY();
  INSERT dbo.khuyen_mai(ten_khuyen_mai,mo_ta,loai_giam,gia_tri_giam,ngay_bat_dau,ngay_ket_thuc,trang_thai) VALUES(N'Ưu đãi mùa thu 07 (demo)',N'Khuyến mãi giả lập','PHAN_TRAM',11,DATEADD(DAY,-10,SYSUTCDATETIME()),DATEADD(DAY,45,SYSUTCDATETIME()),1); SET @km=SCOPE_IDENTITY();
  INSERT dbo.khuyen_mai_bien_the(id_khuyen_mai,id_bien_the) VALUES(@km,@bt);
  INSERT dbo.gio_hang(id_khach_hang) VALUES(@kh); SET @gio=SCOPE_IDENTITY();
  INSERT dbo.chi_tiet_gio_hang(id_gio_hang,id_bien_the,so_luong) VALUES(@gio,@bt,1); SET @ctgio=SCOPE_IDENTITY();
  INSERT dbo.don_hang(id_khach_hang,id_nhan_vien,id_dia_chi_giao_hang,kenh_ban,trang_thai,ngay_dat,tien_hang,tien_giam,phi_van_chuyen,ten_nguoi_nhan,sdt_nguoi_nhan,dia_chi_giao_snapshot,ghi_chu,ton_kho_da_tru,voucher_da_su_dung,ma_yeu_cau_online) VALUES(@kh,@nv,@dc,'ONLINE','HOAN_THANH',DATEADD(DAY,-7,SYSUTCDATETIME()),321000,0,0,N'Bùi Quỳnh Anh','0001166842',N'Số 54, Đường Hoa Mai (giả lập)',N'Đơn hàng minh họa - không giao thực tế',1,0,'bc-demo-online-07'); SET @dh=SCOPE_IDENTITY();
  INSERT dbo.don_hang_chi_tiet(id_don_hang,id_bien_the,so_luong,gia_ban) VALUES(@dh,@bt,1,321000); SET @ctdh=SCOPE_IDENTITY();
  INSERT dbo.danh_gia(id_dh_chi_tiet,diem,binh_luan) VALUES(@ctdh,4,N'Màu sắc hài hòa, dễ phối đồ. (đánh giá giả lập)'); SET @dg=SCOPE_IDENTITY();
  INSERT dbo.thanh_toan(id_don_hang,phuong_thuc,loai_giao_dich,so_tien,trang_thai,ma_giao_dich,ngay_thanh_toan) VALUES(@dh,'CHUYEN_KHOAN','THANH_TOAN',321000,'THANH_CONG','BC-DEMO-PAY-07',DATEADD(DAY,-1,SYSUTCDATETIME())); SET @tt=SCOPE_IDENTITY();
  INSERT dbo.lich_su_don_hang(id_don_hang,id_nhan_vien,su_kien,trang_thai_cu,trang_thai_moi,ghi_chu) VALUES(@dh,@nv,'CHUYEN_TRANG_THAI','HOAN_THANH','HOAN_THANH',N'Nhật ký tạo bởi dữ liệu giả lập'); SET @ls=SCOPE_IDENTITY();
 END
 IF @i=7 INSERT @ids(n,loai,brand,ncc,chatlieu,mau,sizeid,nv,kh,dc,sp,bt,voucher,km,gio,ctgio,dh,ctdh,dg,tt,ls,anh) VALUES(@i,@loai,@brand,@ncc,@chatlieu,@mau,@sizeid,@nv,@kh,@dc,@sp,@bt,@voucher,@km,@gio,@ctgio,@dh,@ctdh,@dg,@tt,@ls,@anh);
 IF @i=8 BEGIN
  INSERT dbo.loai_san_pham(ten_loai,mo_ta) VALUES(N'Vòng tay tối giản',N'Phân loại vòng tay nữ – bộ dữ liệu minh họa'); SET @loai=SCOPE_IDENTITY();
  INSERT dbo.thuong_hieu(ten_thuong_hieu,quoc_gia,mo_ta) VALUES(N'Nami Beads',N'Việt Nam',N'Thương hiệu minh họa, không đại diện đơn vị thực'); SET @brand=SCOPE_IDENTITY();
  INSERT dbo.nha_cung_cap(ten_nha_cung_cap,sdt,email,dia_chi) VALUES(N'Xưởng phụ kiện Ngọc Anh', '0004777667', 'nhacungcap08@example.invalid', N'Đà Lạt (địa chỉ minh họa)'); SET @ncc=SCOPE_IDENTITY();
  INSERT dbo.chat_lieu(ten_chat_lieu,mo_ta) VALUES(N'Đá mã não',N'Chất liệu tham khảo cho dữ liệu thử nghiệm'); SET @chatlieu=SCOPE_IDENTITY();
  INSERT dbo.bien_the_mau_sac(ten_mau_sac,ma_mau,mo_ta) VALUES(N'Nâu hổ phách','#A9774F',N'Màu minh họa'); SET @mau=SCOPE_IDENTITY();
  INSERT dbo.bien_the_kich_thuoc(ten_kich_thuoc,mo_ta) VALUES(N'20 cm',N'Chu vi cổ tay tham khảo'); SET @sizeid=SCOPE_IDENTITY();
  INSERT dbo.nhan_vien(ho_ten,email,mat_khau_hash,chuc_danh,trang_thai) VALUES(N'Nguyễn Khánh Linh','demo.nv08@example.invalid','DEMO_DISABLED_NO_LOGIN',N'NHAN_VIEN',0); SET @nv=SCOPE_IDENTITY();
  INSERT dbo.khach_hang(ho_ten,email,sdt,ngay_sinh,mat_khau_hash,trang_thai) VALUES(N'Hoàng Bảo Trâm','demo.kh08@example.invalid','0004777667',DATEADD(YEAR,-(22+7),CAST(SYSUTCDATETIME() AS DATE)),NULL,1); SET @kh=SCOPE_IDENTITY();
  INSERT dbo.dia_chi_giao_hang(id_khach_hang,ten_nguoi_nhan,sdt,dia_chi_chi_tiet,mac_dinh) VALUES(@kh,N'Hoàng Bảo Trâm','0004777667',N'Số 61, Đường Hoa Mai, Đà Lạt (giả lập)',1); SET @dc=SCOPE_IDENTITY();
  INSERT dbo.san_pham(id_loai,id_thuong_hieu,id_nha_cung_cap,id_chat_lieu,ten_san_pham,trong_luong_gram,mo_ta,ngay_san_xuat) VALUES(@loai,@brand,@ncc,@chatlieu,N'Vòng tay Biển Xanh',16.75,N'Thiết kế thanh lịch, phù hợp làm quà tặng. Nội dung và thông số sản phẩm chỉ để demo.',DATEADD(DAY,-65,CAST(SYSUTCDATETIME() AS DATE))); SET @sp=SCOPE_IDENTITY();
  INSERT dbo.san_pham_bien_the(id_san_pham,id_mau_sac,id_kich_thuoc,gia_ban,so_luong_ton) VALUES(@sp,@mau,@sizeid,353000,34); SET @bt=SCOPE_IDENTITY();
  INSERT dbo.hinh_anh_sp(id_san_pham,duong_dan_anh,anh_chinh) VALUES(@sp,'/uploads/beauticon-demo-08.svg',1); SET @anh=SCOPE_IDENTITY();
  INSERT dbo.ma_giam_gia(code,mo_ta,loai_giam,gia_tri_giam,gia_tri_don_toi_thieu,giam_toi_da,so_luong,so_luong_da_dung,ngay_bat_dau,ngay_ket_thuc,id_nhan_vien,trang_thai) VALUES('BCDEMO2026-08',N'Voucher giả lập để kiểm thử','PHAN_TRAM',12,100000,50000,50,0,DATEADD(DAY,-15,SYSUTCDATETIME()),DATEADD(DAY,60,SYSUTCDATETIME()),@nv,1); SET @voucher=SCOPE_IDENTITY();
  INSERT dbo.khuyen_mai(ten_khuyen_mai,mo_ta,loai_giam,gia_tri_giam,ngay_bat_dau,ngay_ket_thuc,trang_thai) VALUES(N'Ưu đãi mùa thu 08 (demo)',N'Khuyến mãi giả lập','PHAN_TRAM',12,DATEADD(DAY,-10,SYSUTCDATETIME()),DATEADD(DAY,45,SYSUTCDATETIME()),1); SET @km=SCOPE_IDENTITY();
  INSERT dbo.khuyen_mai_bien_the(id_khuyen_mai,id_bien_the) VALUES(@km,@bt);
  INSERT dbo.gio_hang(id_khach_hang) VALUES(@kh); SET @gio=SCOPE_IDENTITY();
  INSERT dbo.chi_tiet_gio_hang(id_gio_hang,id_bien_the,so_luong) VALUES(@gio,@bt,2); SET @ctgio=SCOPE_IDENTITY();
  INSERT dbo.don_hang(id_khach_hang,id_nhan_vien,id_dia_chi_giao_hang,kenh_ban,trang_thai,ngay_dat,tien_hang,tien_giam,phi_van_chuyen,ten_nguoi_nhan,sdt_nguoi_nhan,dia_chi_giao_snapshot,ghi_chu,ton_kho_da_tru,voucher_da_su_dung,ma_yeu_cau_online) VALUES(@kh,@nv,@dc,'OFFLINE','HOAN_THANH',DATEADD(DAY,-8,SYSUTCDATETIME()),353000,0,0,N'Hoàng Bảo Trâm','0004777667',N'Số 61, Đường Hoa Mai (giả lập)',N'Đơn hàng minh họa - không giao thực tế',1,0,NULL); SET @dh=SCOPE_IDENTITY();
  INSERT dbo.don_hang_chi_tiet(id_don_hang,id_bien_the,so_luong,gia_ban) VALUES(@dh,@bt,1,353000); SET @ctdh=SCOPE_IDENTITY();
  INSERT dbo.danh_gia(id_dh_chi_tiet,diem,binh_luan) VALUES(@ctdh,5,N'Đóng gói gọn gàng và đẹp mắt. (đánh giá giả lập)'); SET @dg=SCOPE_IDENTITY();
  INSERT dbo.thanh_toan(id_don_hang,phuong_thuc,loai_giao_dich,so_tien,trang_thai,ma_giao_dich,ngay_thanh_toan) VALUES(@dh,'TIEN_MAT','THANH_TOAN',353000,'THANH_CONG','BC-DEMO-PAY-08',DATEADD(DAY,-1,SYSUTCDATETIME())); SET @tt=SCOPE_IDENTITY();
  INSERT dbo.lich_su_don_hang(id_don_hang,id_nhan_vien,su_kien,trang_thai_cu,trang_thai_moi,ghi_chu) VALUES(@dh,@nv,'CHUYEN_TRANG_THAI','HOAN_THANH','HOAN_THANH',N'Nhật ký tạo bởi dữ liệu giả lập'); SET @ls=SCOPE_IDENTITY();
 END
 IF @i=8 INSERT @ids(n,loai,brand,ncc,chatlieu,mau,sizeid,nv,kh,dc,sp,bt,voucher,km,gio,ctgio,dh,ctdh,dg,tt,ls,anh) VALUES(@i,@loai,@brand,@ncc,@chatlieu,@mau,@sizeid,@nv,@kh,@dc,@sp,@bt,@voucher,@km,@gio,@ctgio,@dh,@ctdh,@dg,@tt,@ls,@anh);
 IF @i=9 BEGIN
  INSERT dbo.loai_san_pham(ten_loai,mo_ta) VALUES(N'Vòng tay phong thủy',N'Phân loại vòng tay nữ – bộ dữ liệu minh họa'); SET @loai=SCOPE_IDENTITY();
  INSERT dbo.thuong_hieu(ten_thuong_hieu,quoc_gia,mo_ta) VALUES(N'Roselle Handmade',N'Việt Nam',N'Thương hiệu minh họa, không đại diện đơn vị thực'); SET @brand=SCOPE_IDENTITY();
  INSERT dbo.nha_cung_cap(ten_nha_cung_cap,sdt,email,dia_chi) VALUES(N'Xưởng phụ kiện Hoài An', '0005748140', 'nhacungcap09@example.invalid', N'Vũng Tàu (địa chỉ minh họa)'); SET @ncc=SCOPE_IDENTITY();
  INSERT dbo.chat_lieu(ten_chat_lieu,mo_ta) VALUES(N'Dây dù cao cấp',N'Chất liệu tham khảo cho dữ liệu thử nghiệm'); SET @chatlieu=SCOPE_IDENTITY();
  INSERT dbo.bien_the_mau_sac(ten_mau_sac,ma_mau,mo_ta) VALUES(N'Xanh ngọc','#6CB7A4',N'Màu minh họa'); SET @mau=SCOPE_IDENTITY();
  INSERT dbo.bien_the_kich_thuoc(ten_kich_thuoc,mo_ta) VALUES(N'21 cm',N'Chu vi cổ tay tham khảo'); SET @sizeid=SCOPE_IDENTITY();
  INSERT dbo.nhan_vien(ho_ten,email,mat_khau_hash,chuc_danh,trang_thai) VALUES(N'Trần Ngọc Hà','demo.nv09@example.invalid','DEMO_DISABLED_NO_LOGIN',N'NHAN_VIEN',0); SET @nv=SCOPE_IDENTITY();
  INSERT dbo.khach_hang(ho_ten,email,sdt,ngay_sinh,mat_khau_hash,trang_thai) VALUES(N'Đặng Thùy Dương','demo.kh09@example.invalid','0005748140',DATEADD(YEAR,-(22+0),CAST(SYSUTCDATETIME() AS DATE)),NULL,1); SET @kh=SCOPE_IDENTITY();
  INSERT dbo.dia_chi_giao_hang(id_khach_hang,ten_nguoi_nhan,sdt,dia_chi_chi_tiet,mac_dinh) VALUES(@kh,N'Đặng Thùy Dương','0005748140',N'Số 68, Đường Hoa Mai, Vũng Tàu (giả lập)',1); SET @dc=SCOPE_IDENTITY();
  INSERT dbo.san_pham(id_loai,id_thuong_hieu,id_nha_cung_cap,id_chat_lieu,ten_san_pham,trong_luong_gram,mo_ta,ngay_san_xuat) VALUES(@loai,@brand,@ncc,@chatlieu,N'Vòng tay Mộc Miên',18.0,N'Thiết kế thanh lịch, phù hợp làm quà tặng. Nội dung và thông số sản phẩm chỉ để demo.',DATEADD(DAY,-70,CAST(SYSUTCDATETIME() AS DATE))); SET @sp=SCOPE_IDENTITY();
  INSERT dbo.san_pham_bien_the(id_san_pham,id_mau_sac,id_kich_thuoc,gia_ban,so_luong_ton) VALUES(@sp,@mau,@sizeid,385000,38); SET @bt=SCOPE_IDENTITY();
  INSERT dbo.hinh_anh_sp(id_san_pham,duong_dan_anh,anh_chinh) VALUES(@sp,'/uploads/beauticon-demo-09.svg',1); SET @anh=SCOPE_IDENTITY();
  INSERT dbo.ma_giam_gia(code,mo_ta,loai_giam,gia_tri_giam,gia_tri_don_toi_thieu,giam_toi_da,so_luong,so_luong_da_dung,ngay_bat_dau,ngay_ket_thuc,id_nhan_vien,trang_thai) VALUES('BCDEMO2026-09',N'Voucher giả lập để kiểm thử','PHAN_TRAM',13,100000,50000,50,0,DATEADD(DAY,-15,SYSUTCDATETIME()),DATEADD(DAY,60,SYSUTCDATETIME()),@nv,1); SET @voucher=SCOPE_IDENTITY();
  INSERT dbo.khuyen_mai(ten_khuyen_mai,mo_ta,loai_giam,gia_tri_giam,ngay_bat_dau,ngay_ket_thuc,trang_thai) VALUES(N'Ưu đãi mùa thu 09 (demo)',N'Khuyến mãi giả lập','PHAN_TRAM',13,DATEADD(DAY,-10,SYSUTCDATETIME()),DATEADD(DAY,45,SYSUTCDATETIME()),1); SET @km=SCOPE_IDENTITY();
  INSERT dbo.khuyen_mai_bien_the(id_khuyen_mai,id_bien_the) VALUES(@km,@bt);
  INSERT dbo.gio_hang(id_khach_hang) VALUES(@kh); SET @gio=SCOPE_IDENTITY();
  INSERT dbo.chi_tiet_gio_hang(id_gio_hang,id_bien_the,so_luong) VALUES(@gio,@bt,3); SET @ctgio=SCOPE_IDENTITY();
  INSERT dbo.don_hang(id_khach_hang,id_nhan_vien,id_dia_chi_giao_hang,kenh_ban,trang_thai,ngay_dat,tien_hang,tien_giam,phi_van_chuyen,ten_nguoi_nhan,sdt_nguoi_nhan,dia_chi_giao_snapshot,ghi_chu,ton_kho_da_tru,voucher_da_su_dung,ma_yeu_cau_online) VALUES(@kh,@nv,@dc,'ONLINE','HOAN_THANH',DATEADD(DAY,-9,SYSUTCDATETIME()),385000,0,0,N'Đặng Thùy Dương','0005748140',N'Số 68, Đường Hoa Mai (giả lập)',N'Đơn hàng minh họa - không giao thực tế',1,0,'bc-demo-online-09'); SET @dh=SCOPE_IDENTITY();
  INSERT dbo.don_hang_chi_tiet(id_don_hang,id_bien_the,so_luong,gia_ban) VALUES(@dh,@bt,1,385000); SET @ctdh=SCOPE_IDENTITY();
  INSERT dbo.danh_gia(id_dh_chi_tiet,diem,binh_luan) VALUES(@ctdh,4,N'Kiểu dáng phù hợp sử dụng hằng ngày. (đánh giá giả lập)'); SET @dg=SCOPE_IDENTITY();
  INSERT dbo.thanh_toan(id_don_hang,phuong_thuc,loai_giao_dich,so_tien,trang_thai,ma_giao_dich,ngay_thanh_toan) VALUES(@dh,'CHUYEN_KHOAN','THANH_TOAN',385000,'THANH_CONG','BC-DEMO-PAY-09',DATEADD(DAY,-1,SYSUTCDATETIME())); SET @tt=SCOPE_IDENTITY();
  INSERT dbo.lich_su_don_hang(id_don_hang,id_nhan_vien,su_kien,trang_thai_cu,trang_thai_moi,ghi_chu) VALUES(@dh,@nv,'HUY_DON','HOAN_THANH','HOAN_THANH',N'Nhật ký tạo bởi dữ liệu giả lập'); SET @ls=SCOPE_IDENTITY();
 END
 IF @i=9 INSERT @ids(n,loai,brand,ncc,chatlieu,mau,sizeid,nv,kh,dc,sp,bt,voucher,km,gio,ctgio,dh,ctdh,dg,tt,ls,anh) VALUES(@i,@loai,@brand,@ncc,@chatlieu,@mau,@sizeid,@nv,@kh,@dc,@sp,@bt,@voucher,@km,@gio,@ctgio,@dh,@ctdh,@dg,@tt,@ls,@anh);
 IF @i=10 BEGIN
  INSERT dbo.loai_san_pham(ten_loai,mo_ta) VALUES(N'Vòng tay thời trang',N'Phân loại vòng tay nữ – bộ dữ liệu minh họa'); SET @loai=SCOPE_IDENTITY();
  INSERT dbo.thuong_hieu(ten_thuong_hieu,quoc_gia,mo_ta) VALUES(N'Soluna',N'Việt Nam',N'Thương hiệu minh họa, không đại diện đơn vị thực'); SET @brand=SCOPE_IDENTITY();
  INSERT dbo.nha_cung_cap(ten_nha_cung_cap,sdt,email,dia_chi) VALUES(N'Xưởng phụ kiện Thanh Vân', '0006053676', 'nhacungcap10@example.invalid', N'Bắc Ninh (địa chỉ minh họa)'); SET @ncc=SCOPE_IDENTITY();
  INSERT dbo.chat_lieu(ten_chat_lieu,mo_ta) VALUES(N'Pha lê',N'Chất liệu tham khảo cho dữ liệu thử nghiệm'); SET @chatlieu=SCOPE_IDENTITY();
  INSERT dbo.bien_the_mau_sac(ten_mau_sac,ma_mau,mo_ta) VALUES(N'Vàng hồng','#B76E79',N'Màu minh họa'); SET @mau=SCOPE_IDENTITY();
  INSERT dbo.bien_the_kich_thuoc(ten_kich_thuoc,mo_ta) VALUES(N'22 cm',N'Chu vi cổ tay tham khảo'); SET @sizeid=SCOPE_IDENTITY();
  INSERT dbo.nhan_vien(ho_ten,email,mat_khau_hash,chuc_danh,trang_thai) VALUES(N'Lê Phương Thảo','demo.nv10@example.invalid','DEMO_DISABLED_NO_LOGIN',N'NHAN_VIEN',0); SET @nv=SCOPE_IDENTITY();
  INSERT dbo.khach_hang(ho_ten,email,sdt,ngay_sinh,mat_khau_hash,trang_thai) VALUES(N'Phan Nhật Vy','demo.kh10@example.invalid','0006053676',DATEADD(YEAR,-(22+1),CAST(SYSUTCDATETIME() AS DATE)),NULL,1); SET @kh=SCOPE_IDENTITY();
  INSERT dbo.dia_chi_giao_hang(id_khach_hang,ten_nguoi_nhan,sdt,dia_chi_chi_tiet,mac_dinh) VALUES(@kh,N'Phan Nhật Vy','0006053676',N'Số 75, Đường Hoa Mai, Bắc Ninh (giả lập)',1); SET @dc=SCOPE_IDENTITY();
  INSERT dbo.san_pham(id_loai,id_thuong_hieu,id_nha_cung_cap,id_chat_lieu,ten_san_pham,trong_luong_gram,mo_ta,ngay_san_xuat) VALUES(@loai,@brand,@ncc,@chatlieu,N'Vòng tay Đêm Trăng',19.25,N'Thiết kế thanh lịch, phù hợp làm quà tặng. Nội dung và thông số sản phẩm chỉ để demo.',DATEADD(DAY,-75,CAST(SYSUTCDATETIME() AS DATE))); SET @sp=SCOPE_IDENTITY();
  INSERT dbo.san_pham_bien_the(id_san_pham,id_mau_sac,id_kich_thuoc,gia_ban,so_luong_ton) VALUES(@sp,@mau,@sizeid,417000,42); SET @bt=SCOPE_IDENTITY();
  INSERT dbo.hinh_anh_sp(id_san_pham,duong_dan_anh,anh_chinh) VALUES(@sp,'/uploads/beauticon-demo-10.svg',1); SET @anh=SCOPE_IDENTITY();
  INSERT dbo.ma_giam_gia(code,mo_ta,loai_giam,gia_tri_giam,gia_tri_don_toi_thieu,giam_toi_da,so_luong,so_luong_da_dung,ngay_bat_dau,ngay_ket_thuc,id_nhan_vien,trang_thai) VALUES('BCDEMO2026-10',N'Voucher giả lập để kiểm thử','PHAN_TRAM',14,100000,50000,50,0,DATEADD(DAY,-15,SYSUTCDATETIME()),DATEADD(DAY,60,SYSUTCDATETIME()),@nv,1); SET @voucher=SCOPE_IDENTITY();
  INSERT dbo.khuyen_mai(ten_khuyen_mai,mo_ta,loai_giam,gia_tri_giam,ngay_bat_dau,ngay_ket_thuc,trang_thai) VALUES(N'Ưu đãi mùa thu 10 (demo)',N'Khuyến mãi giả lập','PHAN_TRAM',14,DATEADD(DAY,-10,SYSUTCDATETIME()),DATEADD(DAY,45,SYSUTCDATETIME()),1); SET @km=SCOPE_IDENTITY();
  INSERT dbo.khuyen_mai_bien_the(id_khuyen_mai,id_bien_the) VALUES(@km,@bt);
  INSERT dbo.gio_hang(id_khach_hang) VALUES(@kh); SET @gio=SCOPE_IDENTITY();
  INSERT dbo.chi_tiet_gio_hang(id_gio_hang,id_bien_the,so_luong) VALUES(@gio,@bt,1); SET @ctgio=SCOPE_IDENTITY();
  INSERT dbo.don_hang(id_khach_hang,id_nhan_vien,id_dia_chi_giao_hang,kenh_ban,trang_thai,ngay_dat,tien_hang,tien_giam,phi_van_chuyen,ten_nguoi_nhan,sdt_nguoi_nhan,dia_chi_giao_snapshot,ghi_chu,ton_kho_da_tru,voucher_da_su_dung,ma_yeu_cau_online) VALUES(@kh,@nv,@dc,'OFFLINE','HOAN_THANH',DATEADD(DAY,-10,SYSUTCDATETIME()),417000,0,0,N'Phan Nhật Vy','0006053676',N'Số 75, Đường Hoa Mai (giả lập)',N'Đơn hàng minh họa - không giao thực tế',1,0,NULL); SET @dh=SCOPE_IDENTITY();
  INSERT dbo.don_hang_chi_tiet(id_don_hang,id_bien_the,so_luong,gia_ban) VALUES(@dh,@bt,1,417000); SET @ctdh=SCOPE_IDENTITY();
  INSERT dbo.danh_gia(id_dh_chi_tiet,diem,binh_luan) VALUES(@ctdh,5,N'Chi tiết vòng tay nhìn khá chỉn chu. (đánh giá giả lập)'); SET @dg=SCOPE_IDENTITY();
  INSERT dbo.thanh_toan(id_don_hang,phuong_thuc,loai_giao_dich,so_tien,trang_thai,ma_giao_dich,ngay_thanh_toan) VALUES(@dh,'TIEN_MAT','THANH_TOAN',417000,'THANH_CONG','BC-DEMO-PAY-10',DATEADD(DAY,-1,SYSUTCDATETIME())); SET @tt=SCOPE_IDENTITY();
  INSERT dbo.lich_su_don_hang(id_don_hang,id_nhan_vien,su_kien,trang_thai_cu,trang_thai_moi,ghi_chu) VALUES(@dh,@nv,'CHUYEN_TRANG_THAI','HOAN_THANH','HOAN_THANH',N'Nhật ký tạo bởi dữ liệu giả lập'); SET @ls=SCOPE_IDENTITY();
 END
 IF @i=10 INSERT @ids(n,loai,brand,ncc,chatlieu,mau,sizeid,nv,kh,dc,sp,bt,voucher,km,gio,ctgio,dh,ctdh,dg,tt,ls,anh) VALUES(@i,@loai,@brand,@ncc,@chatlieu,@mau,@sizeid,@nv,@kh,@dc,@sp,@bt,@voucher,@km,@gio,@ctgio,@dh,@ctdh,@dg,@tt,@ls,@anh);
 SET @i=@i+1;
END
COMMIT TRANSACTION;
PRINT N'Da them 10 ban ghi moi cho 22 bang (bao gom khuyen_mai_bien_the).';
END TRY
BEGIN CATCH
 IF @@TRANCOUNT>0 ROLLBACK TRANSACTION;
 THROW;
END CATCH;
GO
