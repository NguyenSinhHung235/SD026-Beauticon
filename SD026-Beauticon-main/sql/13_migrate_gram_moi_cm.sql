-- Thêm định mức trọng lượng (gram / 1 cm) riêng cho từng sản phẩm
USE VongTayNu;
GO
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO
BEGIN TRY
BEGIN TRANSACTION;

    IF COL_LENGTH(N'dbo.san_pham', N'gram_moi_cm') IS NULL
        EXEC(N'ALTER TABLE dbo.san_pham ADD gram_moi_cm DECIMAL(10,4) NULL');

    IF NOT EXISTS (SELECT 1 FROM sys.check_constraints
                   WHERE parent_object_id = OBJECT_ID(N'dbo.san_pham') AND name = N'CK_sp_gram_moi_cm')
        EXEC(N'ALTER TABLE dbo.san_pham WITH CHECK ADD CONSTRAINT CK_sp_gram_moi_cm CHECK (gram_moi_cm IS NULL OR gram_moi_cm > 0)');

    -- Nếu sản phẩm đã có trong_luong_gram (chuẩn 16 cm) mà chưa có gram_moi_cm thì gợi ý quy đổi sẵn
EXEC(N'
        UPDATE dbo.san_pham
        SET gram_moi_cm = CAST(trong_luong_gram / 16.0 AS DECIMAL(10,4))
        WHERE gram_moi_cm IS NULL AND trong_luong_gram IS NOT NULL AND trong_luong_gram > 0
    ');

COMMIT TRANSACTION;
PRINT N'Migration 13 (gram_moi_cm theo từng sản phẩm) hoàn tất.';
END TRY
BEGIN CATCH
IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO