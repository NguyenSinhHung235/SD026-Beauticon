package com.example.vongtay.orders;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/** Friendly redirects for common URLs used in older README versions or typed by hand. */
@Controller
public class RouteRedirectController {
    @GetMapping("/health")
    @ResponseBody
    public String health() { return "BeautIcon Admin GD8 is running"; }

    @GetMapping({"/dashboard", "/admin/"})
    public String home() { return "redirect:/admin/dashboard"; }

    @GetMapping({"/admin/don-hang", "/admin/don-hang/online", "/admin/don-hang-online/"})
    public String orders() { return "redirect:/admin/don-hang-online"; }

    @GetMapping({"/admin/hoa-don/", "/admin/hoa-don/da-hoan-thanh"})
    public String invoices() { return "redirect:/admin/hoa-don"; }

    @GetMapping("/admin/hoa-don/online")
    public String onlineInvoices() { return "redirect:/admin/hoa-don?kenh=ONLINE"; }

    @GetMapping("/admin/hoa-don/offline")
    public String offlineInvoices() { return "redirect:/admin/hoa-don?kenh=OFFLINE"; }

    @GetMapping("/admin/pos")
    public String pos() { return "redirect:/admin/ban-tai-quay"; }
}
