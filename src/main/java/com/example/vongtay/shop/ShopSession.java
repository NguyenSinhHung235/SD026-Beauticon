package com.example.vongtay.shop;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

/** Customer authentication is separate from /login (staff Spring Security). */
@Component
public class ShopSession {
    private static final String KEY = "SHOP_CUSTOMER_ID";
    private static final String HASH = "SHOP_CUSTOMER_HASH";
    private final ShopAuthService accounts;

    public ShopSession(ShopAuthService accounts) { this.accounts = accounts; }

    public ShopAuthService.Customer current(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Object id = session.getAttribute(KEY);
        if (!(id instanceof Long customerId)) return null;
        ShopAuthService.Customer valid = accounts.verifiedCustomer(customerId,
                (String) session.getAttribute(HASH));
        if (valid == null) {
            session.removeAttribute(KEY);
            session.removeAttribute(HASH);
        }
        return valid;
    }

    public void login(HttpServletRequest request, ShopAuthService.Credentials credentials) {
        HttpSession session = request.getSession(true);
        request.changeSessionId(); // prevent session fixation
        session.setAttribute(KEY, credentials.customer().id());
        session.setAttribute(HASH, credentials.hash());
        session.removeAttribute("SHOP_BAD_LOGIN");
        session.removeAttribute("SHOP_LOCK_UNTIL");
    }

    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
    }

    public boolean blocked(HttpSession session) {
        Object value = session.getAttribute("SHOP_LOCK_UNTIL");
        return value instanceof Long t && System.currentTimeMillis() < t;
    }

    public void failedLogin(HttpSession session) {
        int bad = session.getAttribute("SHOP_BAD_LOGIN") instanceof Integer n ? n : 0;
        session.setAttribute("SHOP_BAD_LOGIN", ++bad);
        if (bad >= 5) session.setAttribute("SHOP_LOCK_UNTIL", System.currentTimeMillis() + 300_000L);
    }
}
