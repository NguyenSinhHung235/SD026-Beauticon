# Giao diện BeautIcon sau khi tinh gọn

- Dựa trên dự án GD8 FakeData trước đó. Không thay đổi Java, SQL, API, luồng bán hàng hoặc dữ liệu.
- Đã bỏ những thành phần `class="hint"` chỉ hiển thị lời hướng dẫn tĩnh.
- Giữ các `hint` dùng để hiển thị dữ liệu Thymeleaf, tên nhân viên, tên sản phẩm, mã khách, số lượng, trạng thái, tiền thối hoặc trạng thái giỏ hàng do JavaScript quản lý.
- Rút gọn lời hướng dẫn cố định nằm kèm các thành phần dữ liệu động.
- Menu quản trị: `CON NGƯỜI` -> `NGƯỜI DÙNG`; `Danh mục & thuộc tính` -> `Thuộc tính`, giữ nguyên menu con và đường dẫn.
- Đồng nhất Times New Roman cho toàn bộ giao diện admin, trang cửa hàng, form đăng nhập và các bản xem trước demo.

## Chạy dự án

Xem `README.md` và `HUONG_DAN_GD8.md` để cấu hình SQL Server / Java; sử dụng `CHAY_DU_AN_WINDOWS.bat` theo hướng dẫn hiện có.
Lưu ý: thay đổi này không sửa script seed SQL trước đó.
