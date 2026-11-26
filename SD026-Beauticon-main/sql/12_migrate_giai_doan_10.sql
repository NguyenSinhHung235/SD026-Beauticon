USE VongTayNu;
GO
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO
BEGIN TRY
BEGIN TRANSACTION;

    IF COL_LENGTH(N'dbo.chat_lieu', N'don_gia_gram') IS NULL
        EXEC(N'ALTER TABLE dbo.chat_lieu ADD don_gia_gram DECIMAL(18,2) NOT NULL CONSTRAINT DF_chat_lieu_don_gia_gram DEFAULT (0)');

    IF COL_LENGTH(N'dbo.san_pham', N'chi_phi_che_tac') IS NULL
        EXEC(N'ALTER TABLE dbo.san_pham ADD chi_phi_che_tac DECIMAL(18,2) NOT NULL CONSTRAINT DF_sp_chi_phi_che_tac DEFAULT (0)');

    IF COL_LENGTH(N'dbo.san_pham_bien_the', N'phu_phi_bien_the') IS NULL
        EXEC(N'ALTER TABLE dbo.san_pham_bien_the ADD phu_phi_bien_the DECIMAL(18,2) NOT NULL CONSTRAINT DF_bt_phu_phi_bien_the DEFAULT (0)');

    IF COL_LENGTH(N'dbo.don_hang', N'phuong_thuc_giao_hang') IS NULL
        EXEC(N'ALTER TABLE dbo.don_hang ADD phuong_thuc_giao_hang VARCHAR(30) NOT NULL CONSTRAINT DF_dh_pt_giao_hang DEFAULT (''GIAO_TIEU_CHUAN'')');

EXEC(N'
        UPDATE dbo.chat_lieu SET don_gia_gram = 45000
        WHERE LOWER(LTRIM(RTRIM(ten_chat_lieu))) LIKE N''%bạc%'' AND don_gia_gram = 0;

        UPDATE dbo.chat_lieu SET don_gia_gram = 120000
        WHERE LOWER(LTRIM(RTRIM(ten_chat_lieu))) LIKE N''%vàng%'' AND don_gia_gram = 0;

        UPDATE dbo.chat_lieu SET don_gia_gram = 18000
        WHERE (LOWER(LTRIM(RTRIM(ten_chat_lieu))) LIKE N''%thép%'' OR LOWER(LTRIM(RTRIM(ten_chat_lieu))) LIKE N''%titan%'')
          AND don_gia_gram = 0;

        UPDATE dbo.chat_lieu SET don_gia_gram = 25000
        WHERE (LOWER(LTRIM(RTRIM(ten_chat_lieu))) LIKE N''%đá%'' OR LOWER(LTRIM(RTRIM(ten_chat_lieu))) LIKE N''%ngọc%'')
          AND don_gia_gram = 0;
    ');

COMMIT TRANSACTION;
PRINT N'GD10 migration hoàn tất.';
END TRY
BEGIN CATCH
IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO