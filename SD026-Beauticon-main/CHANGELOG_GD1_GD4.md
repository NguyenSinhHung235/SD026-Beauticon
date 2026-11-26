# Website bán vòng tay nữ — SQL Server + Admin Dashboard

## Phạm vi giai đoạn 1

- **21 bảng**: giữ **17 bảng ERD** và thêm `nha_cung_cap`, `chat_lieu`, `khuyen_mai`, `khuyen_mai_bien_the` để đáp ứng quản lý thuộc tính và chương trình khuyến mãi.
- Tách **voucher** (`ma_giam_gia`, giảm theo đơn) và **khuyến mãi** (`khuyen_mai`, áp dụng cho biến thể).
- **ONLINE/OFFLINE** đều nằm ở `don_hang.kenh_ban`, trạng thái `CHUA_XU_LY`, `DANG_XU_LY`, `DA_XU_LY`, `HOAN_THANH`, `DA_HUY`.
- Hóa đơn hoàn tất: tra cứu `don_hang` có `trang_thai = 'HOAN_THANH'`, theo `kenh_ban`.
- Thanh toán ghi riêng; doanh thu lấy theo **thanh toán thành công trừ hoàn tiền thành công**, không cộng `tong_tien` cho mọi đơn mới tạo.
- Dashboard **chỉ đọc (Read)** và lọc theo ngày. Không có Create/Update/Delete cho chỉ số thống kê vì số liệu được tổng hợp từ nghiệp vụ khác.

## Chạy dự án (JDK 17+, Maven, SQL Server)

1. Trong SSMS, mở và chạy **`sql/01_schema.sql`** trên SQL Server 2019+.
2. Nếu cần số liệu demo, chạy **`sql/02_seed_demo.sql`** **một lần trên DB trống**. Tài khoản demo KHÔNG đăng nhập được (mật khẩu giả), dùng cho thống kê.
3. Sửa `src/main/resources/application.properties` để trỏ tới SQL Server. Nên đặt biến môi trường `DB_USER`, `DB_PASSWORD`, `DB_URL` thay vì lưu mật khẩu trong file.
4. Chạy `mvn spring-boot:run` từ thư mục có `pom.xml`.
5. Mở **http://localhost:8080/admin/dashboard**.

