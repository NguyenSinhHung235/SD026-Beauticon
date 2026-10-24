package com.example.vongtay.catalog;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindingResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/thuoc-tinh/{slug}")
public class AttributeController {
    private final AttributeService service;

    public AttributeController(AttributeService service) { this.service = service; }

    @GetMapping
    public String list(@PathVariable String slug, @RequestParam(defaultValue = "") String q, Model model) {
        AttributeKind kind = AttributeKind.fromSlug(slug);
        model.addAttribute("kind", kind);
        model.addAttribute("items", service.list(kind, q));
        model.addAttribute("q", q);
        return "admin/attributes/index";
    }

    @GetMapping("/them")
    public String create(@PathVariable String slug, Model model) {
        fill(model, AttributeKind.fromSlug(slug), null);
        model.addAttribute("form", new AttributeForm());
        return "admin/attributes/form";
    }

    @PostMapping("/them")
    public String create(@PathVariable String slug, @ModelAttribute("form") AttributeForm form,
                         BindingResult binding, Model model, RedirectAttributes flash) {
        AttributeKind kind = AttributeKind.fromSlug(slug);
        if (binding.hasErrors()) return bindError(kind, null, model);
        return save(kind, null, form, model, flash);
    }

    @GetMapping("/{id}/sua")
    public String edit(@PathVariable String slug, @PathVariable long id, Model model) {
        AttributeKind kind = AttributeKind.fromSlug(slug);
        fill(model, kind, id);
        model.addAttribute("form", service.form(service.get(kind, id)));
        return "admin/attributes/form";
    }

    @PostMapping("/{id}/sua")
    public String edit(@PathVariable String slug, @PathVariable long id,
                       @ModelAttribute("form") AttributeForm form, BindingResult binding,
                       Model model, RedirectAttributes flash) {
        AttributeKind kind = AttributeKind.fromSlug(slug);
        if (binding.hasErrors()) return bindError(kind, id, model);
        return save(kind, id, form, model, flash);
    }

    @PostMapping("/{id}/trang-thai")
    public String status(@PathVariable String slug, @PathVariable long id,
                         @RequestParam boolean active, RedirectAttributes flash) {
        AttributeKind kind = AttributeKind.fromSlug(slug);
        service.setActive(kind, id, active);
        flash.addFlashAttribute("success", active ? "Đã kích hoạt thuộc tính." : "Đã ẩn thuộc tính (không xóa dữ liệu cũ).");
        return "redirect:/admin/thuoc-tinh/" + kind.slug;
    }

    private String bindError(AttributeKind kind, Long id, Model model) {
        fill(model, kind, id);
        model.addAttribute("error", "Dữ liệu nhập không hợp lệ.");
        return "admin/attributes/form";
    }

    private String save(AttributeKind kind, Long id, AttributeForm form, Model model, RedirectAttributes flash) {
        try {
            service.save(kind, id, form);
            flash.addFlashAttribute("success", id == null ? "Đã thêm thuộc tính." : "Đã cập nhật thuộc tính.");
            return "redirect:/admin/thuoc-tinh/" + kind.slug;
        } catch (IllegalArgumentException | DataIntegrityViolationException ex) {
            fill(model, kind, id);
            model.addAttribute("error", ProductController.errorMessage(ex));
            return "admin/attributes/form";
        }
    }

    private void fill(Model model, AttributeKind kind, Long id) {
        model.addAttribute("kind", kind);
        model.addAttribute("attrId", id);
    }
}
