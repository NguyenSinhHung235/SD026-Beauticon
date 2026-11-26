package com.example.vongtay.sales;

import com.example.vongtay.security.CurrentStaff;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Controller
@RequestMapping("/admin/voucher")
public class VoucherController {
    private final VoucherService service;
    private final CurrentStaff current;
    public VoucherController(VoucherService service,CurrentStaff current) { this.service = service; this.current = current; }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("items", service.list());
        return "admin/vouchers/index";
    }
    @GetMapping("/them")
    public String create(Model model) {
        VoucherForm f = new VoucherForm();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        f.setNgayBatDau(SaleSupport.toHtml(now));
        f.setNgayKetThuc(SaleSupport.toHtml(now.plusDays(30)));
        model.addAttribute("form", f);
        fill(model, null);
        return "admin/vouchers/form";
    }
    @GetMapping("/{id}/sua")
    public String edit(@PathVariable long id, Model model) {
        model.addAttribute("form", service.find(id));
        fill(model, id);
        return "admin/vouchers/form";
    }
    @PostMapping("/them")
    public String createPost(@ModelAttribute("form") VoucherForm f, BindingResult binding,
                             Model model, RedirectAttributes flash) {
        return save(null, f, binding, model, flash);
    }
    @PostMapping("/{id}/sua")
    public String editPost(@PathVariable long id, @ModelAttribute("form") VoucherForm f,
                           BindingResult binding, Model model, RedirectAttributes flash) {
        return save(id, f, binding, model, flash);
    }
    private String save(Long id, VoucherForm f, BindingResult binding,
                        Model model, RedirectAttributes flash) {
        if (!binding.hasErrors()) try {
            // Creator/owner of a voucher must not be supplied by the browser.
            f.setIdNhanVien(id==null ? current.id() : service.find(id).getIdNhanVien());
            service.save(id, f);
            flash.addFlashAttribute("success", "Da luu voucher.");
            return "redirect:/admin/voucher";
        } catch (IllegalArgumentException | DataIntegrityViolationException ex) {
            model.addAttribute("error", PromotionController.errorMessage(ex));
        }
        if (binding.hasErrors()) model.addAttribute("error", "Vui long kiem tra dinh dang so va cac truong nhap.");
        fill(model, id);
        return "admin/vouchers/form";
    }
    @PostMapping("/{id}/trang-thai")
    public String status(@PathVariable long id, @RequestParam boolean active, RedirectAttributes flash) {
        try {
            service.setActive(id, active);
            flash.addFlashAttribute("success", active ? "Da bat voucher." : "Da tam dung voucher.");
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/voucher";
    }
    private void fill(Model model, Long id) {
        model.addAttribute("voucherId", id);
    }
}
