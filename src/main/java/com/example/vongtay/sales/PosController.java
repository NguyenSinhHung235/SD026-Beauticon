package com.example.vongtay.sales;

import com.example.vongtay.security.CurrentStaff;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/admin/ban-tai-quay")
public class PosController {
    private final PosService pos;
    private final CurrentStaff current;
    public PosController(PosService pos,CurrentStaff current) { this.pos = pos; this.current = current; }

    @GetMapping
    public String index(Model model) {
        PosForm f = new PosForm();
        f.setRequestToken(UUID.randomUUID().toString());
        f.setStaffId(current.id());
        model.addAttribute("form", f);
        fill(model);
        return "admin/pos/index";
    }

    @PostMapping(value="/tam-tinh", consumes="application/json")
    @ResponseBody
    public java.util.Map<String, Object> quote(@RequestBody PosForm form) {
        try { return pos.quote(form); }
        catch (IllegalArgumentException ex) { return java.util.Map.of("error", ex.getMessage()); }
    }

    @PostMapping("/tao")
    public String checkout(@ModelAttribute("form") PosForm form, BindingResult binding,
                           Model model, RedirectAttributes flash) {
        if (!binding.hasErrors()) try {
            form.setStaffId(current.id()); // NEVER trust staffId submitted by the browser.
            long id = pos.checkout(form);
            flash.addFlashAttribute("success", "Hoa don da thanh toan. Ton kho da duoc cap nhat.");
            return "redirect:/admin/ban-tai-quay/" + id + "/hoa-don";
        } catch (IllegalArgumentException | DataIntegrityViolationException ex) {
            model.addAttribute("error", PromotionController.errorMessage(ex));
        }
        if (binding.hasErrors()) model.addAttribute("error", "Du lieu san pham, so luong hoac tien mat khong dung dinh dang.");
        fill(model);
        return "admin/pos/index";
    }

    @GetMapping("/{id}/hoa-don")
    public String receipt(@PathVariable long id, Model model) {
        model.addAttribute("receipt", pos.receipt(id));
        return "admin/pos/receipt";
    }

    private void fill(Model model) {
        model.addAttribute("variants", pos.variants());
        model.addAttribute("customers", pos.customers());
    }
}
