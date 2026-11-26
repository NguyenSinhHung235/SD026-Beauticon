package com.example.vongtay.shop;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ShopOrdersController {
    private final ShopSession sessions;
    private final ShopOrderService orders;
    public ShopOrdersController(ShopSession sessions,ShopOrderService orders) {
        this.sessions=sessions;this.orders=orders;
    }

    @GetMapping("/don-hang-cua-toi")
    public String mine(HttpServletRequest request,Model model) {
        ShopAuthService.Customer customer=sessions.current(request);
        if(customer==null)return "redirect:/dang-nhap";
        model.addAttribute("orders",orders.mine(customer.id()));
        return "shop/orders";
    }

    @GetMapping("/don-hang-cua-toi/{id}")
    public String detail(@PathVariable long id,HttpServletRequest request,Model model) {
        ShopAuthService.Customer customer=sessions.current(request);
        if(customer==null)return "redirect:/dang-nhap";
        model.addAttribute("view",orders.detail(customer.id(),id));
        return "shop/order-detail";
    }
}
