/*
  GD6 - CHI KIEM TRA, KHONG SUA DU LIEU
  Chay tren SSMS sau khi da tao/cap nhat DB den giai doan 5.
*/
USE VongTayNu;
GO
SET NOCOUNT ON;
DECLARE @required TABLE (table_name SYSNAME PRIMARY KEY);
INSERT INTO @required (table_name) VALUES
(N'loai_san_pham'),(N'thuong_hieu'),(N'nha_cung_cap'),(N'chat_lieu'),
(N'bien_the_mau_sac'),(N'bien_the_kich_thuoc'),(N'nhan_vien'),(N'khach_hang'),
(N'dia_chi_giao_hang'),(N'san_pham'),(N'hinh_anh_sp'),(N'san_pham_bien_the'),
(N'ma_giam_gia'),(N'khuyen_mai'),(N'khuyen_mai_bien_the'),(N'gio_hang'),
(N'chi_tiet_gio_hang'),(N'don_hang'),(N'don_hang_chi_tiet'),(N'danh_gia'),
(N'thanh_toan'),(N'lich_su_don_hang');

IF EXISTS (SELECT 1 FROM @required WHERE OBJECT_ID(N'dbo.'+table_name,N'U') IS NULL)
    THROW 51000, N'Thieu bang. Hay chay migration dung thu tu GD1 -> GD5.', 1;
IF NOT EXISTS (SELECT 1 FROM sys.key_constraints WHERE name=N'UQ_bien_the' AND type='UQ')
    THROW 51001, N'Thieu rang buoc UNIQUE ket hop cua san_pham_bien_the.', 1;
IF NOT EXISTS (SELECT 1 FROM sys.key_constraints WHERE name=N'UQ_gio_khach' AND type='UQ')
    THROW 51002, N'Thieu rang buoc 1 gio hang / khach hang.', 1;
IF NOT EXISTS (SELECT 1 FROM sys.key_constraints WHERE name=N'UQ_dg_ctdh' AND type='UQ')
    THROW 51003, N'Thieu rang buoc 1 danh gia / chi tiet don hang.', 1;
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name=N'UX_dh_ma_yeu_cau_pos' AND object_id=OBJECT_ID('dbo.don_hang'))
    THROW 51004, N'Thieu chi muc chong thanh toan POS trung lan.', 1;
IF EXISTS (SELECT 1 FROM dbo.san_pham_bien_the WHERE so_luong_ton<0 OR gia_ban<0)
    THROW 51005, N'Du lieu ton kho/gia ban khong hop le.', 1;
IF EXISTS (SELECT 1 FROM dbo.ma_giam_gia WHERE so_luong_da_dung>so_luong OR so_luong_da_dung<0)
    THROW 51006, N'Du lieu so luot voucher khong hop le.', 1;
IF EXISTS (SELECT 1 FROM dbo.don_hang WHERE tong_tien<0)
    THROW 51007, N'Co don hang tong tien am.', 1;

SELECT N'PASS: 22 bang, rang buoc va ton kho/voucher co ban' AS result;
SELECT (SELECT COUNT_BIG(*) FROM dbo.san_pham) AS san_pham,
       (SELECT COUNT_BIG(*) FROM dbo.san_pham_bien_the) AS bien_the,
       (SELECT COUNT_BIG(*) FROM dbo.don_hang) AS don_hang,
       (SELECT COUNT_BIG(*) FROM dbo.thanh_toan WHERE trang_thai='THANH_CONG') AS giao_dich_thanh_cong;
GO
