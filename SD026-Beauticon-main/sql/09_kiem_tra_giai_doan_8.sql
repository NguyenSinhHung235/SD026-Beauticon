-- Read-only acceptance checks for Stage 8 (run in SSMS).
USE VongTayNu;
GO
SELECT 'Customer and cart tables' AS check_name,
       CASE WHEN OBJECT_ID('dbo.khach_hang') IS NOT NULL
             AND OBJECT_ID('dbo.gio_hang') IS NOT NULL
             AND OBJECT_ID('dbo.chi_tiet_gio_hang') IS NOT NULL
             AND OBJECT_ID('dbo.don_hang') IS NOT NULL
             AND OBJECT_ID('dbo.thanh_toan') IS NOT NULL
            THEN 'OK' ELSE 'MISSING' END AS result;
SELECT 'Online request token' AS check_name,
       CASE WHEN COL_LENGTH('dbo.don_hang','ma_yeu_cau_online') IS NULL
            THEN 'MISSING - RUN 08_migrate_giai_doan_8.sql'
            ELSE 'OK' END AS result;
SELECT 'Unique online request index' AS check_name,
       CASE WHEN EXISTS(SELECT 1 FROM sys.indexes
          WHERE object_id=OBJECT_ID('dbo.don_hang') AND name='UX_dh_ma_yeu_cau_online')
            THEN 'OK' ELSE 'MISSING' END AS result;
SELECT TOP(15) d.id_don_hang,d.kenh_ban,d.trang_thai,d.tien_hang,d.tien_giam,
       d.phi_van_chuyen,d.tong_tien,d.ton_kho_da_tru,
       d.ma_yeu_cau_online,d.ngay_dat
FROM dbo.don_hang d WHERE d.kenh_ban='ONLINE' ORDER BY d.id_don_hang DESC;
SELECT TOP(15) t.id_don_hang,t.phuong_thuc,t.loai_giao_dich,t.trang_thai,
       t.so_tien,t.ngay_thanh_toan
FROM dbo.thanh_toan t ORDER BY t.id_thanh_toan DESC;
GO