Ví dụ URL kết nối local:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=VongTayNu;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=YOUR_LOCAL_PASSWORD
```

`trustServerCertificate=true` chỉ dùng local với chứng chỉ tự ký; production phải xác thực chứng chỉ TLS (`trustServerCertificate=false`).

**Nếu bạn đã có Spring Boot project**, chỉ sao chép các file Java trong `com/example/vongtay`, template `templates/admin/dashboard.html`, và SQL. Đổi package theo package dự án. Các dependencies user cung cấp phù hợp Spring Boot 4; `spring-boot-starter-data-jpa` đã kéo theo JDBC cho `NamedParameterJdbcTemplate`, nên không cần thêm dependency `spring-boot-starter-jdbc` riêng.

## Dashboard có gì?

- Bộ lọc ngày mặc định **7 ngày**, tối đa **91 ngày**.
- Cards: **doanh thu thực thu**, tổng đơn, hoàn thành, tồn kho thấp (ngưỡng ≤5), doanh thu online/offline, khách mới, đơn hủy.
- Biểu đồ doanh thu từng ngày, đếm đơn chưa/đang/đã xử lý, hoàn thành, top 5 sản phẩm bán chạy và 10 đơn mới nhất.
- Bộ lọc ngày được tính theo **Asia/Ho_Chi_Minh**; các `DATETIME2` trong DB là **UTC**.
- **Định nghĩa khác nhau:** doanh thu = theo thời điểm thanh toán; tổng đơn = theo thời điểm tạo đơn; top bán chạy = chi tiết các đơn `HOAN_THANH` lọc theo thời điểm đặt.

## Ràng buộc đã có

- `gio_hang`: `UNIQUE(id_khach_hang)` — 1 khách tối đa 1 giỏ.
- `san_pham_bien_the`: `UNIQUE(id_san_pham,id_mau_sac,id_kich_thuoc)` và tồn kho không âm.
- `don_hang`: `id_ma_giam_gia`/`id_nhan_vien` cho phép NULL; khách tại quầy có thể NULL.
- `danh_gia`: `UNIQUE(id_dh_chi_tiet)` — mỗi dòng mua tối đa 1 đánh giá.
- `chi_tiet_gio_hang`: `UNIQUE(id_gio_hang,id_bien_the)`.
- `dia_chi_giao_hang`: kiểm tra địa chỉ thuộc đúng khách hàng qua FK kép, snapshot địa chỉ trên đơn để giữ lịch sử.
- `hinh_anh_sp`: chỉ một ảnh chính cho mỗi sản phẩm.

## Ghi chú để triển khai phần CRUD tiếp theo

Phần SQL hoàn chỉnh ở mức **schema + ràng buộc**. Cần triển khai trong **Service + @Transactional**: trừ/hoàn tồn kho tránh race condition, cập nhật `tien_hang`, quản lý voucher và hoàn tiền, chống trùng đơn, chính sách khuyến mãi, xác nhận đánh giá chỉ sau khi đơn hoàn thành. Không nên làm điều này bằng CRUD `save()` đơn thuần. Tên quan hệ trong schema khớp ERD; cột ảnh / tên / khối lượng có chuẩn hóa ký pháp snake_case.

**Bảo mật:** Giai đoạn 1 **chưa tích hợp đăng nhập/phân quyền admin**. Route `/admin/dashboard` **chưa được bảo vệ**. Trước khi triển khai thật, thêm `spring-boot-starter-security`, hash mật khẩu BCrypt/Argon2, giới hạn quyền admin/nhân viên, CSRF và kiểm thử xác thực thanh toán. Tuyệt đối không public project bản này ra Internet.

**Giai đoạn 2 dự kiến:** CRUD sản phẩm & biến thể, màu/kích thước/chất liệu/nhà cung cấp → khuyến mãi/voucher → POS → đơn hàng online & hóa đơn → nhân viên/khách hàng.

---

## Giai đoạn 2 — CRUD sản phẩm, biến thể, thuộc tính và ảnh

**Đã triển khai trong cùng mã nguồn giai đoạn 1** (không phải project mới):

| Chức năng | Đường dẫn admin | Thao tác |
|---|---|---|
| Sản phẩm | `/admin/san-pham` | Danh sách phân trang, tìm kiếm, lọc trạng thái, thêm, sửa, ẩn/khôi phục |
| Biến thể | `/admin/san-pham/{id}/bien-the` | Thêm/sửa màu, kích thước, giá và tồn kho; ẩn/khôi phục |
| Ảnh sản phẩm | `/admin/san-pham/{id}/hinh-anh` | Thêm/sửa URL ảnh, xóa ảnh, chọn ảnh đại diện |
| Loại sản phẩm | `/admin/thuoc-tinh/loai` | Thêm/sửa/ẩn/khôi phục |
| Màu sắc | `/admin/thuoc-tinh/mau-sac` | Thêm/sửa/ẩn/khôi phục |
| Kích thước | `/admin/thuoc-tinh/kich-thuoc` | Thêm/sửa/ẩn/khôi phục |
| Thương hiệu | `/admin/thuoc-tinh/thuong-hieu` | Thêm/sửa/ẩn/khôi phục |
| Nhà cung cấp | `/admin/thuoc-tinh/nha-cung-cap` | Thêm/sửa/ẩn/khôi phục |
| Chất liệu | `/admin/thuoc-tinh/chat-lieu` | Thêm/sửa/ẩn/khôi phục |

> **Nếu database đã tạo ở GĐ1**: CHỈ chạy `sql/03_migrate_giai_doan_2.sql` trong SSMS, không chạy lại `01_schema.sql` và KHÔNG chạy lại seed. Script này bổ sung cột `trang_thai` cho `thuong_hieu`, `chat_lieu` và cột `mo_ta` cho `nha_cung_cap`. Giữ nguyên dữ liệu đã có. Nếu tạo database **mới**, chạy bản `01_schema.sql` trong gói ZIP cập nhật rồi `02_seed_demo.sql` (nếu cần), không cần `03`.

### Luồng sử dụng demo

1. Mở `/admin/thuoc-tinh/loai` để thêm loại sản phẩm (nếu database chưa có loại).
2. Vào `/admin/thuoc-tinh/mau-sac` và `/admin/thuoc-tinh/kich-thuoc` để tạo màu, size; có thể tạo `Freesize` cho vòng tay không phân size.
3. Vào `/admin/san-pham` → **Thêm sản phẩm**, điền thông tin chung; khi lưu, hệ thống chuyển đến trang **Biến thể**.
4. **Thêm biến thể** → chọn màu/kích thước, giá bán và tồn kho. Mỗi bộ `(id_san_pham,id_mau_sac,id_kich_thuoc)` là duy nhất theo UQ của SQL.
5. Vào **Ảnh** để lưu các đường dẫn HTTPS hoặc `/uploads/...` (file upload thật chưa được triển khai).
6. Nếu muốn ngừng bán, nhấn **Ẩn** ở sản phẩm hoặc biến thể. Đừng xóa bản ghi đã có lịch sử bán.

### Cấu trúc mã nguồn giai đoạn 2

```text
src/main/java/com/example/vongtay/catalog/
  Product.java, ProductVariant.java       JPA Entities
  ProductRepository.java                  JPA + tìm kiếm phân trang
  VariantRepository.java                  JPA + tổng tồn và kiểm trùng
  ProductForm.java, VariantForm.java      DTO nhập liệu
  CatalogService.java                     Validate, @Transactional, nghiệp vụ CRUD
  ProductController.java                  Endpoints sản phẩm
  VariantController.java                  Endpoints biến thể
  AttributeKind.java, AttributeForm.java  Whitelist loại thuộc tính + DTO
  AttributeService.java                   SQL parameter hóa, CRUD 6 loại thuộc tính
  AttributeController.java                Endpoints thuộc tính
  ProductImageService.java                CRUD URL ảnh + chọn ảnh chính
  ProductImageController.java             Endpoints hình ảnh
