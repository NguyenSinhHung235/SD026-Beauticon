package com.example.vongtay.shop;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ShopCheckoutController {
    private static final String TOKEN_KEY="SHOP_CHECKOUT_TOKEN";
    private final ShopSession sessions;
    private final ShopCartService carts;
    private final ShopAddressService addresses;
    private final ShopCheckoutService checkout;

    public ShopCheckoutController(ShopSession sessions,ShopCartService carts,
                                  ShopAddressService addresses,ShopCheckoutService checkout) {
        this.sessions=sessions;this.carts=carts;this.addresses=addresses;this.checkout=checkout;
    }

    @GetMapping("/thanh-toan")
    public String checkout(@RequestParam(value="voucher",defaultValue="") String voucher,
                           HttpServletRequest request,Model model,RedirectAttributes flash) {
        ShopAuthService.Customer customer=sessions.current(request);
        if(customer==null)return "redirect:/dang-nhap";
        ShopCartService.Basket basket=carts.basket(customer.id());
        if(!basket.canCheckout()) {
            flash.addFlashAttribute("error","Hãy kiểm tra sản phẩm và tồn kho trong giỏ trước khi đặt hàng.");
            return "redirect:/gio-hang";
        }
        ShopCheckoutService.Quote quote;
        try { quote=checkout.preview(customer.id(),voucher); }
        catch(IllegalArgumentException ex) {
            model.addAttribute("error",ex.getMessage());
            quote=checkout.preview(customer.id(),"");
        }
        HttpSession session=request.getSession(true);
        if(!(session.getAttribute(TOKEN_KEY) instanceof String))
            session.setAttribute(TOKEN_KEY,UUID.randomUUID().toString());
        model.addAttribute("checkoutToken",session.getAttribute(TOKEN_KEY));
        model.addAttribute("basket",basket);
        model.addAttribute("quote",quote);
        model.addAttribute("addresses",addresses.list(customer.id()));
        return "shop/checkout";
    }

    @PostMapping("/thanh-toan/dat-hang")
    public String place(@ModelAttribute ShopCheckoutForm form,
                        HttpServletRequest request,RedirectAttributes flash) {
        ShopAuthService.Customer customer=sessions.current(request);
        if(customer==null)return "redirect:/dang-nhap";
        HttpSession session=request.getSession(false);
        if (session == null) return "redirect:/gio-hang";
        if (!(session.getAttribute(TOKEN_KEY) instanceof String saved)
                || !saved.equals(form.getMaYeuCau())) {
            // Graceful replay of an already successful browser request.
            if (form.getMaYeuCau() != null
                    && form.getMaYeuCau().equals(session.getAttribute("SHOP_LAST_ORDER_TOKEN"))
                    && session.getAttribute("SHOP_LAST_ORDER_ID") instanceof Long oldId)
                return "redirect:/don-hang-cua-toi/" + oldId;
            flash.addFlashAttribute("error","Phiên đặt hàng không hợp lệ. Hãy tải lại trang thanh toán.");
            return "redirect:/gio-hang";
        }
        try {
            ShopCheckoutService.Result done=checkout.place(customer.id(),form);
            session.setAttribute("SHOP_LAST_ORDER_TOKEN",saved);
            session.setAttribute("SHOP_LAST_ORDER_ID",done.orderId());
            session.removeAttribute(TOKEN_KEY); // next visit to /thanh-toan gets a new order token
            return "redirect:/don-hang-cua-toi/"+done.orderId()+"?vua-dat=1";
        } catch(IllegalArgumentException ex) {
            flash.addFlashAttribute("error",ex.getMessage());
            flash.addAttribute("voucher",form.getMaVoucher()==null?"":form.getMaVoucher());
            return "redirect:/thanh-toan";
        }
    }
}
