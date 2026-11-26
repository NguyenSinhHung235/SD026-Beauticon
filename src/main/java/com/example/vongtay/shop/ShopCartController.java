package com.example.vongtay.shop;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ShopCartController {
    private final ShopSession sessions;
    private final ShopCartService carts;
    public ShopCartController(ShopSession sessions,ShopCartService carts) {
        this.sessions=sessions;this.carts=carts;
    }

    @GetMapping("/gio-hang")
    public String cart(HttpServletRequest request,Model model) {
        ShopAuthService.Customer c=sessions.current(request);
        if(c==null)return "redirect:/dang-nhap";
        model.addAttribute("basket",carts.basket(c.id()));
        return "shop/cart";
    }

    @PostMapping("/gio-hang/them")
    public String add(@RequestParam long idBienThe,@RequestParam(defaultValue="1") int soLuong,
                      @RequestParam(defaultValue="") String kieuKichThuoc,
                      @RequestParam(defaultValue="") String chuViCoTayCm,
                      @RequestParam(defaultValue="") String doRongDeo,
                      @RequestParam(defaultValue="") String ghiChuKichThuoc,
                      HttpServletRequest request,RedirectAttributes flash) {
        ShopAuthService.Customer c=sessions.current(request);
        if(c==null) {
            flash.addFlashAttribute("error","Vui lòng đăng nhập để lưu sản phẩm vào giỏ.");
            return "redirect:/dang-nhap";
        }
        try {
            carts.add(c.id(),idBienThe,soLuong,kieuKichThuoc,chuViCoTayCm,doRongDeo,ghiChuKichThuoc);
            flash.addFlashAttribute("success","Đã thêm vòng tay vào giỏ hàng.");
        } catch (IllegalArgumentException ex) { flash.addFlashAttribute("error",ex.getMessage()); }
        return "redirect:/gio-hang";
    }

    @PostMapping("/gio-hang/cap-nhat")
    public String update(@RequestParam long idChiTiet,@RequestParam int soLuong,
                         HttpServletRequest request,RedirectAttributes flash) {
        ShopAuthService.Customer c=sessions.current(request);
        if(c==null)return "redirect:/dang-nhap";
        try { carts.change(c.id(),idChiTiet,soLuong);flash.addFlashAttribute("success","Đã cập nhật số lượng."); }
        catch (IllegalArgumentException ex) {flash.addFlashAttribute("error",ex.getMessage());}
        return "redirect:/gio-hang";
    }

    @PostMapping("/gio-hang/xoa")
    public String remove(@RequestParam long idChiTiet,HttpServletRequest request,RedirectAttributes flash) {
        ShopAuthService.Customer c=sessions.current(request);
        if(c==null)return "redirect:/dang-nhap";
        try {carts.remove(c.id(),idChiTiet);flash.addFlashAttribute("success","Đã bỏ sản phẩm khỏi giỏ.");}
        catch(IllegalArgumentException ex) {flash.addFlashAttribute("error",ex.getMessage());}
        return "redirect:/gio-hang";
    }
}