src/main/resources/templates/admin/
  products/, variants/, attributes/, fragments/nav.html
src/main/resources/static/css/admin-catalog.css
sql/03_migrate_giai_doan_2.sql
```

### Quy tắc dữ liệu

- `san_pham`, `san_pham_bien_the`, các danh mục: **xóa mềm** bằng `trang_thai=0`, có nút khôi phục. Điều này giữ toàn vẹn khóa ngoại của đơn hàng và lịch sử bán.
- `hinh_anh_sp` không có bảng nghiệp vụ tham chiếu, nên hỗ trợ **xóa cứng URL ảnh**. Xóa ảnh chính sẽ tự chọn ảnh còn lại đầu tiên làm ảnh chính.
- Không cho sửa **màu/kích thước** của biến thể đã xuất hiện trong `don_hang_chi_tiet`, tránh làm thay đổi mô tả sản phẩm trong đơn đã mua. Vẫn có thể sửa giá bán (giá chốt từng đơn đã được snapshot ở `don_hang_chi_tiet.gia_ban`).
- **Chưa có nhập/xuất kho theo phiếu**. Khi làm POS và đặt hàng online, phải trừ/hoàn tồn kho **atomically** và ghi nhật ký để không bị mất dữ liệu khi nhiều người mua cùng lúc.
- Danh mục bị ẩn vẫn hiển thị trong dropdown chỉnh sửa của sản phẩm cũ; khi xây storefront cần lọc trạng thái `trang_thai=1` của sản phẩm + biến thể và nhóm thuộc tính.

### Bảo mật và mức kiểm thử

**Mã nguồn này phục vụ phát triển và trình diễn trên máy local.** Route `/admin/**` hiện vẫn **chưa có xác thực/phân quyền** như giai đoạn 1. Không public lên Internet. Trước triển khai thật phải bổ sung Spring Security, quyền ADMIN/STAFF, CSRF, quản lý phiên, kiểm tra giới hạn chỉnh tồn kho, và kiểm thử tích hợp SQL Server. Các form POST hiện chưa được bảo vệ bởi lớp xác thực/CSRF.

Mã source được rà soát cú pháp và tính nhất quán của cột SQL; môi trường thực thi không có SQL Server/Maven nên **chưa xác nhận chạy qua `mvn test` hoặc kết nối cơ sở dữ liệu thật**. Với local cài Maven/JDK 17+ và SQL Server, chạy `mvn clean spring-boot:run` để kiểm thử giao diện.

**Các bước tiếp theo chưa có CRUD**: khuyến mãi/voucher, POS, đơn online/hóa đơn online-offline, khách hàng, nhân viên và phân quyền.

---

## Giai doan 3 - Khuyen mai, Voucher, POS (BAN CAP NHAT NAY)

**Project da bao gom giai doan 1 + 2 + 3.** Mo project tai thu muc goc co `pom.xml`.

### Nang cap SQL Server (KHONG xoa du lieu cu)

- **Neu da co database sau GD2:** chi chay `sql/04_migrate_giai_doan_3.sql` trong SQL Server Management Studio, sau do chay lai ung dung. Migration idempotent, bo sung `ma_yeu_cau_pos`, `tien_khach_dua`, `tien_thoi_lai`, unique request token, CHECK tien mat cho `don_hang`.
- **Neu tao database moi:** chay `sql/01_schema.sql` (ban trong ZIP GD3) roi tuy chon `sql/02_seed_demo.sql`. KHONG chay 03/04 khi tao moi bang `01_schema.sql` GD3.
- **Neu database o GD1 va chua nang GD2:** chay `03_migrate_giai_doan_2.sql` truoc `04_migrate_giai_doan_3.sql`.

### Duong dan man hinh

| Module | URL | Nghiep vu |
|---|---|---|
| Khuyen mai | `/admin/khuyen-mai` | Danh sach, them, sua, tam dung/bat lai, gan nhieu bien the |
| Voucher | `/admin/voucher` | Danh sach, them, sua, tam dung/bat lai, gioi han han dung/so luot |
| Ban tai quay | `/admin/ban-tai-quay` | Chon bien the, so luong, khach, nhan vien, voucher, thanh toan |
| Hoa don vua ban | `/admin/ban-tai-quay/{id}/hoa-don` | Xem/ in hoa don OFFLINE HOAN_THANH da thanh toan |
| Dashboard cu | `/admin/dashboard` | Doanh thu va so don bao gom giao dich POS thanh cong |

### Quy tac dinh gia

1. Khuyen mai ap dung **theo bien the**, hieu luc khi `trang_thai=1` va `ngay_bat_dau <= nowUtc < ngay_ket_thuc`.
2. Neu nhieu khuyen mai trung nhau tren mot bien the, lay **muc giam tien lon nhat**; khong cong don. Gia sau khuyen mai duoc luu lam `don_hang_chi_tiet.gia_ban` (snapshot gia chot).
3. `don_hang.tien_hang` = tong gia sau khuyen mai. Voucher ap dung **sau khuyen mai**, dieu kien don toi thieu tinh tren `tien_hang`. `don_hang.tien_giam` = voucher, `tong_tien` duoc SQL Server tinh. Don 0 dong chua ho tro POS.
4. Voucher duoc khoa hang SQL Server `UPDLOCK,HOLDLOCK` va tang `so_luong_da_dung` trong cung transaction voi checkout; voucher khong hop le se rollback toan bo.
5. POS tao `don_hang.kenh_ban='OFFLINE'`, `trang_thai='HOAN_THANH'`, mot dong `thanh_toan` THANH_CONG. Tien mat bat buoc du tien; chuyen khoan phai duoc nhan vien xac nhan **da doi chieu tien nhan**. Module nay KHONG ket noi cong thanh toan ngan hang.
6. Moi bien the duoc kiem tra va tru ton kho **trong transaction**, khoa ghi va UPDATE co dieu kien `so_luong_ton>=so_luong`, giao dich that bai se rollback ton + voucher + hoa don.
7. `ma_yeu_cau_pos` UNIQUE theo UUID giup tranh tao trung hoa don khi gui lai cung ma phien. Neu hai POST dong thoi voi cung ma, unique database ngan viec tru ton hai lan; request thu hai co the nhan loi xung dot va can mo hoa don da tao.
8. Khi admin sua bien the, form luu `expectedStock` va service kiem tra so ton hien tai bang row lock. Neu POS vua ban hang lam thay doi so ton, admin phai tai lai form thay vi ghi de ton kho cu.

### Cach thu nhanh voi du lieu demo

1. Ban demo co khuyen mai 5% cho mot so bien the va voucher `WELCOME10` (dieu kien gia tri don toi thieu).
2. Vao `/admin/khuyen-mai` de xem/bat/tat khuyen mai; vao `/admin/voucher` de sua han, gioi han va cap voucher.
3. Vao `/admin/ban-tai-quay` chon nhan vien, them bien the con hang va nhap so luong.
4. Neu tien mat, nhap tien khach dua >= tong tien thuc te. Neu chuyen khoan, chi danh dau xac nhan sau khi tien da vao ngan hang.
5. Bam tao hoa don, in, sau do xem `/admin/dashboard` de kiem tra doanh thu offline.
6. Kiem tra SQL: `SELECT TOP 10 * FROM dbo.don_hang WHERE kenh_ban='OFFLINE' ORDER BY id_don_hang DESC;` va `SELECT * FROM dbo.thanh_toan WHERE ma_giao_dich LIKE 'POS-%';`.
7. Can luu y **gia tam tinh trong giao dien JS chua tru voucher**; server la nguon quyet dinh cuoi cung. Gia/ton/co hieu luc co the doi giua luc them vao gio va thanh toan, khi do server bao loi va rollback.

### Can trien khai truoc khi dua len Internet

- Route `/admin/**` **chua co dang nhap/phan quyen** tu GD1-2. Chuong trinh chi danh cho **development local**. Can Spring Security, phan quyen NHAN_VIEN/QUAN_LY, CSRF, session, rate limit va audit log cho thao tac thu tien.
- Chuyen khoan chi danh dau thanh cong theo **xac nhan thu cong**, khong tu kiem tra ngan hang. Khong coi la thanh toan online tu dong.
- Chua lam hoan tien, huy hoa don, quan ly online, khach hang/nhan vien, bao cao cong no va nhat ky nhap/xuat kho. Se lam trong cac giai doan tiep theo.
- So voucher da su dung chi tang khi ban POS trong GD3; hoan/huy don trong tuong lai can nghiep vu hoan coupon va ton kho theo transaction va phuong thuc thanh toan.
- Trang khuyen mai chi load 500 bien the de chon; trang POS load 300 bien the dang ban. Khi du lieu lon, can tim kiem va phan trang server-side.
- Voi suat bat gian hang that, nen them bang nhat ky giao dich kho, snapshot ten SP/size/mau tren chi tiet hoa don, phan quyen va test tich hop SQL Server.

### Danh sach source giai doan 3

```text
src/main/java/com/example/vongtay/sales/
  SaleSupport.java
  PromotionForm.java, PromotionService.java, PromotionController.java
  VoucherForm.java, VoucherService.java, VoucherController.java
  PosForm.java, PosService.java, PosController.java
src/main/resources/templates/admin/promotions/{index,form}.html
src/main/resources/templates/admin/vouchers/{index,form}.html
src/main/resources/templates/admin/pos/{index,receipt}.html
sql/04_migrate_giai_doan_3.sql
src/test/java/com/example/vongtay/sales/SaleSupportTest.java
```

### Kiem thu

- Da kiem tra Java moi **bang javac --release 17 voi cac stub cua Spring**, HTML bang parser va thu nghiem doc lap logic chiet khau/mui gio. **Khong co Maven va SQL Server trong moi truong dong goi**, nen chua the khang dinh `mvn test` hay qua test tich hop voi SQL Server.
- Tren may cua ban: chay `mvn clean test` sau do `mvn spring-boot:run`; thu tao don va 2 giao dich canh tranh cho 1 bien the/voucher tren SQL Server that.

**POS quote:** `POST /admin/ban-tai-quay/tam-tinh` voi JSON `{ "items": [{"variantId": 1, "quantity": 2}], "voucherCode": "WELCOME10" }` se tra ve `subtotal`, `voucherDiscount`, `total` hoac `error`. Day la preview doc-only, chua tru ton/chua su dung voucher.

---

# Giai doan 4 - Don online, hoa don online/offline va sua link 404

**Day la ban GD4, bao gom GD1 + GD2 + GD3.** Database gom **22 bang**: 21 bang truoc day va 1 bang moi `lich_su_don_hang` de ghi nhat ky thao tac. Khong thay doi ten bang trong ERD.

## Huong dan update database

- **Da dung GD3 tren SQL Server:** Sao luu database, sau do chay **`sql/05_migrate_giai_doan_4.sql`** 1 lan trong SSMS. Khong chay lai `01_schema.sql` hay demo seed.
- **Database moi:** Chay **`sql/01_schema.sql`** ban GD4; tuy chon chay `02_seed_demo.sql` **mot lan**. Khong chay migration 03-05 neu schema da la GD4.
- **GD2:** lan luot 04 roi 05; **GD1:** 03, 04, 05.
- Migration 05 giu du lieu cu, them `ton_kho_da_tru`, `voucher_da_su_dung`, ngay cap nhat, ngay huy/hoan thanh, ly do huy, ma doi soat va nhat ky.

**Quan trong voi du lieu cu:** GD3 da tru kho cho POS; migration danh dau `ton_kho_da_tru=1` doi voi hoa don POS co `ma_yeu_cau_pos`. **Voi ONLINE cu**, he thong KHONG doan da tru kho hay chua, mac dinh la 0. Neu ban da co luong checkout online tru kho tu truoc, can DOI SOAT `don_hang.ton_kho_da_tru=1` cua tung don da tru kho truoc khi su dung xu ly/huy don; neu khong co the tru kho lan 2. Tuong tu, neu voucher online cu da tieu luot, phai doi soat `voucher_da_su_dung=1`. Nhung luong checkout online moi phai ghi 2 co nay trong cung transaction voi tru kho/voucher.

## URL da co Controller

| Trang | URL |
|---|---|
| Trang chu | `/` hoac `/admin/dashboard` |
| Don hang online, 5 trang thai | `/admin/don-hang-online` |
| Chi tiet, xu ly, huy don | `/admin/don-hang-online/{id}` |
| Hoa don HOAN_THANH ca 2 kenh | `/admin/hoa-don` |
| Hoa don Online | `/admin/hoa-don?kenh=ONLINE` |
| Hoa don POS | `/admin/hoa-don?kenh=OFFLINE` |
| Chi tiet/in hoa don | `/admin/hoa-don/{id}` |
| POS GD3 | `/admin/ban-tai-quay` |

**Sua 404:** co `/health` de kiem tra ung dung. `/`, `/dashboard`, `/admin/`, `/admin/don-hang`, `/admin/pos`, `/admin/hoa-don/online`, `/admin/hoa-don/offline` duoc redirect ve URL chinh. Menu Dashboard GD1 da duoc thay cac link thuc; menu chung da co trang don online/hoa don. Doc `TROUBLESHOOT_404.md` neu van gap loi.

## Nghiep vu GD4

1. **ONLINE:** `CHUA_XU_LY -> DANG_XU_LY -> DA_XU_LY -> HOAN_THANH`; phai nhan du tien truoc khi HOAN_THANH. Khi nhan xu ly, neu don chua tru kho thi tru kho atomically trong transaction. Neu khong du ton, rollback va giu nguyen trang thai.
2. **Huy:** Chi cho huy don ONLINE chua HOAN_THANH. Neu da tru kho thi hoan ton; voucher chi hoan luot neu `voucher_da_su_dung=1`. Da huy khong huy lan 2.
3. **Hoan tien:** Neu don da thu tien, huy don tao ban ghi `HOAN_TIEN/CHO_XU_LY`. KHONG tu dong chuyen khoan, KHONG tu danh dau thanh cong. Khi da hoan thuc te, nhan vien xac nhan va dien ma doi soat; luc do moi ghi `THANH_CONG`.
4. **Thu tien thu cong:** Chi cho nhan vien xac nhan sau khi da doi soat tien mat/COD hoac chuyen khoan. Cac giao dich thu co ma idempotent rieng, ma doi soat cua tung phuong thuc la duy nhat.
5. **Hoa don:** Chi lay `HOAN_THANH`, loc ONLINE/OFFLINE, tim kiem, xem chi tiet va in. Khong xoa cung hoa don thanh cong. POS GD3 giu nguyen.
6. **Nhat ky:** Luu su kien chuyen trang thai, huy, xac nhan thu/hoan tien va nguoi thao tac.

**Han che:** Module admin chua co Spring Security/xac thuc, chi danh cho localhost. Khong co gateway thuc, giao nhan van chuyen, COD API hay chuyen tien tu dong. Chua co storefront checkout online (don online demo co san trong seed). Chua test Maven/SQL Server that tai moi truong dong goi. Phai them integration test truoc trien khai kinh doanh.

## Thu nhanh

1. Chay database va Maven theo phan dau README.
2. Vao `http://localhost:8080/` -> Dashboard (kiem tra loi 404 da het).
3. Vao `/admin/don-hang-online` -> xem don #demo o trang thai CHUA_XU_LY; chi tiet -> chon nhan vien -> chuyen trang thai. Ton kho duoc tru khi xu ly lan dau.
4. Thu huy 1 don chua hoan thanh, kiem tra ton kho da hoan va nhat ky.
5. Vao `/admin/hoa-don`, loc ONLINE/OFFLINE, xem chi tiet/in.
6. Chay `mvn clean test`; neu gap loi, xem terminal Java/SQL Server va huong dan `TROUBLESHOOT_404.md`.
