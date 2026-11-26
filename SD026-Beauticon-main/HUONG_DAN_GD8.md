# BeautIcon | Giai doan 8 - Web khach hang hoan chinh o muc demo

## 1. Ket qua

- Dang ky / dang nhap tai khoan khach rieng voi nhan vien; hash BCrypt.
- Gio hang gan theo khach: them bien the, sua so luong, xoa, xem ton kho va gia khuyen mai.
- Quan ly dia chi nhan hang; chi doc/chinh sua dia chi cua chinh chu tai khoan.
- Checkout online: voucher, dia chi, ghi chu, COD hoac chuyen khoan cho doi soat.
- Dat don atomically: tinh lai gia tai server, tru ton kho, dung voucher, tao don/chi tiet/thanh toan CHO_XU_LY, xoa gio.
- Ma UUID cho moi lan checkout, UNIQUE de giam dat trung; trang thai va thanh toan hien thi cho khach.
- Don ONLINE hien ngay trong /admin/don-hang-online. Admin tiep tuc quy trinh Giai doan 4.

## 2. SQL Server

Neu ban DA CO database tu Giai doan 7:

1. BACKUP database `VongTayNu`.
2. Trong SQL Server Management Studio, chay duy nhat `sql/08_migrate_giai_doan_8.sql`.
3. Chay `sql/09_kiem_tra_giai_doan_8.sql` de kiem tra.
4. KHONG chay lai `01_schema.sql` va KHONG chay seed lan nua.

Neu ban CHUA CO database:

1. Chay `sql/01_schema.sql` cua ban GD8 (da co cot online token), tuy chon `sql/02_seed_demo.sql` mot lan.
2. KHONG can chay migration 08 tren database moi.

Database van **22 bang**, chi them mot cot `don_hang.ma_yeu_cau_online VARCHAR(36) NULL`, UNIQUE filtered index va CHECK kenh online.

## 3. IntelliJ va ung dung

Yeucau: JDK 17+ (JDK 21 phu hop), Maven, SQL Server dang chay.

```
# Windows PowerShell
$env:DB_USER="sa"
$env:DB_PASSWORD="YOUR_SQL_SERVER_PASSWORD"
mvn clean test
mvn spring-boot:run
```

Neu chua co nhan vien quan ly, xem phan tao admin lan dau trong `README.md`.

Truy cap:

| Chuc nang | URL |
|---|---|
| Kiem tra server | http://localhost:8080/health |
| Trang chu | http://localhost:8080/ |
| Dang ky | http://localhost:8080/dang-ky |
| Dang nhap khach | http://localhost:8080/dang-nhap |
| Tai khoan | http://localhost:8080/tai-khoan |
| Dia chi giao | http://localhost:8080/tai-khoan/dia-chi |
| Gio hang | http://localhost:8080/gio-hang |
| Thanh toan | http://localhost:8080/thanh-toan |
| Don cua toi | http://localhost:8080/don-hang-cua-toi |
| Admin login | http://localhost:8080/login |
| Don online admin | http://localhost:8080/admin/don-hang-online |

### Kich ban demo nhanh

1. Vao `/dang-ky`, tao KH moi (mat khau toi thieu 12 ky tu). Khach nhan vien la 2 khu dang nhap rieng.
2. Vao bo suu tap; chon san pham co `so_luong_ton > 0`; chon mau/size, `Them vao gio`.
3. Vao gio, tang giam so luong; den trang thanh toan.
4. Them dia chi moi, chon COD, kiem tra so tien va dat hang.
5. Vao `Don hang cua toi`: thay CHUA_XU_LY / COD chua thu tien.
6. Dang nhap Admin tai `/login`; mo `/admin/don-hang-online`, tim ma don vua tao.
7. Xu ly trang thai va khi THUC SU nhan tien thi ghi nhan o Admin. Chi sau khi doi soat moi hoan tat.
8. Quay lai khu khach: trang don hang cap nhat status/da thanh toan.

## 4. Quy tac nghiep vu quan trong

- Gia ban, khuyen mai, voucher, ton kho, phi ship duoc MAY CHU tinh lai khi dat hang; khong tin so tien tu trinh duyet.
- Phi van chuyen demo: 30,000 VND, mien phi neu tien hang truoc voucher tu 500,000 VND. Hien chi la quy tac gia dinh de bao ve do an, can thay theo chinh sach cua hang.
- Don online tru ton kho ngay khi dat va gan `ton_kho_da_tru=1`; Admin GD4 khong tru lai khi xu ly. Huy don Admin hoan ton kho, hoan luot voucher va tao yeu cau hoan tien cho neu da thu tien.
- `thanh_toan` ban dau la CHO_XU_LY, KHONG phai thanh cong. COD va chuyen khoan deu do nhan vien xac nhan thu cong, khong co webhook hay tai khoan ngan hang gia.
- Dia chi cho don duoc luu thanh ban snapshot trong `don_hang`; khi khach xoa dia chi cua tai khoan thi lich su van dung.
- Khach A khong truy cap duoc `/don-hang-cua-toi/{id}` cua khach B (404).
- Session khach va nhan vien su dung luong dang nhap khac nhau; `/admin/**` van bao ve bang Spring Security. Logout tai khoan khach dong session hien tai.
- Ma checkout UUID tao server + UNIQUE trong SQL, cho phep retry an toan, khong dat trung cung mot yeu cau.

## 5. QA va gioi han

Chay cac buoc:

```
python scripts/verify_project.py
sh scripts/run_java_smoke.sh
sh scripts/run_showroom_smoke.sh
sh scripts/run_shop_smoke.sh
mvn clean test
```

4 lenh dau la kiem tra doc lap voi Spring API STUBS / phan tich tinh. Lenh `mvn clean test` chi co the xac nhan tren may da cai Maven va tai duoc dependencies.

**Chua duoc kiem thu ket noi SQL Server thuc te trong moi truong tao ma.** Can chay checklist `KICH_BAN_KIEM_THU_GD8.md` tren may cua ban truoc buoi bao ve.

**Chua nen deploy public**: chua co HTTPS production, email verification, quen mat khau, rate limit phan tan, cong thanh toan that, webhook, email notifications, tracking van chuyen, kiem thu transaction concurrent bang SQL Server that, audit bao mat day du. Da co CSRF Spring Security va password hash BCrypt, nhung khong thay the kiem thu thuc te.

## 6. Loai tru 404

- `GET /health` phai tra ve chuoi co BeautIcon.
- Neu `/health` 404: dang chay sai app hoac sai port; dung instance cu tren 8080, chay lai project GD8.
- Neu `/dang-nhap` 404 ma `/health` dung: kiem tra controller `shop/ShopAuthController` duoc scan boi Spring Boot tu package `com.example.vongtay`.
- Neu trang thanh toan bi chuyen ve gio: dang chua dang nhap, gio trong, san pham khong du ton, hoac phien checkout da ket thuc.
- Neu SQL bao `Invalid column name 'ma_yeu_cau_online'`, chay migration 08 tren dung database.
- Neu chua dang nhap Admin, route `/admin/**` chuyen ve `/login` (302); neu khong du quyen tra 403, khong phai 404.
