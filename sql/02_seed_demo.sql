/* DEMO ONLY: chay SAU 01_schema.sql, tren DB chua co san pham. */
USE VongTayNu;
GO
SET XACT_ABORT ON;
BEGIN TRY
    BEGIN TRANSACTION;
    IF EXISTS (SELECT 1 FROM dbo.san_pham)
        THROW 51000, 'Database da co san pham. Khong chay lai seed demo.', 1;

    DECLARE @loai BIGINT, @brand BIGINT, @ncc BIGINT, @material BIGINT;
    DECLARE @silver BIGINT, @gold BIGINT, @pink BIGINT, @free BIGINT, @cm16 BIGINT, @cm18 BIGINT;
    DECLARE @staff BIGINT, @customer1 BIGINT, @customer2 BIGINT, @address BIGINT;
    DECLARE @sp1 BIGINT, @sp2 BIGINT, @sp3 BIGINT, @bt1 BIGINT, @bt2 BIGINT, @bt3 BIGINT, @bt4 BIGINT;
    DECLARE @voucher BIGINT, @promo BIGINT;

    INSERT dbo.loai_san_pham(ten_loai) VALUES (N'Vòng tay');
    SET @loai = SCOPE_IDENTITY();
    INSERT dbo.thuong_hieu(ten_thuong_hieu, quoc_gia) VALUES(N'BeautIcon', N'Việt Nam');
    SET @brand = SCOPE_IDENTITY();
    INSERT dbo.nha_cung_cap(ten_nha_cung_cap) VALUES(N'Nhà cung cấp mẫu');
    SET @ncc = SCOPE_IDENTITY();
    INSERT dbo.chat_lieu(ten_chat_lieu) VALUES(N'Bạc 925');
    SET @material = SCOPE_IDENTITY();

    INSERT dbo.bien_the_mau_sac(ten_mau_sac, ma_mau) VALUES(N'Bạc', '#c0c0c0');
    SET @silver = SCOPE_IDENTITY();
    INSERT dbo.bien_the_mau_sac(ten_mau_sac, ma_mau) VALUES(N'Vàng', '#d4af37');
    SET @gold = SCOPE_IDENTITY();
    INSERT dbo.bien_the_mau_sac(ten_mau_sac, ma_mau) VALUES(N'Hồng', '#ffc0cb');
    SET @pink = SCOPE_IDENTITY();
    INSERT dbo.bien_the_kich_thuoc(ten_kich_thuoc) VALUES(N'Freesize');
    SET @free = SCOPE_IDENTITY();
    INSERT dbo.bien_the_kich_thuoc(ten_kich_thuoc) VALUES(N'16 cm');
    SET @cm16 = SCOPE_IDENTITY();
    INSERT dbo.bien_the_kich_thuoc(ten_kich_thuoc) VALUES(N'18 cm');
    SET @cm18 = SCOPE_IDENTITY();

    INSERT dbo.nhan_vien(ho_ten, email, mat_khau_hash, chuc_danh, trang_thai)
    VALUES(N'Nhân viên demo', 'demo-staff@example.invalid', 'DEMO_DISABLED_ACCOUNT_NO_LOGIN', N'QUAN_LY', 0);
    SET @staff = SCOPE_IDENTITY();
    INSERT dbo.khach_hang(ho_ten, email, sdt)
    VALUES(N'Nguyễn Minh Anh', 'minhanh@example.invalid', '0900000001');
    SET @customer1 = SCOPE_IDENTITY();
    INSERT dbo.khach_hang(ho_ten, email, sdt)
    VALUES(N'Trần Bảo Ngọc', 'baongoc@example.invalid', '0900000002');
    SET @customer2 = SCOPE_IDENTITY();
    INSERT dbo.dia_chi_giao_hang(id_khach_hang, ten_nguoi_nhan, sdt, dia_chi_chi_tiet, mac_dinh)
    VALUES(@customer1, N'Nguyễn Minh Anh', '0900000001', N'Quận Cầu Giấy, Hà Nội', 1);
    SET @address = SCOPE_IDENTITY();

    INSERT dbo.san_pham(id_loai, id_thuong_hieu, id_nha_cung_cap, id_chat_lieu, ten_san_pham)
    VALUES(@loai, @brand, @ncc, @material, N'Vòng tay Ánh Trăng');
    SET @sp1 = SCOPE_IDENTITY();
    INSERT dbo.san_pham(id_loai, id_thuong_hieu, id_nha_cung_cap, ten_san_pham)
    VALUES(@loai, @brand, @ncc, N'Vòng tay Đá Hồng');
    SET @sp2 = SCOPE_IDENTITY();
    INSERT dbo.san_pham(id_loai, id_thuong_hieu, ten_san_pham)
    VALUES(@loai, @brand, N'Vòng tay Charm Trái Tim');
    SET @sp3 = SCOPE_IDENTITY();

    INSERT dbo.san_pham_bien_the(id_san_pham, id_mau_sac, id_kich_thuoc, gia_ban, so_luong_ton)
    VALUES(@sp1, @silver, @cm16, 249000, 28);
    SET @bt1 = SCOPE_IDENTITY();
    INSERT dbo.san_pham_bien_the(id_san_pham, id_mau_sac, id_kich_thuoc, gia_ban, so_luong_ton)
    VALUES(@sp1, @gold, @cm18, 279000, 9);
    SET @bt2 = SCOPE_IDENTITY();
    INSERT dbo.san_pham_bien_the(id_san_pham, id_mau_sac, id_kich_thuoc, gia_ban, so_luong_ton)
    VALUES(@sp2, @pink, @free, 179000, 3);
    SET @bt3 = SCOPE_IDENTITY();
    INSERT dbo.san_pham_bien_the(id_san_pham, id_mau_sac, id_kich_thuoc, gia_ban, so_luong_ton)
    VALUES(@sp3, @gold, @free, 99000, 22);
    SET @bt4 = SCOPE_IDENTITY();

    INSERT dbo.ma_giam_gia(code, loai_giam, gia_tri_giam, gia_tri_don_toi_thieu,
                            so_luong, so_luong_da_dung, ngay_bat_dau, ngay_ket_thuc, id_nhan_vien)
    VALUES('WELCOME10', 'PHAN_TRAM', 10, 100000, 100, 1,
           DATEADD(DAY,-30,SYSUTCDATETIME()), DATEADD(DAY,30,SYSUTCDATETIME()), @staff);
    SET @voucher = SCOPE_IDENTITY();
    INSERT dbo.khuyen_mai(ten_khuyen_mai, loai_giam, gia_tri_giam, ngay_bat_dau, ngay_ket_thuc)
    VALUES(N'Ưu đãi vòng tay demo', 'PHAN_TRAM', 5,
           DATEADD(DAY,-7,SYSUTCDATETIME()), DATEADD(DAY,7,SYSUTCDATETIME()));
    SET @promo = SCOPE_IDENTITY();
    INSERT dbo.khuyen_mai_bien_the(id_khuyen_mai,id_bien_the)
    VALUES(@promo,@bt3),(@promo,@bt4);

    DECLARE @o1 BIGINT, @o2 BIGINT, @o3 BIGINT, @o4 BIGINT, @o5 BIGINT;
    DECLARE @o6 BIGINT, @o7 BIGINT, @o8 BIGINT, @o9 BIGINT, @o10 BIGINT;

    -- Online completed: 2 x 249000
    INSERT dbo.don_hang(id_khach_hang,id_dia_chi_giao_hang,kenh_ban,trang_thai,ngay_dat,tien_hang,
                        ten_nguoi_nhan,sdt_nguoi_nhan,dia_chi_giao_snapshot)
    VALUES(@customer1,@address,'ONLINE','HOAN_THANH',DATEADD(DAY,-2,SYSUTCDATETIME()),498000,
           N'Nguyễn Minh Anh','0900000001',N'Quận Cầu Giấy, Hà Nội');
    SET @o1 = SCOPE_IDENTITY();
    INSERT dbo.don_hang_chi_tiet(id_don_hang,id_bien_the,so_luong,gia_ban) VALUES(@o1,@bt1,2,249000);
    INSERT dbo.thanh_toan(id_don_hang,phuong_thuc,so_tien,trang_thai,ma_giao_dich,ngay_thanh_toan)
    VALUES(@o1,'CHUYEN_KHOAN',498000,'THANH_CONG','DEMO-001',DATEADD(DAY,-2,SYSUTCDATETIME()));

    -- Offline completed
    INSERT dbo.don_hang(id_nhan_vien,kenh_ban,trang_thai,ngay_dat,tien_hang)
    VALUES(@staff,'OFFLINE','HOAN_THANH',DATEADD(HOUR,-2,SYSUTCDATETIME()),179000);
    SET @o2 = SCOPE_IDENTITY();
    INSERT dbo.don_hang_chi_tiet(id_don_hang,id_bien_the,so_luong,gia_ban) VALUES(@o2,@bt3,1,179000);
    INSERT dbo.thanh_toan(id_don_hang,phuong_thuc,so_tien,trang_thai,ma_giao_dich,ngay_thanh_toan)
    VALUES(@o2,'TIEN_MAT',179000,'THANH_CONG','DEMO-002',DATEADD(HOUR,-2,SYSUTCDATETIME()));

    -- Online pending
    INSERT dbo.don_hang(id_khach_hang,kenh_ban,trang_thai,ngay_dat,tien_hang,
                        ten_nguoi_nhan,sdt_nguoi_nhan,dia_chi_giao_snapshot)
    VALUES(@customer2,'ONLINE','CHUA_XU_LY',DATEADD(HOUR,-3,SYSUTCDATETIME()),279000,
           N'Trần Bảo Ngọc','0900000002',N'Quận Thanh Xuân, Hà Nội');
    SET @o3 = SCOPE_IDENTITY();
    INSERT dbo.don_hang_chi_tiet(id_don_hang,id_bien_the,so_luong,gia_ban) VALUES(@o3,@bt2,1,279000);

    -- Online processing
    INSERT dbo.don_hang(id_khach_hang,id_nhan_vien,kenh_ban,trang_thai,ngay_dat,tien_hang,
                        ten_nguoi_nhan,sdt_nguoi_nhan,dia_chi_giao_snapshot)
    VALUES(@customer2,@staff,'ONLINE','DANG_XU_LY',DATEADD(DAY,-1,SYSUTCDATETIME()),179000,
           N'Trần Bảo Ngọc','0900000002',N'Quận Thanh Xuân, Hà Nội');
    SET @o4 = SCOPE_IDENTITY();
    INSERT dbo.don_hang_chi_tiet(id_don_hang,id_bien_the,so_luong,gia_ban) VALUES(@o4,@bt3,1,179000);

    -- Offline completed: 249000 + 99000
    INSERT dbo.don_hang(id_nhan_vien,kenh_ban,trang_thai,ngay_dat,tien_hang)
    VALUES(@staff,'OFFLINE','HOAN_THANH',DATEADD(DAY,-3,SYSUTCDATETIME()),348000);
    SET @o5 = SCOPE_IDENTITY();
    INSERT dbo.don_hang_chi_tiet(id_don_hang,id_bien_the,so_luong,gia_ban)
    VALUES(@o5,@bt1,1,249000),(@o5,@bt4,1,99000);
    INSERT dbo.thanh_toan(id_don_hang,phuong_thuc,so_tien,trang_thai,ma_giao_dich,ngay_thanh_toan)
    VALUES(@o5,'TIEN_MAT',348000,'THANH_CONG','DEMO-005',DATEADD(DAY,-3,SYSUTCDATETIME()));

    -- Online processed and paid (not yet completed)
    INSERT dbo.don_hang(id_khach_hang,id_nhan_vien,kenh_ban,trang_thai,ngay_dat,tien_hang,
                        ten_nguoi_nhan,sdt_nguoi_nhan,dia_chi_giao_snapshot)
    VALUES(@customer1,@staff,'ONLINE','DA_XU_LY',DATEADD(DAY,-1,SYSUTCDATETIME()),99000,
           N'Nguyễn Minh Anh','0900000001',N'Quận Cầu Giấy, Hà Nội');
    SET @o6 = SCOPE_IDENTITY();
    INSERT dbo.don_hang_chi_tiet(id_don_hang,id_bien_the,so_luong,gia_ban) VALUES(@o6,@bt4,1,99000);
    INSERT dbo.thanh_toan(id_don_hang,phuong_thuc,so_tien,trang_thai,ma_giao_dich,ngay_thanh_toan)
    VALUES(@o6,'CHUYEN_KHOAN',99000,'THANH_CONG','DEMO-006',DATEADD(DAY,-1,SYSUTCDATETIME()));

    -- Online cancelled
    INSERT dbo.don_hang(id_khach_hang,kenh_ban,trang_thai,ngay_dat,tien_hang,
                        ten_nguoi_nhan,sdt_nguoi_nhan,dia_chi_giao_snapshot)
    VALUES(@customer2,'ONLINE','DA_HUY',DATEADD(DAY,-4,SYSUTCDATETIME()),279000,
           N'Trần Bảo Ngọc','0900000002',N'Quận Thanh Xuân, Hà Nội');
    SET @o7 = SCOPE_IDENTITY();
    INSERT dbo.don_hang_chi_tiet(id_don_hang,id_bien_the,so_luong,gia_ban) VALUES(@o7,@bt2,1,279000);

    -- Online completed, voucher 10%: 249000-24900 = 224100
    INSERT dbo.don_hang(id_khach_hang,id_ma_giam_gia,kenh_ban,trang_thai,ngay_dat,tien_hang,tien_giam,
                        ten_nguoi_nhan,sdt_nguoi_nhan,dia_chi_giao_snapshot)
    VALUES(@customer1,@voucher,'ONLINE','HOAN_THANH',DATEADD(DAY,-5,SYSUTCDATETIME()),249000,24900,
           N'Nguyễn Minh Anh','0900000001',N'Quận Cầu Giấy, Hà Nội');
    SET @o8 = SCOPE_IDENTITY();
    INSERT dbo.don_hang_chi_tiet(id_don_hang,id_bien_the,so_luong,gia_ban) VALUES(@o8,@bt1,1,249000);
    INSERT dbo.thanh_toan(id_don_hang,phuong_thuc,so_tien,trang_thai,ma_giao_dich,ngay_thanh_toan)
    VALUES(@o8,'CHUYEN_KHOAN',224100,'THANH_CONG','DEMO-008',DATEADD(DAY,-5,SYSUTCDATETIME()));

    -- Offline completed, 6 days ago
    INSERT dbo.don_hang(id_nhan_vien,kenh_ban,trang_thai,ngay_dat,tien_hang)
    VALUES(@staff,'OFFLINE','HOAN_THANH',DATEADD(DAY,-6,SYSUTCDATETIME()),99000);
    SET @o9 = SCOPE_IDENTITY();
    INSERT dbo.don_hang_chi_tiet(id_don_hang,id_bien_the,so_luong,gia_ban) VALUES(@o9,@bt4,1,99000);
    INSERT dbo.thanh_toan(id_don_hang,phuong_thuc,so_tien,trang_thai,ma_giao_dich,ngay_thanh_toan)
    VALUES(@o9,'TIEN_MAT',99000,'THANH_CONG','DEMO-009',DATEADD(DAY,-6,SYSUTCDATETIME()));

    -- Offline completed, 7 days ago (not shown in default 7-day filter)
    INSERT dbo.don_hang(id_nhan_vien,kenh_ban,trang_thai,ngay_dat,tien_hang)
    VALUES(@staff,'OFFLINE','HOAN_THANH',DATEADD(DAY,-7,SYSUTCDATETIME()),179000);
    SET @o10 = SCOPE_IDENTITY();
    INSERT dbo.don_hang_chi_tiet(id_don_hang,id_bien_the,so_luong,gia_ban) VALUES(@o10,@bt3,1,179000);
    INSERT dbo.thanh_toan(id_don_hang,phuong_thuc,so_tien,trang_thai,ma_giao_dich,ngay_thanh_toan)
    VALUES(@o10,'TIEN_MAT',179000,'THANH_CONG','DEMO-010',DATEADD(DAY,-7,SYSUTCDATETIME()));

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO
