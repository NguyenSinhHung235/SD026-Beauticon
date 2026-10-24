/*
  Web ban vong tay nu - SQL Server 2019+
  17 bang trong ERD + 5 bang: nha_cung_cap, chat_lieu, khuyen_mai, khuyen_mai_bien_the, lich_su_don_hang.
  Thoi gian luu UTC (SYSUTCDATETIME); tien VND dung DECIMAL(18,2).
  Chay 1 lan tren CSDL moi bang SQL Server Management Studio.
*/
IF DB_ID(N'VongTayNu') IS NULL
    CREATE DATABASE VongTayNu;
GO
USE VongTayNu;
GO

CREATE TABLE dbo.loai_san_pham (
    id_loai BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ten_loai NVARCHAR(150) NOT NULL,
    mo_ta NVARCHAR(1000) NULL,
    trang_thai BIT NOT NULL CONSTRAINT DF_loai_trang_thai DEFAULT 1,
    CONSTRAINT UQ_loai_ten UNIQUE (ten_loai)
);
GO
CREATE TABLE dbo.thuong_hieu (
    id_thuong_hieu BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ten_thuong_hieu NVARCHAR(150) NOT NULL,
    quoc_gia NVARCHAR(100) NULL,
    mo_ta NVARCHAR(1000) NULL,
    trang_thai BIT NOT NULL CONSTRAINT DF_thuong_hieu_trang_thai DEFAULT 1,
    CONSTRAINT UQ_thuong_hieu_ten UNIQUE (ten_thuong_hieu)
);
GO
CREATE TABLE dbo.nha_cung_cap (
    id_nha_cung_cap BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ten_nha_cung_cap NVARCHAR(200) NOT NULL,
    sdt VARCHAR(20) NULL,
    email VARCHAR(254) NULL,
    dia_chi NVARCHAR(500) NULL,
    mo_ta NVARCHAR(500) NULL,
    trang_thai BIT NOT NULL CONSTRAINT DF_ncc_trang_thai DEFAULT 1,
    CONSTRAINT UQ_nha_cung_cap_ten UNIQUE (ten_nha_cung_cap)
);
GO
CREATE TABLE dbo.chat_lieu (
    id_chat_lieu BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ten_chat_lieu NVARCHAR(100) NOT NULL,
    mo_ta NVARCHAR(500) NULL,
    trang_thai BIT NOT NULL CONSTRAINT DF_chat_lieu_trang_thai DEFAULT 1,
    CONSTRAINT UQ_chat_lieu_ten UNIQUE (ten_chat_lieu)
);
GO
CREATE TABLE dbo.bien_the_mau_sac (
    id_mau_sac BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ten_mau_sac NVARCHAR(100) NOT NULL,
    ma_mau VARCHAR(9) NULL,
    mo_ta NVARCHAR(500) NULL,
    ngay_tao DATETIME2(0) NOT NULL CONSTRAINT DF_mau_ngay_tao DEFAULT SYSUTCDATETIME(),
    trang_thai BIT NOT NULL CONSTRAINT DF_mau_trang_thai DEFAULT 1,
    CONSTRAINT UQ_mau_ten UNIQUE (ten_mau_sac)
);
GO
CREATE TABLE dbo.bien_the_kich_thuoc (
    id_kich_thuoc BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ten_kich_thuoc NVARCHAR(100) NOT NULL,
    mo_ta NVARCHAR(500) NULL,
    ngay_tao DATETIME2(0) NOT NULL CONSTRAINT DF_size_ngay_tao DEFAULT SYSUTCDATETIME(),
    trang_thai BIT NOT NULL CONSTRAINT DF_size_trang_thai DEFAULT 1,
    CONSTRAINT UQ_size_ten UNIQUE (ten_kich_thuoc)
);
GO
CREATE TABLE dbo.nhan_vien (
    id_nhan_vien BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ho_ten NVARCHAR(150) NOT NULL,
    email VARCHAR(254) NOT NULL,
    mat_khau_hash VARCHAR(255) NOT NULL, -- BCrypt/Argon2, KHONG luu mat khau ro
    chuc_danh NVARCHAR(80) NOT NULL CONSTRAINT DF_nhan_vien_vai_tro DEFAULT N'NHAN_VIEN',
    trang_thai BIT NOT NULL CONSTRAINT DF_nv_trang_thai DEFAULT 1,
    CONSTRAINT UQ_nhan_vien_email UNIQUE (email),
    CONSTRAINT CK_nhan_vien_chuc_danh CHECK (chuc_danh IN (N'QUAN_LY', N'NHAN_VIEN'))
);
GO
CREATE TABLE dbo.khach_hang (
    id_khach_hang BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ho_ten NVARCHAR(150) NOT NULL,
    email VARCHAR(254) NULL,
    sdt VARCHAR(20) NULL,
    ngay_sinh DATE NULL,
    mat_khau_hash VARCHAR(255) NULL, -- NULL neu dang nhap OTP/Google
    trang_thai BIT NOT NULL CONSTRAINT DF_khach_trang_thai DEFAULT 1,
    ngay_tao DATETIME2(0) NOT NULL CONSTRAINT DF_khach_ngay_tao DEFAULT SYSUTCDATETIME()
);
GO
CREATE UNIQUE INDEX UX_khach_email ON dbo.khach_hang(email) WHERE email IS NOT NULL;
CREATE INDEX IX_khach_hang_sdt ON dbo.khach_hang(sdt) WHERE sdt IS NOT NULL;
GO
CREATE TABLE dbo.dia_chi_giao_hang (
    id_dia_chi BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    id_khach_hang BIGINT NOT NULL,
    ten_nguoi_nhan NVARCHAR(150) NOT NULL,
    sdt VARCHAR(20) NOT NULL,
    dia_chi_chi_tiet NVARCHAR(500) NOT NULL,
    mac_dinh BIT NOT NULL CONSTRAINT DF_dia_chi_mac_dinh DEFAULT 0,
    trang_thai BIT NOT NULL CONSTRAINT DF_dia_chi_trang_thai DEFAULT 1,
    CONSTRAINT FK_dia_chi_khach FOREIGN KEY(id_khach_hang) REFERENCES dbo.khach_hang(id_khach_hang),
    CONSTRAINT UQ_dia_chi_chu_so_huu UNIQUE(id_khach_hang, id_dia_chi)
);
GO
CREATE UNIQUE INDEX UX_dia_chi_mac_dinh ON dbo.dia_chi_giao_hang(id_khach_hang) WHERE mac_dinh = 1;
GO
CREATE TABLE dbo.san_pham (
    id_san_pham BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    id_loai BIGINT NOT NULL,
    id_thuong_hieu BIGINT NULL,
    id_nha_cung_cap BIGINT NULL,
    id_chat_lieu BIGINT NULL,
    ten_san_pham NVARCHAR(200) NOT NULL,
    trong_luong_gram DECIMAL(10,3) NULL,
    mo_ta NVARCHAR(MAX) NULL,
    ngay_san_xuat DATE NULL,
    freesize_min_cm DECIMAL(4,1) NULL,
    freesize_max_cm DECIMAL(4,1) NULL,
    trang_thai BIT NOT NULL CONSTRAINT DF_sp_trang_thai DEFAULT 1,
    ngay_tao DATETIME2(0) NOT NULL CONSTRAINT DF_sp_ngay_tao DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_sp_loai FOREIGN KEY(id_loai) REFERENCES dbo.loai_san_pham(id_loai),
    CONSTRAINT FK_sp_thuong_hieu FOREIGN KEY(id_thuong_hieu) REFERENCES dbo.thuong_hieu(id_thuong_hieu),
    CONSTRAINT FK_sp_ncc FOREIGN KEY(id_nha_cung_cap) REFERENCES dbo.nha_cung_cap(id_nha_cung_cap),
    CONSTRAINT FK_sp_chat_lieu FOREIGN KEY(id_chat_lieu) REFERENCES dbo.chat_lieu(id_chat_lieu),
    CONSTRAINT CK_sp_trong_luong CHECK (trong_luong_gram IS NULL OR trong_luong_gram > 0),
    CONSTRAINT CK_sp_freesize_range CHECK (
      (freesize_min_cm IS NULL AND freesize_max_cm IS NULL)
      OR (freesize_min_cm >= 10.0 AND freesize_max_cm <= 30.0
          AND freesize_min_cm < freesize_max_cm))
);
GO
CREATE TABLE dbo.hinh_anh_sp (
    id_anh BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    id_san_pham BIGINT NOT NULL,
    duong_dan_anh NVARCHAR(1000) NOT NULL,
    anh_chinh BIT NOT NULL CONSTRAINT DF_anh_chinh DEFAULT 0,
    CONSTRAINT FK_anh_sp FOREIGN KEY(id_san_pham) REFERENCES dbo.san_pham(id_san_pham)
);
GO
CREATE UNIQUE INDEX UX_anh_chinh ON dbo.hinh_anh_sp(id_san_pham) WHERE anh_chinh = 1;
GO
CREATE TABLE dbo.san_pham_bien_the (
    id_bien_the BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    id_san_pham BIGINT NOT NULL,
    id_mau_sac BIGINT NOT NULL,
    id_kich_thuoc BIGINT NOT NULL,
    gia_ban DECIMAL(18,2) NOT NULL,
    so_luong_ton INT NOT NULL CONSTRAINT DF_bt_ton DEFAULT 0,
    trang_thai BIT NOT NULL CONSTRAINT DF_bt_trang_thai DEFAULT 1,
    CONSTRAINT FK_bt_sp FOREIGN KEY(id_san_pham) REFERENCES dbo.san_pham(id_san_pham),
    CONSTRAINT FK_bt_mau FOREIGN KEY(id_mau_sac) REFERENCES dbo.bien_the_mau_sac(id_mau_sac),
    CONSTRAINT FK_bt_size FOREIGN KEY(id_kich_thuoc) REFERENCES dbo.bien_the_kich_thuoc(id_kich_thuoc),
    CONSTRAINT UQ_bien_the UNIQUE(id_san_pham, id_mau_sac, id_kich_thuoc),
    CONSTRAINT CK_bt_gia CHECK(gia_ban >= 0),
    CONSTRAINT CK_bt_ton CHECK(so_luong_ton >= 0)
);
GO
CREATE TABLE dbo.ma_giam_gia (
    id_ma_giam_gia BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    code VARCHAR(60) NOT NULL,
    mo_ta NVARCHAR(500) NULL,
    loai_giam VARCHAR(10) NOT NULL, -- PHAN_TRAM / CO_DINH
    gia_tri_giam DECIMAL(18,2) NOT NULL,
    gia_tri_don_toi_thieu DECIMAL(18,2) NOT NULL CONSTRAINT DF_voucher_min DEFAULT 0,
    giam_toi_da DECIMAL(18,2) NULL,
    so_luong INT NOT NULL,
    so_luong_da_dung INT NOT NULL CONSTRAINT DF_voucher_used DEFAULT 0,
    ngay_bat_dau DATETIME2(0) NOT NULL,
    ngay_ket_thuc DATETIME2(0) NOT NULL,
    id_nhan_vien BIGINT NULL,
    trang_thai BIT NOT NULL CONSTRAINT DF_voucher_status DEFAULT 1,
    CONSTRAINT UQ_voucher_code UNIQUE(code),
    CONSTRAINT FK_voucher_nv FOREIGN KEY(id_nhan_vien) REFERENCES dbo.nhan_vien(id_nhan_vien),
    CONSTRAINT CK_voucher_type CHECK(loai_giam IN ('PHAN_TRAM','CO_DINH')),
    CONSTRAINT CK_voucher_value CHECK(gia_tri_giam > 0 AND (loai_giam <> 'PHAN_TRAM' OR gia_tri_giam <= 100)),
    CONSTRAINT CK_voucher_min CHECK(gia_tri_don_toi_thieu >= 0),
    CONSTRAINT CK_voucher_cap CHECK(giam_toi_da IS NULL OR giam_toi_da > 0),
    CONSTRAINT CK_voucher_qty CHECK(so_luong >= 0 AND so_luong_da_dung >= 0 AND so_luong_da_dung <= so_luong),
    CONSTRAINT CK_voucher_time CHECK(ngay_ket_thuc > ngay_bat_dau)
);
GO
CREATE TABLE dbo.khuyen_mai (
    id_khuyen_mai BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    ten_khuyen_mai NVARCHAR(200) NOT NULL,
    mo_ta NVARCHAR(500) NULL,
    loai_giam VARCHAR(10) NOT NULL,
    gia_tri_giam DECIMAL(18,2) NOT NULL,
    ngay_bat_dau DATETIME2(0) NOT NULL,
    ngay_ket_thuc DATETIME2(0) NOT NULL,
    trang_thai BIT NOT NULL CONSTRAINT DF_km_status DEFAULT 1,
    CONSTRAINT CK_km_type CHECK(loai_giam IN ('PHAN_TRAM','CO_DINH')),
    CONSTRAINT CK_km_value CHECK(gia_tri_giam > 0 AND (loai_giam <> 'PHAN_TRAM' OR gia_tri_giam <= 100)),
    CONSTRAINT CK_km_time CHECK(ngay_ket_thuc > ngay_bat_dau)
);
GO
CREATE TABLE dbo.khuyen_mai_bien_the (
    id_khuyen_mai BIGINT NOT NULL,
    id_bien_the BIGINT NOT NULL,
    CONSTRAINT PK_km_bien_the PRIMARY KEY(id_khuyen_mai, id_bien_the),
    CONSTRAINT FK_kmbt_km FOREIGN KEY(id_khuyen_mai) REFERENCES dbo.khuyen_mai(id_khuyen_mai),
    CONSTRAINT FK_kmbt_bt FOREIGN KEY(id_bien_the) REFERENCES dbo.san_pham_bien_the(id_bien_the)
);
GO
CREATE TABLE dbo.gio_hang (
    id_gio_hang BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    id_khach_hang BIGINT NOT NULL,
    ngay_tao DATETIME2(0) NOT NULL CONSTRAINT DF_gh_ngay_tao DEFAULT SYSUTCDATETIME(),
    ngay_cap_nhat DATETIME2(0) NOT NULL CONSTRAINT DF_gh_ngay_update DEFAULT SYSUTCDATETIME(),
    trang_thai BIT NOT NULL CONSTRAINT DF_gh_status DEFAULT 1,
    CONSTRAINT FK_gio_khach FOREIGN KEY(id_khach_hang) REFERENCES dbo.khach_hang(id_khach_hang),
    CONSTRAINT UQ_gio_khach UNIQUE(id_khach_hang)
);
GO
CREATE TABLE dbo.chi_tiet_gio_hang (
    id_chi_tiet_gh BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    id_gio_hang BIGINT NOT NULL,
    id_bien_the BIGINT NOT NULL,
    so_luong INT NOT NULL,
    kieu_kich_thuoc VARCHAR(16) NOT NULL CONSTRAINT DF_ctgh_size_mode DEFAULT 'CO_SAN',
    chu_vi_co_tay_cm DECIMAL(4,1) NULL,
    do_rong_deo VARCHAR(8) NULL,
    ghi_chu_kich_thuoc NVARCHAR(300) NULL,
    CONSTRAINT FK_ctgh_gio FOREIGN KEY(id_gio_hang) REFERENCES dbo.gio_hang(id_gio_hang),
    CONSTRAINT FK_ctgh_bt FOREIGN KEY(id_bien_the) REFERENCES dbo.san_pham_bien_the(id_bien_the),
    CONSTRAINT UQ_ctgh_unique UNIQUE(id_gio_hang, id_bien_the,
        kieu_kich_thuoc, chu_vi_co_tay_cm, do_rong_deo),
    CONSTRAINT CK_ctgh_size_mode CHECK (
      (kieu_kich_thuoc IN ('CO_SAN','FREESIZE') AND chu_vi_co_tay_cm IS NULL AND do_rong_deo IS NULL)
      OR (kieu_kich_thuoc='THEO_YEU_CAU' AND chu_vi_co_tay_cm IS NOT NULL AND do_rong_deo IS NOT NULL
          AND chu_vi_co_tay_cm BETWEEN 10 AND 30
          AND do_rong_deo IN ('OM','VUA','RONG'))),
    CONSTRAINT CK_ctgh_qty CHECK(so_luong > 0)
);
GO
CREATE TABLE dbo.don_hang (
    id_don_hang BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    id_khach_hang BIGINT NULL, -- NULL: khach vang lai tai quay
    id_ma_giam_gia BIGINT NULL,
    id_nhan_vien BIGINT NULL, -- NULL: don online chua co nguoi xu ly
    id_dia_chi_giao_hang BIGINT NULL,
    kenh_ban VARCHAR(10) NOT NULL, -- ONLINE / OFFLINE
    trang_thai VARCHAR(20) NOT NULL CONSTRAINT DF_dh_status DEFAULT 'CHUA_XU_LY',
    ngay_dat DATETIME2(0) NOT NULL CONSTRAINT DF_dh_ngay_dat DEFAULT SYSUTCDATETIME(),
    tien_hang DECIMAL(18,2) NOT NULL CONSTRAINT DF_dh_tien_hang DEFAULT 0,
    tien_giam DECIMAL(18,2) NOT NULL CONSTRAINT DF_dh_tien_giam DEFAULT 0,
    phi_van_chuyen DECIMAL(18,2) NOT NULL CONSTRAINT DF_dh_ship DEFAULT 0,
    tong_tien AS (tien_hang - tien_giam + phi_van_chuyen) PERSISTED,
    ten_nguoi_nhan NVARCHAR(150) NULL, -- snapshot khi dat hang
    sdt_nguoi_nhan VARCHAR(20) NULL,
    dia_chi_giao_snapshot NVARCHAR(700) NULL,
    ghi_chu NVARCHAR(1000) NULL,
    ma_yeu_cau_pos VARCHAR(36) NULL, -- UUID de chong bam nut thanh toan 2 lan
    ma_yeu_cau_online VARCHAR(36) NULL, -- UUID de chong gui don online trung
    tien_khach_dua DECIMAL(18,2) NULL, -- tien mat tai quay
    tien_thoi_lai DECIMAL(18,2) NULL,
    ton_kho_da_tru BIT NOT NULL CONSTRAINT DF_dh_ton_kho_da_tru DEFAULT (0),
    voucher_da_su_dung BIT NOT NULL CONSTRAINT DF_dh_voucher_da_su_dung DEFAULT (0),
    ngay_cap_nhat DATETIME2(0) NOT NULL CONSTRAINT DF_dh_ngay_cap_nhat DEFAULT SYSUTCDATETIME(),
    ngay_hoan_thanh DATETIME2(0) NULL,
    ngay_huy DATETIME2(0) NULL,
    ly_do_huy NVARCHAR(500) NULL,
    CONSTRAINT CK_dh_cash CHECK(
      (tien_khach_dua IS NULL AND tien_thoi_lai IS NULL)
      OR (kenh_ban = 'OFFLINE' AND tien_khach_dua IS NOT NULL AND tien_thoi_lai IS NOT NULL
          AND tien_khach_dua >= 0 AND tien_thoi_lai >= 0
          AND tien_khach_dua - tien_thoi_lai = tien_hang - tien_giam + phi_van_chuyen)),
    CONSTRAINT FK_dh_khach FOREIGN KEY(id_khach_hang) REFERENCES dbo.khach_hang(id_khach_hang),
    CONSTRAINT FK_dh_voucher FOREIGN KEY(id_ma_giam_gia) REFERENCES dbo.ma_giam_gia(id_ma_giam_gia),
    CONSTRAINT FK_dh_nv FOREIGN KEY(id_nhan_vien) REFERENCES dbo.nhan_vien(id_nhan_vien),
    CONSTRAINT FK_dh_dia_chi_dung_khach FOREIGN KEY(id_khach_hang, id_dia_chi_giao_hang)
        REFERENCES dbo.dia_chi_giao_hang(id_khach_hang, id_dia_chi),
    CONSTRAINT CK_dh_kenh CHECK(kenh_ban IN ('ONLINE','OFFLINE')),
    CONSTRAINT CK_dh_online_token CHECK(ma_yeu_cau_online IS NULL OR kenh_ban='ONLINE'),
    CONSTRAINT CK_dh_status CHECK(trang_thai IN ('CHUA_XU_LY','DANG_XU_LY','DA_XU_LY','HOAN_THANH','DA_HUY')),
    CONSTRAINT CK_dh_tien CHECK(tien_hang >= 0 AND tien_giam >= 0 AND tien_giam <= tien_hang AND phi_van_chuyen >= 0),
    CONSTRAINT CK_dh_dia_chi_khach CHECK(id_dia_chi_giao_hang IS NULL OR id_khach_hang IS NOT NULL),
    CONSTRAINT CK_dh_online CHECK(
        kenh_ban = 'OFFLINE' OR
        (id_khach_hang IS NOT NULL AND ten_nguoi_nhan IS NOT NULL AND
         sdt_nguoi_nhan IS NOT NULL AND dia_chi_giao_snapshot IS NOT NULL)
    )
);
GO
CREATE TABLE dbo.don_hang_chi_tiet (
    id_dh_chi_tiet BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    id_don_hang BIGINT NOT NULL,
    id_bien_the BIGINT NOT NULL,
    so_luong INT NOT NULL,
    gia_ban DECIMAL(18,2) NOT NULL, -- gia chot tai thoi diem ban
    kieu_kich_thuoc VARCHAR(16) NOT NULL CONSTRAINT DF_ctdh_size_mode DEFAULT 'CO_SAN',
    chu_vi_co_tay_cm DECIMAL(4,1) NULL,
    do_rong_deo VARCHAR(8) NULL,
    ghi_chu_kich_thuoc NVARCHAR(300) NULL,
    ten_kich_thuoc_chot NVARCHAR(100) NULL,
    trang_thai_xac_nhan_size VARCHAR(20) NOT NULL CONSTRAINT DF_ctdh_confirm_size DEFAULT 'KHONG_CAN',
    CONSTRAINT FK_ctdh_don FOREIGN KEY(id_don_hang) REFERENCES dbo.don_hang(id_don_hang),
    CONSTRAINT FK_ctdh_bt FOREIGN KEY(id_bien_the) REFERENCES dbo.san_pham_bien_the(id_bien_the),
    CONSTRAINT UQ_ctdh_bien_the UNIQUE(id_don_hang, id_bien_the,
        kieu_kich_thuoc, chu_vi_co_tay_cm, do_rong_deo),
    CONSTRAINT CK_ctdh_size_mode CHECK (
      (kieu_kich_thuoc IN ('CO_SAN','FREESIZE') AND chu_vi_co_tay_cm IS NULL AND do_rong_deo IS NULL)
      OR (kieu_kich_thuoc='THEO_YEU_CAU' AND chu_vi_co_tay_cm IS NOT NULL AND do_rong_deo IS NOT NULL
          AND chu_vi_co_tay_cm BETWEEN 10 AND 30
          AND do_rong_deo IN ('OM','VUA','RONG'))),
    CONSTRAINT CK_ctdh_confirm_size CHECK (
      (kieu_kich_thuoc='THEO_YEU_CAU' AND trang_thai_xac_nhan_size IN ('CHO_XAC_NHAN','DA_XAC_NHAN'))
      OR (kieu_kich_thuoc<>'THEO_YEU_CAU' AND trang_thai_xac_nhan_size='KHONG_CAN')),
    CONSTRAINT CK_ctdh_qty CHECK(so_luong > 0),
    CONSTRAINT CK_ctdh_gia CHECK(gia_ban >= 0)
);
GO
CREATE TABLE dbo.danh_gia (
    id_danh_gia BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    id_dh_chi_tiet BIGINT NOT NULL,
    diem TINYINT NOT NULL,
    binh_luan NVARCHAR(2000) NULL,
    ngay_dang DATETIME2(0) NOT NULL CONSTRAINT DF_dg_ngay DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_dg_ctdh FOREIGN KEY(id_dh_chi_tiet) REFERENCES dbo.don_hang_chi_tiet(id_dh_chi_tiet),
    CONSTRAINT UQ_dg_ctdh UNIQUE(id_dh_chi_tiet),
    CONSTRAINT CK_dg_diem CHECK(diem BETWEEN 1 AND 5)
);
GO
CREATE TABLE dbo.thanh_toan (
    id_thanh_toan BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    id_don_hang BIGINT NOT NULL,
    phuong_thuc VARCHAR(20) NOT NULL, -- TIEN_MAT / CHUYEN_KHOAN / VI_DIEN_TU
    loai_giao_dich VARCHAR(20) NOT NULL CONSTRAINT DF_tt_loai DEFAULT 'THANH_TOAN', -- THANH_TOAN / HOAN_TIEN
    so_tien DECIMAL(18,2) NOT NULL,
    trang_thai VARCHAR(20) NOT NULL CONSTRAINT DF_tt_status DEFAULT 'CHO_XU_LY',
    ma_giao_dich VARCHAR(120) NULL,
    ma_doi_soat VARCHAR(120) NULL,
    ngay_thanh_toan DATETIME2(0) NULL, -- UTC, khi thanh cong
    CONSTRAINT FK_tt_don FOREIGN KEY(id_don_hang) REFERENCES dbo.don_hang(id_don_hang),
    CONSTRAINT CK_tt_method CHECK(phuong_thuc IN ('TIEN_MAT','CHUYEN_KHOAN','VI_DIEN_TU')),
    CONSTRAINT CK_tt_type CHECK(loai_giao_dich IN ('THANH_TOAN','HOAN_TIEN')),
    CONSTRAINT CK_tt_status CHECK(trang_thai IN ('CHO_XU_LY','THANH_CONG','THAT_BAI')),
    CONSTRAINT CK_tt_amount CHECK(so_tien > 0),
    CONSTRAINT CK_tt_paid_date CHECK(trang_thai <> 'THANH_CONG' OR ngay_thanh_toan IS NOT NULL)
);
GO
CREATE UNIQUE INDEX UX_tt_transaction ON dbo.thanh_toan(ma_giao_dich) WHERE ma_giao_dich IS NOT NULL;
CREATE UNIQUE INDEX UX_tt_method_reference ON dbo.thanh_toan(phuong_thuc,ma_doi_soat) WHERE ma_doi_soat IS NOT NULL;
GO
CREATE UNIQUE INDEX UX_dh_ma_yeu_cau_pos
ON dbo.don_hang(ma_yeu_cau_pos) WHERE ma_yeu_cau_pos IS NOT NULL;
GO
CREATE UNIQUE INDEX UX_dh_ma_yeu_cau_online
ON dbo.don_hang(ma_yeu_cau_online) WHERE ma_yeu_cau_online IS NOT NULL;
GO
CREATE TABLE dbo.lich_su_don_hang (
    id_lich_su BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    id_don_hang BIGINT NOT NULL,
    id_nhan_vien BIGINT NULL,
    su_kien VARCHAR(30) NOT NULL,
    trang_thai_cu VARCHAR(20) NULL,
    trang_thai_moi VARCHAR(20) NULL,
    ghi_chu NVARCHAR(500) NULL,
    ngay_tao DATETIME2(0) NOT NULL CONSTRAINT DF_lsdh_ngay DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_lsdh_don FOREIGN KEY(id_don_hang) REFERENCES dbo.don_hang(id_don_hang),
    CONSTRAINT FK_lsdh_nv FOREIGN KEY(id_nhan_vien) REFERENCES dbo.nhan_vien(id_nhan_vien),
    CONSTRAINT CK_lsdh_event CHECK(su_kien IN ('CHUYEN_TRANG_THAI','HUY_DON','XAC_NHAN_THU_TIEN','XAC_NHAN_HOAN_TIEN','XAC_NHAN_KICH_THUOC'))
);
GO
CREATE INDEX IX_lsdh_don ON dbo.lich_su_don_hang(id_don_hang, ngay_tao DESC);
CREATE INDEX IX_dh_ngay ON dbo.don_hang(ngay_dat) INCLUDE (kenh_ban, trang_thai, tong_tien);
CREATE INDEX IX_dh_kenh_status ON dbo.don_hang(kenh_ban, trang_thai, ngay_dat);
CREATE INDEX IX_tt_ngay ON dbo.thanh_toan(ngay_thanh_toan, trang_thai) INCLUDE (id_don_hang, so_tien, loai_giao_dich);
CREATE INDEX IX_ctdh_bien_the ON dbo.don_hang_chi_tiet(id_bien_the);
CREATE INDEX IX_bt_san_pham ON dbo.san_pham_bien_the(id_san_pham);
CREATE INDEX IX_dg_ngay ON dbo.danh_gia(ngay_dang);
GO

/* Nghiep vu ap dung tai service trong transaction:
   - Khi checkout: kiem tra ton kho, tru ton atomically, ghi chi tiet va cap nhat tien_hang.
   - Khi huy: hoan ton neu da tru; neu da thanh toan, tao yeu cau HOAN_TIEN CHO_XU_LY.
     Chi ghi THANH_CONG sau khi xac nhan thuc te da hoan tien.
   - Voucher: kiem tra ngay, muc toi thieu, gioi han, so luong; tang so_luong_da_dung atomically.
   - Khuyen mai: neu nhieu chuong trinh cung hieu luc, ap dung mot quy tac uu tien tai service.
   - Check danh_gia: chi cho danh gia don HOAN_THANH trong service.
   - Mat khau nhan vien luu BCrypt; module admin da co authentication va phan quyen.
*/
