# Kịch bản kiểm thử tại máy người dùng — BeautIcon GĐ6

> Chạy trên **SQL Server thử nghiệm**, không dùng dữ liệu thật. Trước khi chạy lại dữ liệu mẫu, hãy tạo DB mới. Các kiểm thử Java/tĩnh tự động không thay thế những ca kiểm tra này.

## Chuẩn bị

1. Máy Windows cài JDK 17+ và Maven; SQL Server đang chạy.
2. SSMS: chạy `sql/01_schema.sql` và *chỉ trên DB mới* chạy `sql/02_seed_demo.sql`; **hoặc** nâng cấp DB cũ theo đúng thứ tự GD2 → GD5.
3. Trên DB đã chuẩn bị, chạy `sql/07_kiem_tra_database.sql` (chỉ đọc).
4. Tạo một tài khoản `QUAN_LY` thật bằng `BOOTSTRAP_ADMIN_EMAIL` / `BOOTSTRAP_ADMIN_PASSWORD`; sau lần chạy đầu, gỡ biến môi trường đó.
5. Mở `http://localhost:8080/health`, `/login`, sau đó đăng nhập `/admin/dashboard`.

## 12 ca quan trọng

| # | Thao tác kiểm thử | Kết quả mong đợi |
|---|---|---|
| 1 | Mở `/`, `/dashboard`, `/admin`, `/admin/dashboard` | Chuyển hướng/hiển thị Dashboard; không 404 |
| 2 | Mở `/admin/don-hang-online`, `/admin/hoa-don`, `/admin/ban-tai-quay` | Các trang tải được; nhân viên chưa đăng nhập về `/login` |
| 3 | Đăng nhập Staff, thử mở `/admin/nhan-vien` và POST sửa sản phẩm | Nhận 403; không thay đổi dữ liệu |
| 4 | Đăng nhập Admin, tạo sản phẩm và biến thể màu/kích thước/giá/tồn | Tạo thành công; biến thể trùng màu + size bị chặn |
| 5 | Sửa ảnh, đặt ảnh chính; mở lại trang sản phẩm | Tối đa một ảnh chính; Staff chỉ xem, không được sửa |
| 6 | Tạo voucher; thử voucher hết hạn, không đủ giá trị, hết lượt | Thông báo hợp lệ; không phát sinh đơn/hóa đơn |
| 7 | Tạo POS tiền mặt, đưa số tiền lớn hơn tổng | Hóa đơn OFFLINE hoàn thành; tiền thối đúng; tồn kho giảm |
| 8 | Tạo POS cùng `requestToken` lần nữa | Trả lại đơn cũ, không tạo hai đơn/trừ tồn lần 2 |
| 9 | POS vượt tồn kho hiện tại, hoặc 2 người bán cùng biến thể cuối | Không tồn kho âm, giao dịch lỗi được rollback |
| 10 | Đơn ONLINE: chưa xử lý → đang xử lý → đã xử lý → hoàn thành | Chỉ hoàn thành khi đã ghi nhận đủ tiền; lịch sử thao tác được lưu |
| 11 | Hủy đơn ONLINE đã giữ tồn và đã nhận tiền | Hoàn tồn 1 lần, tạo HOAN_TIEN `CHO_XU_LY`; chỉ Admin xác nhận hoàn tiền thật |
| 12 | Thêm/sửa/ẩn khách và nhân viên; đổi mật khẩu nhân viên | Không xóa đơn cũ; tài khoản bị khóa không truy cập lại được; không khóa admin cuối |

## Checklist khi phát hiện lỗi

- Chụp ảnh màn hình, ghi **URL đầy đủ**, quyền đang đăng nhập, thời điểm, ID đơn/sản phẩm (không gửi mật khẩu).
- Lấy phần log exception trong cửa sổ Spring Boot, mã HTTP (302, 403, 404 hay 500), và tên SQL migration gần nhất.
- 404: kiểm tra `http://localhost:8080/health`, có chạy nhầm project/port không.
- 403: kiểm tra vai trò/CSRF thay vì tạo thêm URL.
- 500: xem `Caused by:` trong log; đối chiếu schema, SQL migration và dữ liệu.
