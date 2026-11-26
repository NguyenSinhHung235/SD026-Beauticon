# BEAUTICON GD9 - KICH THUOC VONG TAY LINH HOAT

**Moi trong GD9:** 3 lua chon **Co san / Freesize co khoang dieu chinh / Theo yeu cau co chu vi co tay va do rong deo**. Chi tiet gio hang va don hang luu rieng tung lua chon; nhan vien xac nhan size dat rieng truoc khi xu ly don. Giao dien giu Times New Roman va menu da rut gon.

**NANG CAP DB DANG DUNG:** sao luu, chay DUY NHAT `sql/11_migrate_giai_doan_9.sql` tren GD8, sau do chay ung dung moi. **KHONG chay lai `01_schema.sql`/`02_seed_demo.sql` tren database cu.** Xem `HUONG_DAN_GD9_KICH_THUOC.md`.

---

# BeautIcon - Giai doan 8 (Admin + Storefront Online)

**Giai doan 8 da them:** dang ky/dang nhap khach, gio hang va bien the, dia chi, checkout COD/chuyen khoan cho doi soat, luu don online, trang theo doi don. Giu lai toan bo Admin GD1-7. **SQL Server van 22 bang**, them mot cot `don_hang.ma_yeu_cau_online` va unique index.

**Cap nhat GD7 -> GD8:** Sao luu DB, chay **`sql/08_migrate_giai_doan_8.sql`**, khong chay lai schema/seed. Neu khoi tao moi, chay `sql/01_schema.sql` GD8; khong can migration 08. Sau do `mvn clean test` va `mvn spring-boot:run`.

**Diem vao:** `/` (cua hang), `/dang-ky` (KH moi), `/dang-nhap` (KH), `/gio-hang`, `/thanh-toan`, `/don-hang-cua-toi`; Admin van o `/login` va `/admin/dashboard`. Xem `HUONG_DAN_GD8.md` va `KICH_BAN_KIEM_THU_GD8.md`.

**Thanh toan:** Dang mo phong quy trinh COD/chuyen khoan doi soat thu cong, khong co cong thanh toan that; `thanh_toan.trang_thai=CHO_XU_LY` den khi Admin xac nhan tien THUC SU da nhan. Chua kiem thu Maven + SQL Server thuc te trong moi truong tao ma.

---

# BeautIcon — Admin web bán vòng tay nữ (Giai đoạn 1–6)

## Phạm vi

Dự án Spring Boot 4 + Spring MVC + Data JPA/JDBC + Thymeleaf + SQL Server (22 bảng). Tất cả các tính năng giai đoạn 1–5 được giữ lại:

- Dashboard, lọc ngày/thống kê doanh thu.
- CRUD sản phẩm, biến thể, ảnh, màu sắc/kích thước/loại/thương hiệu/chất liệu/nhà cung cấp.
- CRUD khuyến mãi và voucher, POS có trừ kho/thanh toán, đơn hàng ONLINE, hóa đơn ONLINE/OFFLINE, hủy/hoàn kho/hoàn tiền thủ công.
- **GĐ5:** đăng nhập nhân viên có BCrypt + Spring Security; vai trò `QUAN_LY` (ROLE_ADMIN), `NHAN_VIEN` (ROLE_STAFF); CRUD nhân viên/khách hàng/địa chỉ, danh sách đơn của khách; khóa/mở lại thay vì xóa cứng.


## Giai đoạn 6 — QA và giao diện thống nhất

- Dashboard dùng cùng **menu/sidebar** với các trang quản lý khác (có khách hàng, nhân viên, đăng xuất); riêng `admin-dashboard.css` chỉ định dạng KPI/biểu đồ.
- Ẩn các nút thay đổi dữ liệu với `NHAN_VIEN` ở trang ảnh, thuộc tính, biến thể và khuyến mãi; các API POST vẫn được Spring Security chặn bằng 403.
- Bổ sung trang `templates/error/404.html`, cải thiện mã hóa nhãn trạng thái đơn; chỉnh kiểm tra giới hạn số tiền `DECIMAL(18,2)`.
- Giao dịch đổi/thêm/xóa ảnh chính có khóa theo sản phẩm trong transaction để giảm xung đột chỉ mục `UNIQUE` khi thao tác đồng thời.
- **Không thêm bảng hoặc cột**: DB vẫn 22 bảng, không có migration mới cho GĐ6.

