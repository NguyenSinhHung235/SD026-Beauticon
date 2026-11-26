package com.example.vongtay.catalog;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/san-pham")
public class ProductController {
    private final CatalogService service;
    private final AttributeService attributes;

    public ProductController(CatalogService service, AttributeService attributes) {
        this.service = service;
        this.attributes = attributes;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(required = false) Boolean active,
                       @RequestParam(defaultValue = "0") int page, Model model) {
        Page<Product> result = service.search(q, active, Math.max(0, page));
        model.addAttribute("items", service.presentProducts(result.getContent()));
        model.addAttribute("page", result);
        model.addAttribute("q", q);
        model.addAttribute("active", active);
        return "admin/products/index";
    }

    @GetMapping("/them")
    public String create(Model model) {
        model.addAttribute("form", new ProductForm());
        fill(model, null);
        return "admin/products/form";
    }

    @PostMapping("/them")
    public String create(@ModelAttribute("form") ProductForm form, BindingResult binding,
                         Model model, RedirectAttributes flash) {
        if (binding.hasErrors()) {
            model.addAttribute("error", "Vui lòng nhập đúng kiểu dữ liệu, số và ngày tháng.");
            fill(model, null);
            return "admin/products/form";
        }
        try {
            Product saved = service.saveProduct(null, form);
            flash.addFlashAttribute("success", "Đã tạo sản phẩm. Hãy thêm màu sắc/kích thước và giá bán.");
            return "redirect:/admin/san-pham/" + saved.getIdSanPham() + "/bien-the";
        } catch (IllegalArgumentException | DataIntegrityViolationException ex) {
            model.addAttribute("error", errorMessage(ex));
            fill(model, null);
            return "admin/products/form";
        }
    }

    @GetMapping("/{id}/sua")
    public String edit(@PathVariable long id, Model model) {
        model.addAttribute("form", ProductForm.from(service.getProduct(id)));
        fill(model, id);
        return "admin/products/form";
    }

    @PostMapping("/{id}/sua")
    public String edit(@PathVariable long id, @ModelAttribute("form") ProductForm form,
                       BindingResult binding, Model model, RedirectAttributes flash) {
        if (binding.hasErrors()) {
            model.addAttribute("error", "Vui lòng nhập đúng kiểu dữ liệu, số và ngày tháng.");
            fill(model, id);
            return "admin/products/form";
        }
        try {
            service.saveProduct(id, form);
            flash.addFlashAttribute("success", "Đã cập nhật sản phẩm.");
            return "redirect:/admin/san-pham";
        } catch (IllegalArgumentException | DataIntegrityViolationException ex) {
            model.addAttribute("error", errorMessage(ex));
            fill(model, id);
            return "admin/products/form";
        }
    }

    @PostMapping("/{id}/trang-thai")
    public String status(@PathVariable long id, @RequestParam boolean active, RedirectAttributes flash) {
        service.setProductActive(id, active);
        flash.addFlashAttribute("success", active ? "Sản phẩm đã được hiện lại." : "Đã ẩn sản phẩm (không xóa lịch sử bán).");
        return "redirect:/admin/san-pham";
    }

    private void fill(Model model, Long id) {
        model.addAttribute("productId", id);
        model.addAttribute("categories", attributes.options(AttributeKind.CATEGORY));
        model.addAttribute("brands", attributes.options(AttributeKind.BRAND));
        model.addAttribute("suppliers", attributes.options(AttributeKind.SUPPLIER));
        model.addAttribute("materials", attributes.options(AttributeKind.MATERIAL));
    }

    static String errorMessage(Exception ex) {
        return ex instanceof DataIntegrityViolationException
                ? "Dữ liệu bị trùng hoặc vi phạm ràng buộc khóa ngoại trong SQL Server."
                : ex.getMessage();
    }
}
