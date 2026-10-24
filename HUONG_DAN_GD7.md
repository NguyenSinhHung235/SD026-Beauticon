# BeautIcon GD7 / Runbook and design checklist

## Start from the previous database

- Back up the working GD6 database. No new migration is needed; 22 tables remain.
- Edit `src/main/resources/application.properties` or set `DB_URL`, `DB_USER`, `DB_PASSWORD`.
- Run `mvn clean test` and then `mvn spring-boot:run`. For first-time admin credentials, follow the GD5 bootstrap instructions in README.

## Main URLs

| Goal | URL | Access |
| --- | --- | --- |
| Public collection | http://localhost:8080/ | Public |
| Filtered collection | http://localhost:8080/bo-suu-tap | Public |
| Product detail | http://localhost:8080/san-pham/1 | Public only if product 1 is active and has an active variant |
| Staff login | http://localhost:8080/login | Public |
| Dashboard | http://localhost:8080/admin/dashboard | Staff |
| Products | http://localhost:8080/admin/san-pham | Staff; edits are admin-only |
| POS | http://localhost:8080/admin/ban-tai-quay | Staff |
| Health text | http://localhost:8080/health | Public |

## Common 404/403 situations

1. If `/` is 404, ensure you are launching this **GD7** source tree, not GD6.
2. If product detail returns 404, verify its product, category, and at least one variant are active; this is intentional.
3. If `/uploads/file.jpg` returns 404, the file is missing from `UPLOADS_DIR` (default `./uploads` relative to the startup directory); an image URL field does **not** copy the actual photo automatically.
4. If `/admin/dashboard` redirects to `/login`, that is expected before sign-in; a staff role denied an admin edit should receive 403.
5. If all routes fail, verify `/health`, the server port, startup logs and SQL Server connectivity.

## Design edits in one place

- `static/css/storefront.css` - public brand style; colors in the `:root` declaration.
- `static/css/admin-brand.css` - management style and login.
- `static/css/admin-dashboard.css` - visual reporting layout.
- `templates/shop/collection.html` - homepage editorial copy.
- `templates/shop/detail.html` - product details.
- `templates/admin/fragments/nav.html` - admin navigation.

## Scope still outstanding

Customer registration, persisted customer cart, online checkout, payment gateway callbacks, orders from public visitors, product image uploader, richer SEO and production security hardening are **not part of GD7**. The Admin features from GD6 remain included. To make a genuine end-to-end e-commerce website, implement these explicitly as a subsequent phase.