### Kiểm tra tự động và trên máy của bạn

**Windows:** chạy `KIEM_TRA_GD6_WINDOWS.bat` (cần JDK; phần kiểm tra route cần Python).
**Linux/macOS:** `python3 scripts/verify_project.py` và `sh scripts/run_java_smoke.sh`.
**Maven:** `mvn clean test` rồi `mvn clean spring-boot:run` để kiểm thử ứng dụng đầy đủ.
**SSMS:** chạy `sql/07_kiem_tra_database.sql` trên DB đã chuẩn bị. Xem `KICH_BAN_KIEM_THU_GD6.md` cho 12 trường hợp cần xác nhận bằng thao tác thật.

Kết quả QA trong môi trường tạo mã: kiểm tra 22 bảng, 83 mapping, 158 URL của 26 template; 21 ca Java thuần JDK; JS POS có cú pháp hợp lệ. **Chưa chạy Maven, đăng nhập thật, hoặc kết nối SQL Server**; không coi các kiểm tra này là xác nhận production.

## 1. Cài đặt & cập nhật SQL Server

- **Chưa có DB:** Mở SSMS, chạy `sql/01_schema.sql` (22 bảng), tùy chọn `sql/02_seed_demo.sql` (chỉ chạy 1 lần trên DB mới).
- **Đã chạy GĐ4:** Sao lưu database, chạy **duy nhất `sql/06_migrate_giai_doan_5.sql`**. Không tạo lại database, không chạy lại file seed.
- Nếu từ GĐ3: chạy 05 trước 06; từ GĐ2: 04,05,06; từ GĐ1: 03,04,05,06.
- 06 bổ sung `dia_chi_giao_hang.trang_thai`, `CHECK` hai chức danh và index tra cứu SĐT. **Không thêm bảng mới**; vẫn 22 bảng.

## 2. Tạo tài khoản quản lý đầu tiên (không dùng mật khẩu mặc định)

Tài khoản seed `demo-staff@example.invalid` không có mật khẩu đăng nhập thật. Để tạo admin mới một lần, trong **PowerShell** tại thư mục dự án:

```powershell
$env:DB_USER="sa"
$env:DB_PASSWORD="YOUR_SQL_SERVER_PASSWORD"
$env:BOOTSTRAP_ADMIN_EMAIL="admin@beauticon.local"
$env:BOOTSTRAP_ADMIN_PASSWORD="REPLACE_WITH_YOUR_OWN_LONG_PASSWORD"
mvn clean spring-boot:run
```

Sau khi tạo thành công, **xóa ngay biến bootstrap ở phiên PowerShell dùng cho những lần sau**:

```powershell
Remove-Item Env:BOOTSTRAP_ADMIN_EMAIL
Remove-Item Env:BOOTSTRAP_ADMIN_PASSWORD
```

> Thực hiện xóa biến sau khi dừng lần chạy đầu; dự án chỉ đọc biến khi khởi động, không thay đổi mật khẩu của tài khoản đã tồn tại. Thay chuỗi `REPLACE_WITH_YOUR_OWN_LONG_PASSWORD` bằng mật khẩu riêng trước khi chạy. Mật khẩu phải 12–128 ký tự. Quản lý có thể đổi lại mật khẩu từ trang `/admin/nhan-vien/{id}/sua`.

Nếu ứng dụng chạy trên cổng khác, hãy đặt `PORT` cho phù hợp. DB mặc định `VongTayNu` tại SQL Server localhost:1433; cấu hình bằng `DB_URL` nếu cần. Xem `src/main/resources/application.properties`.

## 3. Đường dẫn (khắc phục 404)

