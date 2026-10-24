# Khac phuc anh san pham khong hien (GD8)

Nguyen nhan ban cu: form chi luu URL, khong upload file, trong khi `/uploads/**` chi doc file co san trong `UPLOADS_DIR`.

Ban sua: Admin > San pham > Hinh anh > Tai anh tu may (JPG/PNG, <= 5MB). File duoc luu vao `app.uploads.dir` va URL `/uploads/<uuid>.jpg` hoac `.png` duoc them vao DB.

1. Chay ung dung, dang nhap Admin, mo `/admin/san-pham/{id}/hinh-anh`.
2. Chon file va bam **Tai anh len**.
3. Thu mo `http://localhost:8080/uploads/<ten-file>.jpg` trong trinh duyet.
4. Neu 404, kiem tra thu muc cau hinh trong `application.properties`: `app.uploads.dir=${UPLOADS_DIR:uploads}`. Gia tri `uploads` la duong dan TUONG DOI THU MUC CHAY UNG DUNG, khong nhat thiet la thu muc ma nguon. De on dinh, thiet lap `UPLOADS_DIR` den duong dan TUYET DOI tren Windows (vi du `D:/BeautIcon/uploads`). Khoi dong lai.
5. Neu link anh `https://...` khong hien, may chu ben ngoai co the chan hotlink, anh bi xoa, hoac URL khong phai link anh truc tiep. Hay tai anh co quyen su dung ve may va dung form upload.

Luu y: Anh luu tren o dia may chu, khong duoc dong goi tu dong vao SQL backup. Khi ban giao, sao luu ca thu muc uploads. Neu trien khai len host moi, can copy thu muc va cau hinh UPLOADS_DIR.
