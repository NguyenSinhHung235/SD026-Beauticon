/* Chi chay neu da tao DB tu goi Dashboard giai doan 1.
   Neu khoi tao DB moi bang 01_schema.sql da cap nhat: KHONG can chay file nay. */
USE VongTayNu;
GO
IF COL_LENGTH('dbo.thuong_hieu', 'trang_thai') IS NULL
    ALTER TABLE dbo.thuong_hieu
    ADD trang_thai BIT NOT NULL CONSTRAINT DF_thuong_hieu_trang_thai DEFAULT 1 WITH VALUES;
GO
IF COL_LENGTH('dbo.chat_lieu', 'trang_thai') IS NULL
    ALTER TABLE dbo.chat_lieu
    ADD trang_thai BIT NOT NULL CONSTRAINT DF_chat_lieu_trang_thai DEFAULT 1 WITH VALUES;
GO
IF COL_LENGTH('dbo.nha_cung_cap', 'mo_ta') IS NULL
    ALTER TABLE dbo.nha_cung_cap ADD mo_ta NVARCHAR(500) NULL;
GO
