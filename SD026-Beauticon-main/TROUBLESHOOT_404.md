# Khac phuc loi 404 - BeautIcon Admin GD6

## Link nao hoat dong sau khi chay Spring Boot?

Dung `http://localhost:8080` hoac `http://localhost:8080/admin/dashboard`.

| Link | Ket qua |
|---|---|
| `http://localhost:8080/` | Neu chua dang nhap redirect den `/login`; sau dang nhap ve Dashboard |
| `/dashboard`, `/admin/` | Redirect den Dashboard |
| `/admin/san-pham` | Danh sach san pham |
| `/admin/khuyen-mai` | Khuyen mai |
| `/admin/voucher` | Voucher |
| `/admin/ban-tai-quay` va `/admin/pos` | POS |
| `/admin/don-hang-online` | Danh sach don online |
| `/admin/don-hang-online/{id}` | Chi tiet don online |
| `/admin/don-hang` | Redirect den don online |
| `/admin/hoa-don` | Hoa don HOAN_THANH (online/offline) |
| `/admin/hoa-don/online` | Redirect den hoa don kenh ONLINE |
| `/admin/hoa-don/offline` | Redirect den hoa don kenh OFFLINE |
| `/admin/hoa-don/{id}` | Xem hoa don HOAN_THANH |
| `/admin/hoa-don/{id}/in` | In hoa don |

## 6 buoc tim nguyen nhan 404

1. Truy cap `http://localhost:8080/` (khong dung `file:///...`, khong bam truc tiep file HTML trong thu muc `templates`).
2. Mo `http://localhost:8080/health` de xac nhan server dang phuc vu route; neu bao `404`, ban dang chay sai ban project/port; neu khong ket noi duoc, server chua chay. Chay dung project GD6, mo thu muc goc co `pom.xml`, chay `mvn clean spring-boot:run`.
3. Dung ung dung GD1-GD3 dang chay tren port 8080 truoc khi khoi dong GD6; neu khong, trinh duyet co the van ket noi vao server cu va bao 404. Kiem tra terminal co dong `Started VongTayApplication` va port 8080. Neu doi `PORT`, doi port trong URL.
4. Kiem tra `src/main/java/com/example/vongtay/orders/OrderController.java` va `RouteRedirectController.java` nam trong package `com.example.vongtay` de Spring quet duoc controller.
5. Neu URL `/admin/don-hang-online/1` khong ton tai, web se quay ve danh sach va bao loi: hay xem ID thuc tren trang danh sach.
6. Neu SSMS chua chay migration GD4, ung dung co the loi truy van `Invalid column name`, thuong la **HTTP 500** chu khong phai 404. Chay `sql/05_migrate_giai_doan_4.sql`.

### Loi 404 do link tai file ChatGPT

Khong su dung duong dan `file:///mnt/data/...` tren may Windows. Bam link `sandbox:` trong tin nhan ChatGPT de tai file ZIP; neu client khong mo duoc, tim tep ZIP duoc dinh kem trong tin nhan. File ZIP GD6 bao gom SQL va source, khong can bam cac link con de tai rieng.

### Bao mat

GD6 **da co dang nhap/phan quyen**, duong dan `/admin/**` khi chua dang nhap se redirect 302 den `/login` (khong phai 404). Tuy nhien, chua kiem thu tich hop SQL Server, rate limiting va hardening cho production; chi chay local.

### Route moi GD6

- `/login`: dang nhap; `POST /logout` dang xuat.
- `/admin/nhan-vien`: CRUD nhan vien (chi QUAN_LY).
- `/admin/khach-hang`: CRUD khach hang, dia chi, lich su don.
- Neu tai khoan STAFF truy cap `/admin/nhan-vien`, nhan **403**, khong phai 404.
- Neu POS bao 403 cho `/admin/ban-tai-quay/tam-tinh`: kiem tra token CSRF trong request JSON.

## Trang 404 moi trong GD6

- Trang `templates/error/404.html` hien thi thong bao va link ve Dashboard.
- URL cu duoc redirect (bao gom `/admin/pos`, `/admin/don-hang`, `/dashboard`).
- `404` la URL khong ton tai; `403` la bi chan quyen; `500` thuong la loi chuong trinh/DB.
- Kiem tra route/asset tĩnh: `python scripts/verify_project.py`.
