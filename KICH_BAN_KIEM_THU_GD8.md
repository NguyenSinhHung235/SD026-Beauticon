# Kich ban QA giai doan 8 (manual with SQL Server)

1. [ ] Register new customer: new `khach_hang`, BCrypt hash, no plain text.
2. [ ] Register same email twice: clear error, no duplicate customer.
3. [ ] Customer login wrong password 5 times: session blocked temporarily.
4. [ ] Customer login correct: account page and navbar show customer, not staff.
5. [ ] Add real active variant to cart; unique `(cart,variant)` and `so_luong` increment.
6. [ ] Update cart quantity over stock / 21: rejected, DB quantity unchanged.
7. [ ] Change promo/voucher before checkout: server recalculates, no client price trust.
8. [ ] Place COD order with saved address: one ONLINE order, one pending payment, lines match, inventory deducted once.
9. [ ] Double-submit checkout form: no duplicate order, stock/voucher used only once.
10. [ ] Add another cart after a successful order, checkout again: NEW order token, NEW order.
11. [ ] Customer A uses URL for customer B order: 404 without leaking details.
12. [ ] Change customer's saved address after purchase: order shipping snapshot unchanged.
13. [ ] Admin advances CHUA_XU_LY -> DANG_XU_LY: stock not deducted a second time.
14. [ ] Admin confirms actual COD collection then completes order: customer sees paid + completed.
15. [ ] Admin cancels pending online order: stock restored, voucher use returned, status DA_HUY.
16. [ ] Admin cancels paid order: refund is CHO_XU_LY until staff confirms it happened.
17. [ ] Storefront add/cart/payment forms retain CSRF; POST without CSRF is 403.
18. [ ] /login staff, /dang-nhap customer and /admin/** access separation works.
19. [ ] `/`, `/bo-suu-tap`, `/san-pham/{id}`, `/gio-hang`, `/thanh-toan`, `/don-hang-cua-toi` have no broken 404 links.
20. [ ] Mobile phone: no horizontal scroll, radio address and submit button usable.

This checklist is for actual UAT; not evidence that the checks have been executed.
