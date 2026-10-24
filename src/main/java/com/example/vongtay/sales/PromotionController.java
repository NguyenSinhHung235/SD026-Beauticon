package com.example.vongtay.sales;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Controller
@RequestMapping("/admin/khuyen-mai")
public class PromotionController {
    private final PromotionService service;
    public PromotionController(PromotionService service) { this.service = service; }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("items", service.list());
        return "admin/promotions/index";
    }
    @GetMapping("/them")
    public String create(Model model) {
        PromotionForm f = new PromotionForm();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        f.setNgayBatDau(SaleSupport.toHtml(now));
        f.setNgayKetThuc(SaleSupport.toHtml(now.plusDays(7)));
        model.addAttribute("form", f);
        fill(model, null);
        return "admin/promotions/form";
    }
    @GetMapping("/{id}/sua")
    public String edit(@PathVariable long id, Model model) {
        model.addAttribute("form", service.find(id));
        fill(model, id);
        return "admin/promotions/form";
    }
    @PostMapping("/them")
    public String createPost(@ModelAttribute("form") PromotionForm f, BindingResult binding,
                             Model model, RedirectAttributes flash) {
        return save(null, f, binding, model, flash);
    }
    @PostMapping("/{id}/sua")
    public String editPost(@PathVariable long id, @ModelAttribute("form") PromotionForm f,
                           BindingResult binding, Model model, RedirectAttributes flash) {
        return save(id, f, binding, model, flash);
    }
    private String save(Long id, PromotionForm f, BindingResult binding,
                        Model model, RedirectAttributes flash) {
        if (!binding.hasErrors()) try {
            service.save(id, f);
            flash.addFlashAttribute("success", "Da luu chuong trinh khuyen mai.");
            return "redirect:/admin/khuyen-mai";
        } catch (IllegalArgumentException | DataIntegrityViolationException ex) {
            model.addAttribute("error", errorMessage(ex));
        }
        if (binding.hasErrors()) model.addAttribute("error", "Vui long kiem tra dinh dang so va cac truong nhap.");
        fill(model, id);
        return "admin/promotions/form";
    }
    @PostMapping("/{id}/trang-thai")
    public String status(@PathVariable long id, @RequestParam boolean active, RedirectAttributes flash) {
        try {
            service.setActive(id, active);
            flash.addFlashAttribute("success", active ? "Da bat khuyen mai." : "Da tam dung khuyen mai.");
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/khuyen-mai";
    }
    private void fill(Model model, Long id) {
        model.addAttribute("promotionId", id);
        model.addAttribute("variants", service.variantOptions());
    }
    static String errorMessage(Exception ex) {
        return ex instanceof DataIntegrityViolationException
                ? "Du lieu trung hoac vi pham rang buoc SQL Server." : ex.getMessage();
    }
}
