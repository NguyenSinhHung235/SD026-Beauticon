package com.example.vongtay.shop;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ShopAuthController {
    private final ShopAuthService accounts;
    private final ShopSession session;

    public ShopAuthController(ShopAuthService accounts, ShopSession session) {
        this.accounts = accounts; this.session = session;
    }

    @GetMapping("/dang-ky")
    public String registerPage(HttpServletRequest request) {
        return session.current(request) == null ? "shop/register" : "redirect:/tai-khoan";
    }

    @PostMapping("/dang-ky")
    public String register(@RequestParam(defaultValue="") String hoTen,
                           @RequestParam(defaultValue="") String email,
                           @RequestParam(defaultValue="") String sdt,
                           @RequestParam(defaultValue="") String matKhau,
                           @RequestParam(defaultValue="") String xacNhanMatKhau,
                           HttpServletRequest request, Model model) {
        try {
            if (session.current(request) != null) return "redirect:/tai-khoan";
            ShopAuthService.Credentials newAccount = accounts.register(hoTen,email,sdt,matKhau,xacNhanMatKhau);
            session.login(request,newAccount);
            return "redirect:/tai-khoan";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error",ex.getMessage());
            model.addAttribute("hoTen",hoTen);
            model.addAttribute("email",email);
            model.addAttribute("sdt",sdt);
            return "shop/register";
        }
    }

    @GetMapping("/dang-nhap")
    public String loginPage(HttpServletRequest request) {
        return session.current(request) == null ? "shop/login" : "redirect:/tai-khoan";
    }

    @PostMapping("/dang-nhap")
    public String login(@RequestParam(defaultValue="") String email,
                        @RequestParam(defaultValue="") String matKhau,
                        HttpServletRequest request, Model model) {
        if (session.current(request) != null) return "redirect:/tai-khoan";
        HttpSession store = request.getSession(true);
        try {
            if (session.blocked(store)) throw new IllegalArgumentException("Bạn đã thử quá nhiều lần. Vui lòng thử lại sau 5 phút.");
            ShopAuthService.Credentials customer = accounts.login(email,matKhau);
            session.login(request,customer);
            return "redirect:/tai-khoan";
        } catch (IllegalArgumentException ex) {
            if (!session.blocked(store)) session.failedLogin(store);
            model.addAttribute("error",ex.getMessage());
            model.addAttribute("email",email);
            return "shop/login";
        }
    }

    @PostMapping("/tai-khoan/dang-xuat")
    public String logout(HttpServletRequest request) {
        session.logout(request);
        return "redirect:/?da-dang-xuat";
    }

    @GetMapping("/tai-khoan")
    public String account(HttpServletRequest request, Model model) {
        ShopAuthService.Customer customer = session.current(request);
        if (customer == null) return "redirect:/dang-nhap";
        model.addAttribute("customer",customer);
        return "shop/account";
    }
}
