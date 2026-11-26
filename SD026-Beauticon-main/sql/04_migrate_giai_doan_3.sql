/* Run after GD2 on an EXISTING VongTayNu SQL Server database. */
USE VongTayNu;
GO
IF COL_LENGTH('dbo.don_hang', 'ma_yeu_cau_pos') IS NULL
    ALTER TABLE dbo.don_hang ADD ma_yeu_cau_pos VARCHAR(36) NULL;
GO
IF COL_LENGTH('dbo.don_hang', 'tien_khach_dua') IS NULL
    ALTER TABLE dbo.don_hang ADD tien_khach_dua DECIMAL(18,2) NULL;
GO
IF COL_LENGTH('dbo.don_hang', 'tien_thoi_lai') IS NULL
    ALTER TABLE dbo.don_hang ADD tien_thoi_lai DECIMAL(18,2) NULL;
GO
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE name='UX_dh_ma_yeu_cau_pos' AND object_id=OBJECT_ID('dbo.don_hang'))
    CREATE UNIQUE INDEX UX_dh_ma_yeu_cau_pos
    ON dbo.don_hang(ma_yeu_cau_pos) WHERE ma_yeu_cau_pos IS NOT NULL;
GO
IF NOT EXISTS(SELECT 1 FROM sys.check_constraints WHERE name='CK_dh_cash' AND parent_object_id=OBJECT_ID('dbo.don_hang'))
    ALTER TABLE dbo.don_hang WITH CHECK ADD CONSTRAINT CK_dh_cash CHECK (
        (tien_khach_dua IS NULL AND tien_thoi_lai IS NULL)
        OR (kenh_ban = 'OFFLINE' AND tien_khach_dua IS NOT NULL AND tien_thoi_lai IS NOT NULL
          AND tien_khach_dua >= 0 AND tien_thoi_lai >= 0
            AND tien_khach_dua - tien_thoi_lai = tien_hang - tien_giam + phi_van_chuyen));
GO
