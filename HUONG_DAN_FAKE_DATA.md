# Fake data GD8 (10 bản ghi mới mỗi bảng)

- Sao lưu database trước. Chạy trên SQL Server đã nâng cấp đến GD8, sau script 08.
- Chạy `sql/10_fake_data_10_moi_bang.sql` **một lần** bằng SSMS. Script không xóa dữ liệu hiện tại; mỗi bảng thêm 10 bản ghi, bao gồm bảng liên kết khuyến mãi và lịch sử đơn hàng.
- Copy `uploads/beauticon-demo-01.svg` ... `beauticon-demo-10.svg` vào đúng thư mục `uploads` mà ứng dụng cấu hình qua `UPLOADS_DIR` (thư mục dự án hoặc đường dẫn tuyệt đối).
- Mở `/admin/dashboard` và trang sản phẩm để xem dữ liệu.
- Tất cả tên, thương hiệu, địa chỉ, email và số điện thoại là **dữ liệu giả lập**, không xác thực và không đại diện người thật. Email `.invalid`, điện thoại bắt đầu `000` để tránh vô tình liên hệ số thực.
- Nhân viên demo bị khóa; khách demo không có mật khẩu nên không thể đăng nhập. Đừng dùng các tài khoản này để test đăng nhập; tạo tài khoản qua form.
- Các đơn minh họa đều HOAN_THANH để 10 đánh giá có nguồn gốc đơn hoàn thành. Muốn thử luồng trạng thái khác, hãy tạo đơn qua giao diện.
- Không dùng các đánh giá giả lập này như lời chứng thực khách hàng thật khi triển khai công khai.
- Không chạy lại script; nếu phát hiện voucher `BCDEMO2026-01` script sẽ từ chối.
