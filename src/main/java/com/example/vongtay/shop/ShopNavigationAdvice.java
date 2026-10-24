package com.example.vongtay.shop;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Shared navigation for both the pre-existing showroom and the new customer pages. */
@ControllerAdvice(basePackages={"com.example.vongtay.shop","com.example.vongtay.showroom"})
public class ShopNavigationAdvice {
    private final ShopSession sessions;
    private final ShopCartService carts;
    public ShopNavigationAdvice(ShopSession sessions, ShopCartService carts) {
        this.sessions=sessions; this.carts=carts;
    }
    @ModelAttribute("shopCustomer")
    public ShopAuthService.Customer customer(HttpServletRequest request) {
        return sessions.current(request);
    }
    @ModelAttribute("shopCartCount")
    public int cartCount(HttpServletRequest request) {
        ShopAuthService.Customer c = sessions.current(request);
        return c == null ? 0 : carts.count(c.id());
    }
}
