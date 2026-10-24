<<<<<<< HEAD
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
=======
# SD026
>>>>>>> f528d5eab6c1ac4f50d613e47eb557f61dbcbb84
