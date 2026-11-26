package com.example.vongtay.catalog;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/san-pham/{productId}/hinh-anh")
public class ProductImageController {
    private final ProductImageService images;
    private final CatalogService catalog;
    private final ProductImageUploadService uploader;

    public ProductImageController(ProductImageService images, CatalogService catalog, ProductImageUploadService uploader) {
        this.images = images;
        this.catalog = catalog;
        this.uploader = uploader;
    }

    @GetMapping
    public String list(@PathVariable long productId, Model model) {
        model.addAttribute("product", catalog.getProduct(productId));
        model.addAttribute("items", images.list(productId));
        return "admin/products/images";
    }

    @PostMapping("/tai-len")
    public String upload(@PathVariable long productId, @RequestParam("file") MultipartFile file,
                         RedirectAttributes flash) {
        try {
            uploader.upload(productId, file);
            flash.addFlashAttribute("success", "Ảnh đã được tải lên và liên kết với sản phẩm.");
        } catch (IllegalArgumentException | IOException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return back(productId);
    }

    @PostMapping("/them")
    public String add(@PathVariable long productId, @RequestParam String url, RedirectAttributes flash) {
        try {
            images.add(productId, url);
            flash.addFlashAttribute("success", "Đã thêm ảnh.");
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return back(productId);
    }

    @PostMapping("/{id}/sua")
    public String edit(@PathVariable long productId, @PathVariable long id,
                       @RequestParam String url, RedirectAttributes flash) {
        try {
            images.updateUrl(productId, id, url);
            flash.addFlashAttribute("success", "Đã sửa đường dẫn ảnh.");
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return back(productId);
    }

    @PostMapping("/{id}/anh-chinh")
    public String main(@PathVariable long productId, @PathVariable long id, RedirectAttributes flash) {
        images.setMain(productId, id);
        flash.addFlashAttribute("success", "Đã chọn ảnh đại diện.");
        return back(productId);
    }

    @PostMapping("/{id}/xoa")
    public String delete(@PathVariable long productId, @PathVariable long id, RedirectAttributes flash) {
        images.remove(productId, id);
        flash.addFlashAttribute("success", "Đã xóa ảnh khỏi sản phẩm.");
        return back(productId);
    }

    private static String back(long id) { return "redirect:/admin/san-pham/" + id + "/hinh-anh"; }
}
