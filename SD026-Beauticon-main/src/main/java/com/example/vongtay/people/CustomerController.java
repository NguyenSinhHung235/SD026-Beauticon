package com.example.vongtay.people;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/khach-hang")
public class CustomerController {
    private final CustomerService customers;
    private final CustomerAddressService addresses;
    public CustomerController(CustomerService customers,CustomerAddressService addresses) {
        this.customers=customers;this.addresses=addresses;
    }
    @GetMapping
    public String list(@RequestParam(defaultValue="") String q,
                       @RequestParam(defaultValue="1") Integer page,Model model) {
        try { model.addAttribute("listing",customers.list(q,page)); }
        catch(IllegalArgumentException ex) {
            model.addAttribute("error",ex.getMessage());model.addAttribute("listing",customers.list("",1));
        }
        return "admin/people/customers-list";
    }
    @GetMapping("/them")
    public String create(Model model) {
        model.addAttribute("form",new CustomerForm());model.addAttribute("customerId",null);
        return "admin/people/customer-form";
    }
    @GetMapping("/{id}/sua")
    public String edit(@PathVariable long id,Model model) {
        model.addAttribute("form",customers.form(id));model.addAttribute("customerId",id);
        return "admin/people/customer-form";
    }
    @PostMapping("/them")
    public String createPost(@ModelAttribute("form") CustomerForm form, BindingResult binding,
                             Model model,RedirectAttributes flash) {
        return save(null,form,binding,model,flash);
    }
    @PostMapping("/{id}/sua")
    public String editPost(@PathVariable long id,@ModelAttribute("form") CustomerForm form,
                           BindingResult binding,Model model,RedirectAttributes flash) {
        return save(id,form,binding,model,flash);
    }
    private String save(Long id,CustomerForm form,BindingResult binding,Model model,RedirectAttributes flash) {
        try {
            if(binding.hasErrors()) throw new IllegalArgumentException("Ngay sinh hoac thong tin nhap khong hop le.");
            long saved=customers.save(id,form);
            flash.addFlashAttribute("success",id==null?"Da them khach hang.":"Da cap nhat khach hang.");
            return "redirect:/admin/khach-hang/"+saved;
        } catch(IllegalArgumentException | DataIntegrityViolationException ex) {
            model.addAttribute("error",ex instanceof DataIntegrityViolationException?
                    "Email da co nguoi su dung hoac du lieu vi pham rang buoc.":ex.getMessage());
            model.addAttribute("customerId",id);
            return "admin/people/customer-form";
        }
    }
    @GetMapping("/{id}")
    public String detail(@PathVariable long id,Model model) {
        model.addAttribute("customer",customers.get(id));
        model.addAttribute("addresses",addresses.list(id));
        model.addAttribute("orders",customers.recentOrders(id));
        return "admin/people/customer-detail";
    }
    @PostMapping("/{id}/trang-thai")
    public String status(@PathVariable long id,@RequestParam boolean active,RedirectAttributes flash) {
        try {
            customers.setActive(id,active);
            flash.addFlashAttribute("success",active?"Da mo khach hang.":"Da an khach hang, bao toan du lieu don hang.");
        } catch(IllegalArgumentException ex) { flash.addFlashAttribute("error",ex.getMessage()); }
        return "redirect:/admin/khach-hang/"+id;
    }
    @GetMapping("/{customerId}/dia-chi/them")
    public String newAddress(@PathVariable long customerId,Model model) {
        customers.get(customerId);
        model.addAttribute("form",new AddressForm());
        fillAddress(model,customerId,null);
        return "admin/people/address-form";
    }
    @GetMapping("/{customerId}/dia-chi/{addressId}/sua")
    public String editAddress(@PathVariable long customerId,@PathVariable long addressId,Model model) {
        model.addAttribute("form",addresses.form(customerId,addressId));
        fillAddress(model,customerId,addressId);
        return "admin/people/address-form";
    }
    @PostMapping("/{customerId}/dia-chi/them")
    public String addAddress(@PathVariable long customerId,@ModelAttribute("form") AddressForm form,
                             BindingResult binding,Model model,RedirectAttributes flash) {
        return saveAddress(customerId,null,form,binding,model,flash);
    }
    @PostMapping("/{customerId}/dia-chi/{addressId}/sua")
    public String updateAddress(@PathVariable long customerId,@PathVariable long addressId,
                                @ModelAttribute("form") AddressForm form,BindingResult binding,
                                Model model,RedirectAttributes flash) {
        return saveAddress(customerId,addressId,form,binding,model,flash);
    }
    private String saveAddress(long customerId,Long addressId,AddressForm form,BindingResult binding,
                               Model model,RedirectAttributes flash) {
        try {
            if(binding.hasErrors()) throw new IllegalArgumentException("Du lieu dia chi khong dung.");
            addresses.save(customerId,addressId,form);
            flash.addFlashAttribute("success","Da luu dia chi giao hang.");
            return "redirect:/admin/khach-hang/"+customerId;
        } catch(IllegalArgumentException | DataIntegrityViolationException ex) {
            model.addAttribute("error",ex instanceof DataIntegrityViolationException?
                    "Khong the luu dia chi. Kiem tra lai dia chi mac dinh.":ex.getMessage());
            fillAddress(model,customerId,addressId);
            return "admin/people/address-form";
        }
    }
    @PostMapping("/{customerId}/dia-chi/{addressId}/trang-thai")
    public String addressStatus(@PathVariable long customerId,@PathVariable long addressId,
                                @RequestParam boolean active,RedirectAttributes flash) {
        try {
            addresses.setActive(customerId,addressId,active);
            flash.addFlashAttribute("success",active?"Da khoi phuc dia chi.":"Da an dia chi, khong xoa lich su don.");
        } catch(IllegalArgumentException ex) {flash.addFlashAttribute("error",ex.getMessage());}
        return "redirect:/admin/khach-hang/"+customerId;
    }
    private void fillAddress(Model model,long customerId,Long addressId) {
        model.addAttribute("customer",customers.get(customerId));
        model.addAttribute("addressId",addressId);
    }
}
