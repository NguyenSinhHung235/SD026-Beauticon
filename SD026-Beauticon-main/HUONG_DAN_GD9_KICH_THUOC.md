# BEAUTICON - GIAI DOAN 9: CHON KICH THUOC VONG TAY LINH HOAT

## 1. Tai lieu va thiet lap ban dau

Du an Java 17, Spring Boot, Thymeleaf va SQL Server. Bo sung 3 kieu chon kich thuoc:

- **Co san**: chon bien the co size cu the (15 cm, 16 cm...).
- **Freesize**: chi duoc chon khi nhan vien khai bao khoang dieu chinh thuc te tren san pham.
- **Theo yeu cau**: khach nhap **chu vi co tay (cm)**, chon do rong deo `OM`, `VUA`, `RONG` va ghi chu rieng. Cua hang xac nhan so do truoc khi xu ly don.

Chu vi co tay KHONG phai chieu dai thanh pham: nhan vien phai kiem tra thiet ke, kha nang gia cong, va xac nhan truc tiep voi khach.

## 2. Nang cap SQL Server hien co (TU GD8 SANG GD9)

1. **Sao luu database VongTayNu** trong SSMS truoc khi thay doi bat ky dieu gi.
2. Mo **`sql/11_migrate_giai_doan_9.sql`** trong SSMS, chon dung database, chon TOAN BO noi dung (Ctrl+A), va nhan **F5**. File co `BEGIN TRANSACTION` va `ROLLBACK` neu xay ra loi; co the chay lai neu da nang cap thanh cong.
3. Kiem tra thong bao `GD9 hoan thanh` va bang kiem tra cac cot duoc bo sung.
4. Khoi dong ung dung Java moi sau khi migration chay thanh cong. Nen chay `mvn clean test` va `mvn spring-boot:run`.

**KHONG** chay `sql/01_schema.sql` hoac seed len database da co du lieu: co the gay loi trung bang, trung khoa hoac anh huong du lieu. Script GD9 KHONG xoa don hang va giu lai thong tin don cu. Database van co **22 bang**.

**Neu khoi tao database trong:** chay `sql/01_schema.sql` moi, roi tuy chon `sql/02_seed_demo.sql` moi; KHONG can chay migration 11 tren database vua tao tu schema GD9.

## 3. Cau hinh san pham trong trang quan tri

- Vao **San pham -> Sua san pham**: neu san pham co day keo/dieu chinh duoc, khai bao **Freesize tu (cm)** va **den (cm)** (vi du 14-20 cm **chi neu do la khoang do thuc te**). Hai cot bo trong neu san pham khong ho tro Freesize.
- Vao **Thuoc tinh -> Kich thuoc**, kiem tra `Theo yeu cau` da duoc migration them vao; khong can tao size nay thu cong neu da ton tai.
- Vao **Bien the san pham**: voi san pham ho tro, tao bien the gan kich thuoc `Theo yeu cau`, mau sac, gia va so luong co the nhan gia cong; voi Freesize, tao bien the mang kich thuoc `Freesize`; kich thuoc co san van tao binh thuong.
- Chi nhung kieu size da co **bien the dang ban, con hang** moi xuat hien de khach chon. Freesize con yeu cau khoang dieu chinh da cau hinh.

Du lieu `sql/02_seed_demo.sql` co vi du Freesize va Theo yeu cau **chi cho database khoi tao moi**. Khong chay seed nay len database hien tai.

## 4. Luong dat hang

1. Khach vao trang chi tiet san pham, chon **Co san / Freesize / Theo yeu cau**, chon mau va phien ban, nhap chu vi co tay 10-30 cm (toi da 1 so thap phan) va do rong deo neu chon Theo yeu cau.
2. Gio hang va checkout hien thi ro so do. Hai lua chon khac so do cua cung mot bien the duoc luu **thanh hai dong**; kiem tra tong ton kho cua bien the, khong cong don sai.
3. Don hang luu *snapshot* `kieu_kich_thuoc`, `chu_vi_co_tay_cm`, `do_rong_deo`, `ghi_chu_kich_thuoc`, `ten_kich_thuoc_chot` cho tung chi tiet. Khach xem lai trong **Don hang cua toi**.
4. Nhan vien xem don online tai **Don hang online**, thay so do va ghi chu cua tung dong; bam **Xac nhan kich thuoc**. Trong khi con `CHO_XAC_NHAN`, he thong khong cho chuyen trang thai xu ly don. Hoat dong xac nhan duoc ghi vao lich su don.
5. Man hinh ban tai quay (POS) chua co form do rieng, nen **khong cho chon bien the Theo yeu cau** tai POS; dung luong dat online cho don may do.

## 5. Truong du lieu moi

- `dbo.san_pham`: `freesize_min_cm`, `freesize_max_cm`.
- `dbo.chi_tiet_gio_hang`: `kieu_kich_thuoc`, `chu_vi_co_tay_cm`, `do_rong_deo`, `ghi_chu_kich_thuoc`.
- `dbo.don_hang_chi_tiet`: bon cot nhu tren, them `ten_kich_thuoc_chot`, `trang_thai_xac_nhan_size`.
- Hai UNIQUE cho chi tiet gio va chi tiet don duoc mo rong: cung bien the nhung so do / do rong deo khac nhau khong con bi gop nham. SQL CHECK chan so do va kieu size khong hop le.
- `dbo.bien_the_kich_thuoc`: them gia tri danh muc `Theo yeu cau` neu chua ton tai.

## 6. Kiem thu va gioi han

- Chay `scripts/verify_project.py`, `scripts/run_java_smoke.sh`, `scripts/run_showroom_smoke.sh`, `scripts/run_shop_smoke.sh`, `scripts/run_size_smoke.sh` va `scripts/test_sizing_ui.py` trong moi truong phu hop.
- HTML, JavaScript duoc kiem tra bang browser tu dong; Java service duoc kiem tra bang javac va stub co san.
- **Chua kiem thu tich hop truc tiep voi SQL Server / Maven tren may cua nguoi dung.** Hay backup, chay migration, `mvn clean test`, va thu 3 don kich thuoc tren database ban sao truoc khi su dung du lieu that.
- Giao dien van su dung **Times New Roman** va giu lai ten menu **Nguoi dung** / **Thuoc tinh** cua ban GD8.