| Chức năng | URL |
|---|---|
| Kiểm tra máy chủ | `http://localhost:8080/health` (không cần đăng nhập) |
| Đăng nhập | `http://localhost:8080/login` |
| Dashboard | `http://localhost:8080/admin/dashboard` |
| Nhân viên | `http://localhost:8080/admin/nhan-vien` |
| Thêm nhân viên | `/admin/nhan-vien/them` |
| Khách hàng | `http://localhost:8080/admin/khach-hang` |
| Thêm khách hàng | `/admin/khach-hang/them` |
| Chi tiết khách hàng, địa chỉ, lịch sử đơn | `/admin/khach-hang/{id}` |
| Sản phẩm | `/admin/san-pham` |
| Khuyến mãi | `/admin/khuyen-mai` |
| Voucher | `/admin/voucher` |
| POS | `/admin/ban-tai-quay` |
| Đơn hàng online | `/admin/don-hang-online` |
| Hóa đơn ONLINE/OFFLINE | `/admin/hoa-don` |

Mọi URL `/admin/**` cần đăng nhập. Khi chưa đăng nhập, chuyển tới `/login` (HTTP 302). Khi không đủ quyền, hiển thị trang 403. Nếu nhận 404, xác nhận `http://localhost:8080/health` và dừng tất cả instance cũ chạy port 8080. Xem `TROUBLESHOOT_404.md`.

## 4. Phân quyền

| Tác vụ | QUAN_LY | NHAN_VIEN |
|---|---|---|
| Xem Dashboard, sản phẩm, voucher/khuyến mãi | Có | Có |
| Thêm/sửa/ẩn sản phẩm, thuộc tính, voucher/khuyến mãi | Có | Không (403) |
| Bán POS, xử lý/hủy đơn online, ghi nhận thu tiền | Có | Có |
| Xác nhận hoàn tiền đã thực hiện | Có | Không (403) |
| Xem/thêm/sửa khách hàng và địa chỉ | Có | Có |
| Khóa/mở hồ sơ khách hàng | Có | Không (403) |
| Tạo/sửa/khóa nhân viên, đổi vai trò/đặt lại mật khẩu | Có | Không (403) |

Người xử lý nghiệp vụ đơn hàng/POS **lấy từ tài khoản đang đăng nhập**, không dùng `staffId` do người dùng sửa qua DevTools. Voucher ghi nhân viên tạo. Mật khẩu nhân viên dùng BCrypt; không có hiển thị mật khẩu hiện tại. Thay đổi trạng thái, quyền hoặc mật khẩu nhân viên sẽ vô hiệu hóa session cũ khi có request tiếp theo. Không thể tự khóa tài khoản hay hạ vai trò mình; cấm vô hiệu hóa quản lý hoạt động cuối cùng.

**Lưu ý nghiệp vụ:** Xóa khách/nhân viên/địa chỉ được triển khai bằng **khóa/ẩn (soft delete)** để không mất lịch sử bán hàng và khóa ngoại. Chưa triển khai storefront đăng ký/đăng nhập cho khách mua hàng; `mat_khau_hash` của khách không được CRUD admin sửa.

## 5. Kiểm thử và giới hạn

- Kiểm tra cú pháp Java bằng `javac --release 17` với stub API, HTML bằng bộ phân tích cú pháp, JavaScript với `node --check` và logic nhập liệu độc lập. **Đây không thay thế `mvn clean test` hoặc integration test với SQL Server thật.**
- Đã bật Spring Security/CSRF; POS gửi `X-CSRF-TOKEN` khi tạm tính qua AJAX. Form `th:action` của Thymeleaf tự gắn token cho POST.
- Chưa có gateway xác nhận chuyển khoản/hoàn tiền tự động, giới hạn tần suất đăng nhập, bảo vệ production HTTPS và kiểm thử nhiều người dùng tranh chấp. **Chỉ chạy local để hoàn thiện đồ án; không mở public khi chưa hardening/kiểm thử.**
- Nếu không dùng `BOOTSTRAP_ADMIN_*`, cần có ít nhất một tài khoản QUAN_LY với hash BCrypt hợp lệ. Seed demo không đăng nhập được.

