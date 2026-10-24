-- GD8: Upgrade from GD7. Run only on existing databases (backup first).
USE VongTayNu;
GO
IF COL_LENGTH('dbo.don_hang', 'ma_yeu_cau_online') IS NULL
BEGIN
    ALTER TABLE dbo.don_hang ADD ma_yeu_cau_online VARCHAR(36) NULL;
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.don_hang')
               AND name='UX_dh_ma_yeu_cau_online')
BEGIN
    CREATE UNIQUE INDEX UX_dh_ma_yeu_cau_online
    ON dbo.don_hang(ma_yeu_cau_online) WHERE ma_yeu_cau_online IS NOT NULL;
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name='CK_dh_online_token'
               AND parent_object_id=OBJECT_ID('dbo.don_hang'))
BEGIN
    ALTER TABLE dbo.don_hang ADD CONSTRAINT CK_dh_online_token
    CHECK(ma_yeu_cau_online IS NULL OR kenh_ban='ONLINE');
END;
GO
PRINT 'GD8 ready: online checkout idempotency index installed.';
GO
