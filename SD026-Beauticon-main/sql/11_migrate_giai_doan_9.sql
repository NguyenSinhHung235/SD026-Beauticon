-- BEAUTICON GD9: flexible bracelet sizing. APPLY ON AN EXISTING GD8 DATABASE.
-- 1) BACK UP your database. 2) Open this WHOLE file in SSMS, select VongTayNu, press F5.
-- 3) Do not run sql/01_schema.sql again on a populated database.
USE VongTayNu;
GO
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO
BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID(N'dbo.chi_tiet_gio_hang', N'U') IS NULL
       OR OBJECT_ID(N'dbo.don_hang_chi_tiet', N'U') IS NULL
       OR OBJECT_ID(N'dbo.san_pham', N'U') IS NULL
       THROW 51091, 'GD9 needs the existing GD8 schema. Missing database tables.', 1;

    -- Product-level adjustable free-size range (NULL means unconfigured).
    IF COL_LENGTH('dbo.san_pham','freesize_min_cm') IS NULL
        EXEC(N'ALTER TABLE dbo.san_pham ADD freesize_min_cm DECIMAL(4,1) NULL');
    IF COL_LENGTH('dbo.san_pham','freesize_max_cm') IS NULL
        EXEC(N'ALTER TABLE dbo.san_pham ADD freesize_max_cm DECIMAL(4,1) NULL');
    IF NOT EXISTS (SELECT 1 FROM sys.check_constraints
                   WHERE parent_object_id=OBJECT_ID('dbo.san_pham') AND name='CK_sp_freesize_range')
        EXEC(N'ALTER TABLE dbo.san_pham WITH CHECK ADD CONSTRAINT CK_sp_freesize_range CHECK (
            (freesize_min_cm IS NULL AND freesize_max_cm IS NULL)
            OR (freesize_min_cm >= 10.0 AND freesize_max_cm <= 30.0 AND freesize_min_cm < freesize_max_cm))');

    -- Cart line attributes are per variant+measurement, not a free-text order note.
    IF COL_LENGTH('dbo.chi_tiet_gio_hang','kieu_kich_thuoc') IS NULL
        EXEC(N'ALTER TABLE dbo.chi_tiet_gio_hang ADD kieu_kich_thuoc VARCHAR(16) NOT NULL
          CONSTRAINT DF_ctgh_size_mode DEFAULT ''CO_SAN'' WITH VALUES');
    IF COL_LENGTH('dbo.chi_tiet_gio_hang','chu_vi_co_tay_cm') IS NULL
        EXEC(N'ALTER TABLE dbo.chi_tiet_gio_hang ADD chu_vi_co_tay_cm DECIMAL(4,1) NULL');
    IF COL_LENGTH('dbo.chi_tiet_gio_hang','do_rong_deo') IS NULL
        EXEC(N'ALTER TABLE dbo.chi_tiet_gio_hang ADD do_rong_deo VARCHAR(8) NULL');
    IF COL_LENGTH('dbo.chi_tiet_gio_hang','ghi_chu_kich_thuoc') IS NULL
        EXEC(N'ALTER TABLE dbo.chi_tiet_gio_hang ADD ghi_chu_kich_thuoc NVARCHAR(300) NULL');

    -- Order line: immutable size request snapshot + a staff confirmation flag.
    IF COL_LENGTH('dbo.don_hang_chi_tiet','kieu_kich_thuoc') IS NULL
        EXEC(N'ALTER TABLE dbo.don_hang_chi_tiet ADD kieu_kich_thuoc VARCHAR(16) NOT NULL
          CONSTRAINT DF_ctdh_size_mode DEFAULT ''CO_SAN'' WITH VALUES');
    IF COL_LENGTH('dbo.don_hang_chi_tiet','chu_vi_co_tay_cm') IS NULL
        EXEC(N'ALTER TABLE dbo.don_hang_chi_tiet ADD chu_vi_co_tay_cm DECIMAL(4,1) NULL');
    IF COL_LENGTH('dbo.don_hang_chi_tiet','do_rong_deo') IS NULL
        EXEC(N'ALTER TABLE dbo.don_hang_chi_tiet ADD do_rong_deo VARCHAR(8) NULL');
    IF COL_LENGTH('dbo.don_hang_chi_tiet','ghi_chu_kich_thuoc') IS NULL
        EXEC(N'ALTER TABLE dbo.don_hang_chi_tiet ADD ghi_chu_kich_thuoc NVARCHAR(300) NULL');
    IF COL_LENGTH('dbo.don_hang_chi_tiet','ten_kich_thuoc_chot') IS NULL
        EXEC(N'ALTER TABLE dbo.don_hang_chi_tiet ADD ten_kich_thuoc_chot NVARCHAR(100) NULL');
    IF COL_LENGTH('dbo.don_hang_chi_tiet','trang_thai_xac_nhan_size') IS NULL
        EXEC(N'ALTER TABLE dbo.don_hang_chi_tiet ADD trang_thai_xac_nhan_size VARCHAR(20) NOT NULL
          CONSTRAINT DF_ctdh_confirm_size DEFAULT ''KHONG_CAN'' WITH VALUES');

    -- Backfill pre-GD9 Freesize rows and snapshot existing order sizes.
    EXEC sys.sp_executesql N'
        UPDATE c SET kieu_kich_thuoc=''FREESIZE''
        FROM dbo.chi_tiet_gio_hang c
        JOIN dbo.san_pham_bien_the b ON b.id_bien_the=c.id_bien_the
        JOIN dbo.bien_the_kich_thuoc z ON z.id_kich_thuoc=b.id_kich_thuoc
        WHERE c.kieu_kich_thuoc=''CO_SAN'' AND LOWER(LTRIM(RTRIM(z.ten_kich_thuoc))) IN (N''freesize'',N''free size'');
        UPDATE ct SET
            ten_kich_thuoc_chot=COALESCE(ct.ten_kich_thuoc_chot,z.ten_kich_thuoc),
            kieu_kich_thuoc=CASE WHEN ct.kieu_kich_thuoc=''CO_SAN''
                AND LOWER(LTRIM(RTRIM(z.ten_kich_thuoc))) IN (N''freesize'',N''free size'')
                THEN ''FREESIZE'' ELSE ct.kieu_kich_thuoc END
        FROM dbo.don_hang_chi_tiet ct
        JOIN dbo.san_pham_bien_the b ON b.id_bien_the=ct.id_bien_the
        JOIN dbo.bien_the_kich_thuoc z ON z.id_kich_thuoc=b.id_kich_thuoc;';

    -- The old constraints made two different measurements of one variant impossible.
    IF EXISTS(SELECT 1 FROM sys.key_constraints
              WHERE parent_object_id=OBJECT_ID('dbo.chi_tiet_gio_hang') AND name='UQ_ctgh_unique')
        ALTER TABLE dbo.chi_tiet_gio_hang DROP CONSTRAINT UQ_ctgh_unique;
    IF NOT EXISTS(SELECT 1 FROM sys.indexes
                  WHERE object_id=OBJECT_ID('dbo.chi_tiet_gio_hang') AND name='UX_ctgh_sizing')
        EXEC(N'CREATE UNIQUE INDEX UX_ctgh_sizing ON dbo.chi_tiet_gio_hang
          (id_gio_hang,id_bien_the,kieu_kich_thuoc,chu_vi_co_tay_cm,do_rong_deo)');

    IF EXISTS(SELECT 1 FROM sys.key_constraints
              WHERE parent_object_id=OBJECT_ID('dbo.don_hang_chi_tiet') AND name='UQ_ctdh_bien_the')
        ALTER TABLE dbo.don_hang_chi_tiet DROP CONSTRAINT UQ_ctdh_bien_the;
    IF NOT EXISTS(SELECT 1 FROM sys.indexes
                  WHERE object_id=OBJECT_ID('dbo.don_hang_chi_tiet') AND name='UX_ctdh_sizing')
        EXEC(N'CREATE UNIQUE INDEX UX_ctdh_sizing ON dbo.don_hang_chi_tiet
          (id_don_hang,id_bien_the,kieu_kich_thuoc,chu_vi_co_tay_cm,do_rong_deo)');

    IF NOT EXISTS (SELECT 1 FROM sys.check_constraints
                   WHERE parent_object_id=OBJECT_ID('dbo.chi_tiet_gio_hang') AND name='CK_ctgh_size_mode')
        EXEC(N'ALTER TABLE dbo.chi_tiet_gio_hang WITH CHECK ADD CONSTRAINT CK_ctgh_size_mode CHECK (
          (kieu_kich_thuoc IN (''CO_SAN'',''FREESIZE'') AND chu_vi_co_tay_cm IS NULL AND do_rong_deo IS NULL)
          OR (kieu_kich_thuoc=''THEO_YEU_CAU'' AND chu_vi_co_tay_cm IS NOT NULL AND do_rong_deo IS NOT NULL
          AND chu_vi_co_tay_cm BETWEEN 10 AND 30
          AND do_rong_deo IN (''OM'',''VUA'',''RONG'')))');
    IF NOT EXISTS (SELECT 1 FROM sys.check_constraints
                   WHERE parent_object_id=OBJECT_ID('dbo.don_hang_chi_tiet') AND name='CK_ctdh_size_mode')
        EXEC(N'ALTER TABLE dbo.don_hang_chi_tiet WITH CHECK ADD CONSTRAINT CK_ctdh_size_mode CHECK (
          (kieu_kich_thuoc IN (''CO_SAN'',''FREESIZE'') AND chu_vi_co_tay_cm IS NULL AND do_rong_deo IS NULL)
          OR (kieu_kich_thuoc=''THEO_YEU_CAU'' AND chu_vi_co_tay_cm IS NOT NULL AND do_rong_deo IS NOT NULL
          AND chu_vi_co_tay_cm BETWEEN 10 AND 30
          AND do_rong_deo IN (''OM'',''VUA'',''RONG'')))');
    IF NOT EXISTS (SELECT 1 FROM sys.check_constraints
                   WHERE parent_object_id=OBJECT_ID('dbo.don_hang_chi_tiet') AND name='CK_ctdh_confirm_size')
        EXEC(N'ALTER TABLE dbo.don_hang_chi_tiet WITH CHECK ADD CONSTRAINT CK_ctdh_confirm_size CHECK (
          (kieu_kich_thuoc=''THEO_YEU_CAU'' AND trang_thai_xac_nhan_size IN (''CHO_XAC_NHAN'',''DA_XAC_NHAN''))
          OR (kieu_kich_thuoc<>''THEO_YEU_CAU'' AND trang_thai_xac_nhan_size=''KHONG_CAN''))');


    -- Track custom-size confirmations with a dedicated audit event.
    IF EXISTS(SELECT 1 FROM sys.check_constraints
              WHERE parent_object_id=OBJECT_ID('dbo.lich_su_don_hang')
                AND name='CK_lsdh_event'
                AND definition NOT LIKE '%XAC_NHAN_KICH_THUOC%')
    BEGIN
        ALTER TABLE dbo.lich_su_don_hang DROP CONSTRAINT CK_lsdh_event;
    END;
    IF NOT EXISTS(SELECT 1 FROM sys.check_constraints
                  WHERE parent_object_id=OBJECT_ID('dbo.lich_su_don_hang') AND name='CK_lsdh_event')
        EXEC(N'ALTER TABLE dbo.lich_su_don_hang WITH CHECK ADD CONSTRAINT CK_lsdh_event
          CHECK(su_kien IN (''CHUYEN_TRANG_THAI'',''HUY_DON'',''XAC_NHAN_THU_TIEN'',
             ''XAC_NHAN_HOAN_TIEN'',''XAC_NHAN_KICH_THUOC''))');

    -- Size name for product variants that are produced after order confirmation.
    IF NOT EXISTS (SELECT 1 FROM dbo.bien_the_kich_thuoc WHERE ten_kich_thuoc=N'Theo yêu cầu')
        INSERT INTO dbo.bien_the_kich_thuoc(ten_kich_thuoc,mo_ta)
        VALUES(N'Theo yêu cầu',N'Khách nhập chu vi cổ tay và độ rộng đeo; nhân viên phải xác nhận.');

    COMMIT TRANSACTION;
    PRINT N'GD9 hoàn thành. Mở quản lý biến thể để thêm size Theo yêu cầu cho sản phẩm hỗ trợ.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

-- Verify installed columns; idempotent when run again.
SELECT t.name AS Bang, c.name AS Cot
FROM sys.tables t JOIN sys.columns c ON c.object_id=t.object_id
WHERE (t.name='san_pham' AND c.name LIKE 'freesize_%')
   OR (t.name IN ('chi_tiet_gio_hang','don_hang_chi_tiet')
       AND c.name IN ('kieu_kich_thuoc','chu_vi_co_tay_cm','do_rong_deo',
                      'ghi_chu_kich_thuoc','ten_kich_thuoc_chot','trang_thai_xac_nhan_size'))
ORDER BY t.name,c.name;
GO
