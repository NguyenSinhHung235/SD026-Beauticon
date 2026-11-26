package com.example.vongtay.people;

import com.example.vongtay.security.CurrentStaff;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/nhan-vien")
public class StaffController {
    private final StaffService service;
    private final CurrentStaff current;
    public StaffController(StaffService service,CurrentStaff current) {
        this.service=service;this.current=current;
    }
    @GetMapping
    public String index(@RequestParam(defaultValue="") String q,
                        @RequestParam(defaultValue="1") Integer page,Model model) {
        try { model.addAttribute("listing",service.list(q,page)); }
        catch(IllegalArgumentException ex) {
            model.addAttribute("error",ex.getMessage());
            model.addAttribute("listing",service.list("",1));
        }
        return "admin/people/staff-list";
    }
    @GetMapping("/them")
    public String add(Model model) {
        model.addAttribute("form",new StaffForm()); model.addAttribute("employeeId",null);
        return "admin/people/staff-form";
    }
    @GetMapping("/{id}/sua")
    public String edit(@PathVariable long id,Model model) {
        model.addAttribute("form",service.form(id)); model.addAttribute("employeeId",id);
        return "admin/people/staff-form";
    }
    @PostMapping("/them")
    public String addPost(@ModelAttribute("form") StaffForm form,BindingResult binding,
                          Model model,RedirectAttributes flash) {
        return save(null,form,binding,model,flash);
    }
    @PostMapping("/{id}/sua")
    public String editPost(@PathVariable long id,@ModelAttribute("form") StaffForm form,
                           BindingResult binding,Model model,RedirectAttributes flash) {
        return save(id,form,binding,model,flash);
    }
    private String save(Long id,StaffForm form,BindingResult binding,Model model,RedirectAttributes flash) {
        try {
            if(binding.hasErrors()) throw new IllegalArgumentException("Du lieu nhap khong dung dinh dang.");
            service.save(id,form,current.id());
            flash.addFlashAttribute("success",id==null?"Da tao tai khoan nhan vien.":"Da cap nhat nhan vien.");
            return "redirect:/admin/nhan-vien";
        } catch(IllegalArgumentException | DataIntegrityViolationException ex) {
            model.addAttribute("error",ex instanceof DataIntegrityViolationException?
                    "Email da ton tai hoac vi pham rang buoc SQL Server.":ex.getMessage());
            model.addAttribute("employeeId",id);
            form.setMatKhau(""); // Never send password back to the view.
            return "admin/people/staff-form";
        }
    }
    @PostMapping("/{id}/trang-thai")
    public String changeStatus(@PathVariable long id,@RequestParam boolean active,RedirectAttributes flash) {
        try {
            service.setActive(id,active,current.id());
            flash.addFlashAttribute("success",active?"Da mo tai khoan.":"Da khoa tai khoan, giu nguyen lich su ban.");
        } catch(IllegalArgumentException ex) { flash.addFlashAttribute("error",ex.getMessage()); }
        return "redirect:/admin/nhan-vien";
    }
}
