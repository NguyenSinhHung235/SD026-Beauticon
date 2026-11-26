package com.example.vongtay.dashboard;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

@Controller
public class DashboardController {
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping({"/admin", "/admin/dashboard"})
    public String dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Model model) {
        LocalDate today = LocalDate.now(VN_ZONE);
        if (to == null) to = today;
        if (from == null) from = to.minusDays(6);
        if (from.isAfter(to) || from.isBefore(to.minusDays(90))) {
            model.addAttribute("error", "Vui long chon khoang ngay tu 1 den 91 ngay.");
            to = today;
            from = today.minusDays(6);
        }
        Map<String, Object> data = service.load(from, to);
        model.addAllAttributes(data);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "admin/dashboard";
    }
}