## Các tệp quan trọng

```text
sql/01_schema.sql                   schema mới 22 bảng
sql/06_migrate_giai_doan_5.sql     chỉ chạy khi nâng cấp từ GĐ4
src/main/java/com/example/vongtay/security/    đăng nhập & phân quyền
src/main/java/com/example/vongtay/people/      CRUD nhân viên, khách, địa chỉ
src/main/resources/templates/auth/              login, denied
src/main/resources/templates/admin/people/      giao diện CRUD
TROUBLESHOOT_404.md
CHANGELOG_GD1_GD4.md                 tài liệu chi tiết các giai đoạn trước
```

---

## GD7 / Distinctive BeautIcon experience (October 2026)

**New public preview**: `GET /` and `GET /bo-suu-tap` open the actual, database-backed product collection. `GET /san-pham/{id}` opens a product and its available color/size variants. These routes are public GET only; they do not allow visitors to write to the database. Spring Security still protects `/admin/**` and staff actions.

**Scope:** Public browsing and product details are ready as a *read-only showroom*, not a fully implemented customer shop. There is NO customer cart/checkout/login endpoint yet. Avoid claiming otherwise in your demo.

**UI identity:** Warm paper, charcoal and copper; editorial serif display headings; a CSS-only bracelet illustration; branded admin navigation; responsive collapsible staff sidebar; redesigned dashboard and login; accessible focus styles and reduced-motion support. The product cards show real DB images when available. No fake ratings or fabricated testimonials are inserted.

**Images:** The existing Admin image form accepts either HTTPS image URLs or `/uploads/filename.jpg`. GD7 now serves `/uploads/**` as a read-only folder. From the project root:

1. Place your own photos at `uploads/my-bracelet-01.jpg` (or set the `UPLOADS_DIR` environment variable to an absolute existing image folder).
2. In Admin > Product > Images, enter `/uploads/my-bracelet-01.jpg`.
3. Reopen the product on `/` to see the photo. A product without an image displays a deliberate bracelet-shaped placeholder, **not** a fabricated product photo.

**SEO basics:** Public pages have individual titles, descriptions, semantic sections and a local favicon. `robots.txt` excludes the admin and login URLs. Set a real domain, actual product content, policies and canonical/sitemap strategy before public deployment.

**No SQL migration in GD7.** Schema stays at 22 tables, so upgrade users of GD6 only need to replace the app code and restart. Existing images and data remain untouched.

### GD7 smoke checks (not full integration)

```bash
python3 scripts/verify_project.py
sh scripts/run_java_smoke.sh
sh scripts/run_showroom_smoke.sh
node --check src/main/resources/static/js/admin-shell.js
node --check src/main/resources/static/js/storefront.js
mvn clean test
mvn spring-boot:run
```

The first three commands are lightweight checks (the showroom Java build uses **Spring API stubs** for syntax validation). `mvn clean test` and a running SQL Server are still required to validate the real framework/runtime. This generation environment did not have Maven or a SQL Server instance, so do NOT treat the project as production-tested.

### What to personalize before presenting this as your own graduation project

- Replace the BeautIcon sample visual identity if your store has its own official logo and name.
- Shoot and use actual bracelet photos (front, side, on-wrist, size reference), with consistent light and camera framing. Avoid synthetic stock photos pretending to be real inventory.
- Enter honest specifications: bracelet length in cm, materials, plating details, size options and actual stock.
- Edit every product description into natural Vietnamese based on the physical product; remove generic sales adjectives.
- Validate the full customer journey with 3-5 actual testers and fix what they find confusing; record design decisions and screenshots of your own tests for the thesis defense.
- Do not promise online checkout, shipping or warranty until those functions and policies have been implemented.

The `demo/` HTML files are **static visual samples using fabricated demonstration data marked as such**, not connected to SQL Server. Do not copy their sample counts or product names into factual reports.

See `HUONG_DAN_GD7.md` for short troubleshooting and presentation notes.
