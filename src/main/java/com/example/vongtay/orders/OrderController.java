package com.example.vongtay.orders;

import com.example.vongtay.security.CurrentStaff;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class OrderController {
    private final OrderService orders;
    private final CurrentStaff current;
    public OrderController(OrderService orders, CurrentStaff current) { this.orders = orders; this.current = current; }

    @GetMapping("/admin/don-hang-online")
    public String online(@RequestParam(defaultValue="ALL") String status,
                         @RequestParam(defaultValue="") String q,
                         @RequestParam(defaultValue="1") Integer page,
                         Model model) {
        try {
            model.addAttribute("listing", orders.list("ONLINE",status,q,page,false));
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error",ex.getMessage());
            model.addAttribute("listing",orders.list("ONLINE","ALL","",1,false));
        }
        model.addAttribute("active","online");
        return "admin/orders/list";
    }

    @GetMapping("/admin/hoa-don")
    public String invoices(@RequestParam(defaultValue="ALL") String kenh,
                           @RequestParam(defaultValue="") String q,
                           @RequestParam(defaultValue="1") Integer page,
                           Model model) {
        try {
            model.addAttribute("listing",orders.list(kenh,"ALL",q,page,true));
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error",ex.getMessage());
            model.addAttribute("listing",orders.list("ALL","ALL","",1,true));
        }
        model.addAttribute("active","invoices");
        return "admin/orders/list";
    }

    @GetMapping("/admin/don-hang-online/{id}")
    public String onlineDetail(@PathVariable long id,Model model,RedirectAttributes flash) {
        try {
            OrderService.Detail detail = orders.detail(id);
            if(!"ONLINE".equals(detail.order().get("channel")))
                throw new IllegalArgumentException("Don nay la hoa don tai quay, khong phai don online.");
            model.addAttribute("view",detail);
            model.addAttribute("active","online");
            model.addAttribute("printMode",false);
            return "admin/orders/detail";
        } catch(IllegalArgumentException ex) {
            flash.addFlashAttribute("error",ex.getMessage());
            return "redirect:/admin/don-hang-online";
        }
    }

    @GetMapping({"/admin/hoa-don/{id}","/admin/hoa-don/{id}/in"})
    public String invoiceDetail(@PathVariable long id,Model model,RedirectAttributes flash) {
        try {
            OrderService.Detail detail = orders.detail(id);
            if(!"HOAN_THANH".equals(detail.order().get("status")))
                throw new IllegalArgumentException("Hoa don chi hien thi khi don da hoan thanh.");
            model.addAttribute("view",detail);
            model.addAttribute("active","invoices");
            model.addAttribute("printMode",true);
            return "admin/orders/detail";
        } catch(IllegalArgumentException ex) {
            flash.addFlashAttribute("error",ex.getMessage());
            return "redirect:/admin/hoa-don";
        }
    }

    @PostMapping("/admin/don-hang-online/{id}/chuyen-trang-thai")
    public String advance(@PathVariable long id,RedirectAttributes flash) {
        return run(id,flash,()->orders.advance(id,current.id()));
    }

    @PostMapping("/admin/don-hang-online/{id}/huy")
    public String cancel(@PathVariable long id,
                         @RequestParam String lyDoHuy,RedirectAttributes flash) {
        return run(id,flash,()->orders.cancel(id,current.id(),lyDoHuy));
    }

    @PostMapping("/admin/don-hang-online/{id}/xac-nhan-thanh-toan")
    public String payment(@PathVariable long id,
                          @RequestParam String phuongThuc,
                          @RequestParam(defaultValue="false") boolean daNhanTien,
                          @RequestParam(defaultValue="") String maDoiSoat,RedirectAttributes flash) {
        return run(id,flash,()->orders.confirmReceipt(id,current.id(),phuongThuc,daNhanTien,maDoiSoat));
    }

    @PostMapping("/admin/don-hang-online/{id}/xac-nhan-hoan-tien")
    public String refund(@PathVariable long id,
                         @RequestParam(defaultValue="false") boolean daHoanTien,
                         @RequestParam(defaultValue="") String maDoiSoat,RedirectAttributes flash) {
        return run(id,flash,()->orders.confirmRefund(id,current.id(),daHoanTien,maDoiSoat));
    }

    private String run(long id,RedirectAttributes flash,Runnable change) {
        try {
            change.run();
            flash.addFlashAttribute("success","Da cap nhat giao dich / don hang thanh cong.");
        } catch(IllegalArgumentException ex) {
            flash.addFlashAttribute("error",ex.getMessage());
        } catch(org.springframework.dao.DataAccessException ex) {
            flash.addFlashAttribute("error","Loi co so du lieu. Kiem tra lai giao dich hoac tai lai trang.");
        }
        return "redirect:/admin/don-hang-online/"+id;
    }
}
