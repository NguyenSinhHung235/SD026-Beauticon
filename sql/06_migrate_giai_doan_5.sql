/* Upgrade GD4 -> GD5, SQL Server 2019+. Backup the database before running. */
USE VongTayNu;
GO
SET XACT_ABORT ON;
BEGIN TRANSACTION;

-- A customer's addresses are soft-deleted: referenced orders must remain valid.
IF COL_LENGTH('dbo.dia_chi_giao_hang','trang_thai') IS NULL
    ALTER TABLE dbo.dia_chi_giao_hang ADD trang_thai BIT NOT NULL
        CONSTRAINT DF_dia_chi_trang_thai DEFAULT (1);

-- The old seed has a placeholder, not a real password; disable it to avoid a fake active admin.
UPDATE dbo.nhan_vien SET trang_thai=0
WHERE email='demo-staff@example.invalid' AND mat_khau_hash='DEMO_DISABLED_ACCOUNT_NO_LOGIN';

-- Only these two well-defined staff roles may log in.
IF EXISTS (SELECT 1 FROM dbo.nhan_vien WHERE chuc_danh NOT IN (N'QUAN_LY', N'NHAN_VIEN'))
    THROW 51001, N'Có chức danh nhân viên chưa chuẩn. Chuẩn hóa trước khi thêm ràng buộc.', 1;
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_nhan_vien_chuc_danh'
               AND parent_object_id = OBJECT_ID('dbo.nhan_vien'))
    ALTER TABLE dbo.nhan_vien ADD CONSTRAINT CK_nhan_vien_chuc_danh
        CHECK (chuc_danh IN (N'QUAN_LY', N'NHAN_VIEN'));

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='IX_khach_hang_sdt'
               AND object_id=OBJECT_ID('dbo.khach_hang'))
    CREATE INDEX IX_khach_hang_sdt ON dbo.khach_hang(sdt) WHERE sdt IS NOT NULL;

COMMIT TRANSACTION;
GO
