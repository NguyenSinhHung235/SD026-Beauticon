package com.example.vongtay.shop;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ShopAddressController {
    private final ShopSession sessions;
    private final ShopAddressService addresses;
    public ShopAddressController(ShopSession sessions,ShopAddressService addresses) {
        this.sessions=sessions;this.addresses=addresses;
    }

    @GetMapping("/tai-khoan/dia-chi")
    public String list(HttpServletRequest request,Model model) {
        ShopAuthService.Customer customer=sessions.current(request);
        if(customer==null)return "redirect:/dang-nhap";
        model.addAttribute("addresses",addresses.list(customer.id()));
        return "shop/addresses";
    }

    @PostMapping("/tai-khoan/dia-chi/them")
    public String add(@RequestParam(defaultValue="") String tenNguoiNhan,
                      @RequestParam(defaultValue="") String soDienThoai,
                      @RequestParam(defaultValue="") String diaChi,
                      @RequestParam(defaultValue="false") boolean macDinh,
                      HttpServletRequest request,RedirectAttributes flash) {
        ShopAuthService.Customer c=sessions.current(request);
        if(c==null)return "redirect:/dang-nhap";
        try { addresses.add(c.id(),tenNguoiNhan,soDienThoai,diaChi,macDinh);
              flash.addFlashAttribute("success","Đã thêm địa chỉ giao hàng."); }
        catch(IllegalArgumentException ex) {flash.addFlashAttribute("error",ex.getMessage());}
        return "redirect:/tai-khoan/dia-chi";
    }

    @PostMapping("/tai-khoan/dia-chi/xoa")
    public String remove(@RequestParam long idDiaChi,HttpServletRequest request,RedirectAttributes flash) {
        ShopAuthService.Customer c=sessions.current(request);
        if(c==null)return "redirect:/dang-nhap";
        try {addresses.remove(c.id(),idDiaChi);flash.addFlashAttribute("success","Đã ẩn địa chỉ khỏi tài khoản.");}
        catch(IllegalArgumentException ex) {flash.addFlashAttribute("error",ex.getMessage());}
        return "redirect:/tai-khoan/dia-chi";
    }
}
