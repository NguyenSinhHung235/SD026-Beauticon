/* Upgrade from GD3 to GD4. SQL Server 2019+. Keep existing orders/payments. */
USE VongTayNu;
GO
SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF COL_LENGTH('dbo.don_hang','ton_kho_da_tru') IS NULL
    ALTER TABLE dbo.don_hang ADD ton_kho_da_tru BIT NOT NULL
        CONSTRAINT DF_dh_ton_kho_da_tru DEFAULT (0);
IF COL_LENGTH('dbo.don_hang','voucher_da_su_dung') IS NULL
    ALTER TABLE dbo.don_hang ADD voucher_da_su_dung BIT NOT NULL
        CONSTRAINT DF_dh_voucher_da_su_dung DEFAULT (0);
IF COL_LENGTH('dbo.don_hang','ngay_cap_nhat') IS NULL
    ALTER TABLE dbo.don_hang ADD ngay_cap_nhat DATETIME2(0) NOT NULL
        CONSTRAINT DF_dh_ngay_cap_nhat DEFAULT SYSUTCDATETIME();
IF COL_LENGTH('dbo.don_hang','ngay_hoan_thanh') IS NULL
    ALTER TABLE dbo.don_hang ADD ngay_hoan_thanh DATETIME2(0) NULL;
IF COL_LENGTH('dbo.don_hang','ngay_huy') IS NULL
    ALTER TABLE dbo.don_hang ADD ngay_huy DATETIME2(0) NULL;
IF COL_LENGTH('dbo.don_hang','ly_do_huy') IS NULL
    ALTER TABLE dbo.don_hang ADD ly_do_huy NVARCHAR(500) NULL;
IF COL_LENGTH('dbo.thanh_toan','ma_doi_soat') IS NULL
    ALTER TABLE dbo.thanh_toan ADD ma_doi_soat VARCHAR(120) NULL;

/* GD3 checkout always subtracts stock and consumes voucher when used.
   Other old ONLINE orders are intentionally NOT assumed reserved; verify real data manually. */
EXEC sys.sp_executesql N'
UPDATE dbo.don_hang SET ton_kho_da_tru=1,
    voucher_da_su_dung=CASE WHEN id_ma_giam_gia IS NULL THEN 0 ELSE 1 END
WHERE kenh_ban=''OFFLINE'' AND ma_yeu_cau_pos IS NOT NULL AND trang_thai=''HOAN_THANH'';';

IF OBJECT_ID(N'dbo.lich_su_don_hang',N'U') IS NULL
BEGIN
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
        CONSTRAINT CK_lsdh_event CHECK(su_kien IN ('CHUYEN_TRANG_THAI','HUY_DON','XAC_NHAN_THU_TIEN','XAC_NHAN_HOAN_TIEN'))
    );
END;
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='UX_tt_method_reference' AND object_id=OBJECT_ID('dbo.thanh_toan'))
    EXEC sys.sp_executesql N'CREATE UNIQUE INDEX UX_tt_method_reference ON dbo.thanh_toan(phuong_thuc,ma_doi_soat) WHERE ma_doi_soat IS NOT NULL;';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='IX_lsdh_don' AND object_id=OBJECT_ID('dbo.lich_su_don_hang'))
    EXEC sys.sp_executesql N'CREATE INDEX IX_lsdh_don ON dbo.lich_su_don_hang(id_don_hang, ngay_tao DESC);';

COMMIT TRANSACTION;
GO
