package com.example.vongtay.catalog;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindingResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/san-pham/{productId}/bien-the")
public class VariantController {
    private final CatalogService service;
    private final AttributeService attributes;

    public VariantController(CatalogService service, AttributeService attributes) {
        this.service = service;
        this.attributes = attributes;
    }

    @GetMapping
    public String list(@PathVariable long productId, Model model) {
        model.addAttribute("product", service.getProduct(productId));
        model.addAttribute("items", service.presentVariants(productId));
        return "admin/variants/index";
    }

    @GetMapping("/them")
    public String create(@PathVariable long productId, Model model) {
        model.addAttribute("form", new VariantForm());
        fill(model, productId, null);
        return "admin/variants/form";
    }

    @PostMapping("/them")
    public String create(@PathVariable long productId, @ModelAttribute("form") VariantForm form,
                         BindingResult binding, Model model, RedirectAttributes flash) {
        if (binding.hasErrors()) return bindError(productId, null, model);
        return save(productId, null, form, model, flash);
    }

    @GetMapping("/{id}/sua")
    public String edit(@PathVariable long productId, @PathVariable long id, Model model) {
        model.addAttribute("form", VariantForm.from(service.getVariant(productId, id)));
        fill(model, productId, id);
        return "admin/variants/form";
    }

    @PostMapping("/{id}/sua")
    public String edit(@PathVariable long productId, @PathVariable long id,
                       @ModelAttribute("form") VariantForm form, BindingResult binding,
                       Model model, RedirectAttributes flash) {
        if (binding.hasErrors()) return bindError(productId, id, model);
        return save(productId, id, form, model, flash);
    }

    @PostMapping("/{id}/trang-thai")
    public String status(@PathVariable long productId, @PathVariable long id,
                         @RequestParam boolean active, RedirectAttributes flash) {
        service.setVariantActive(productId, id, active);
        flash.addFlashAttribute("success", active ? "Đã bật bán biến thể." : "Đã ngừng bán biến thể; lịch sử đơn hàng được giữ nguyên.");
        return "redirect:/admin/san-pham/" + productId + "/bien-the";
    }

    private String bindError(long productId, Long id, Model model) {
        model.addAttribute("error", "Giá bán, số lượng tồn hoặc thuộc tính không đúng định dạng.");
        fill(model, productId, id);
        return "admin/variants/form";
    }

    private String save(long productId, Long id, VariantForm form, Model model, RedirectAttributes flash) {
        try {
            service.saveVariant(productId, id, form);
            flash.addFlashAttribute("success", id == null ? "Đã tạo biến thể." : "Đã cập nhật biến thể.");
            return "redirect:/admin/san-pham/" + productId + "/bien-the";
        } catch (IllegalArgumentException | DataIntegrityViolationException ex) {
            model.addAttribute("error", ProductController.errorMessage(ex));
            fill(model, productId, id);
            return "admin/variants/form";
        }
    }

    private void fill(Model model, long productId, Long id) {
        model.addAttribute("product", service.getProduct(productId));
        model.addAttribute("productId", productId);
        model.addAttribute("variantId", id);
        model.addAttribute("colors", attributes.options(AttributeKind.COLOR));
        model.addAttribute("sizes", attributes.options(AttributeKind.SIZE));
    }
}
