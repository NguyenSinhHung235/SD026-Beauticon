# GD7 / Acceptance checklist (manual checks on your Windows + SQL Server machine)

**Required:** JDK 17+, Maven, Microsoft SQL Server and the migrated GD6 database.

| ID | User action | Expected outcome |
| --- | --- | --- |
| P01 | Visit `/` without logging in | Shows the public collection; does not redirect to staff login. |
| P02 | Search for a real product name; then search nonsense | Only matching active products display; empty state offers a reset. |
| P03 | Filter by an active product category | Results and result count match the database. |
| P04 | Change a product to hidden in Admin | The product vanishes from public listing, direct `/san-pham/{id}` returns 404. |
| P05 | Set all variants of a product inactive | The product disappears from the public listing. |
| P06 | Keep a variant active but stock zero | Product still displays as sold out; no purchase button is shown. |
| P07 | Open a product with multiple image URLs | Thumbnail selection changes main image; no JavaScript console errors. |
| P08 | Put a real `.jpg` in `uploads/` and add its path through Admin | `/uploads/file.jpg` loads only if file exists; no path guesswork. |
| P09 | As guest visit `/admin/dashboard`, then log in as Staff | Guest gets redirected to login; Staff sees dashboard. |
| P10 | As Staff attempt product mutation via form and raw POST | Admin-only request must be denied with 403. |
| P11 | Submit POS transaction while another user edits stock | Inventory consistency maintained (perform against SQL Server test data). |
| P12 | Check 390px, 768px and 1440px widths | No document-wide horizontal overflow; sidebar opens at mobile width. |
| P13 | Submit a keyword containing `%`, `_`, `'` and `[` | Query treats it as plain text; no SQL error or injection. |
| P14 | Open `/san-pham/9999999` | Styled 404 page appears, not a stack trace. |
| P15 | Visit `/robots.txt` and `/favicon.ico` | Static files served successfully. |
| P16 | Check an Admin URL that previously failed | It maps to a page, redirects to login, or denies with 403 as intended. |

**Run basic automated smoke checks**:

```bash
python3 scripts/verify_project.py
sh scripts/run_java_smoke.sh
sh scripts/run_showroom_smoke.sh
node --check src/main/resources/static/js/storefront.js
node --check src/main/resources/static/js/admin-shell.js
mvn clean test
```

These script checks are not proof of Spring Boot runtime or SQL Server integration. Check Maven output and SSMS logs separately before claiming all acceptance cases passed.

## Authentic design QA before graduation defense

Ask 3-5 people who have never seen the Admin UI to complete one product search, one stock adjustment and one simulated checkout. Note where they hesitate and fix at least 3 concrete issues. This provides genuine design evidence more persuasive than a stock template or a claimed AI-detector score.
