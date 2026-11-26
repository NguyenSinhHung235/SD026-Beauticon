package com.example.vongtay.showroom;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ShowroomController {
    private final ShowroomService service;
    public ShowroomController(ShowroomService service) { this.service=service; }

    @GetMapping({"/", "/bo-suu-tap"})
    public String collection(@RequestParam(value="q",required=false) String q,
                             @RequestParam(value="loai",required=false) Long category,
                             @RequestParam(value="page",required=false) Integer page,
                             Model model) {
        model.addAttribute("collection",service.collection(q,category,page));
        return "shop/collection";
    }

    @GetMapping("/san-pham/{id}")
    public String detail(@PathVariable long id, Model model) {
        model.addAttribute("product",service.detail(id));
        return "shop/detail";
    }
}
