package com.example.vongtay.catalog;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CatalogService {
    private final ProductRepository products;
    private final VariantRepository variants;
    private final AttributeService attributes;
    private final NamedParameterJdbcTemplate jdbc;

    public record ProductItem(Product product, String category, String brand, long variants, long stock, String thumbnail) {}
    public record VariantItem(ProductVariant variant, String color, String size) {}

    public CatalogService(ProductRepository products, VariantRepository variants,
                          AttributeService attributes, NamedParameterJdbcTemplate jdbc) {
        this.products = products;
        this.variants = variants;
        this.attributes = attributes;
        this.jdbc = jdbc;
    }

    public Product getProduct(long id) {
        return products.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));
    }

    public ProductVariant getVariant(long productId, long variantId) {
        ProductVariant result = variants.findById(variantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy biến thể"));
        if (result.getIdSanPham() != productId)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Biến thể không thuộc sản phẩm");
        return result;
    }

    public Page<Product> search(String q, Boolean active, int page) {
        String keyword = q == null || q.isBlank() ? null : q.trim();
        Specification<Product> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            if (keyword != null) predicates.add(cb.like(cb.lower(root.get("tenSanPham")),
                    "%" + keyword.toLowerCase(java.util.Locale.ROOT) + "%"));
            if (active != null) predicates.add(cb.equal(root.get("trangThai"), active));
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        return products.findAll(spec, PageRequest.of(Math.max(0, page), 10,
                Sort.by(Sort.Direction.DESC, "idSanPham")));
    }

    public List<ProductItem> presentProducts(List<Product> items) {
        Map<Long, String> categories = attributeNames(AttributeKind.CATEGORY);
        Map<Long, String> brands = attributeNames(AttributeKind.BRAND);
        Map<Long, long[]> stock = new HashMap<>();
        Map<Long, String> thumbnails = new HashMap<>();
        if (!items.isEmpty()) {
            org.springframework.jdbc.core.namedparam.MapSqlParameterSource params =
                    new org.springframework.jdbc.core.namedparam.MapSqlParameterSource()
                            .addValue("ids", items.stream().map(Product::getIdSanPham).toList());
            jdbc.query("""
                    SELECT id_san_pham, duong_dan_anh FROM dbo.hinh_anh_sp
                    WHERE anh_chinh=1 AND id_san_pham IN (:ids)
                    """, params, rs -> {
                thumbnails.put(rs.getLong("id_san_pham"), rs.getString("duong_dan_anh"));
            });
            for (Object[] row : variants.summarize(items.stream().map(Product::getIdSanPham).toList())) {
                stock.put(((Number) row[0]).longValue(), new long[] {
                        ((Number) row[1]).longValue(), ((Number) row[2]).longValue() });
            }
        }
        return items.stream().map(p -> {
            long[] n = stock.getOrDefault(p.getIdSanPham(), new long[] {0L, 0L});
            return new ProductItem(p, categories.getOrDefault(p.getIdLoai(), "—"),
                    brands.getOrDefault(p.getIdThuongHieu(), "—"), n[0], n[1],
                    thumbnails.get(p.getIdSanPham()));
        }).toList();
    }

    public List<VariantItem> presentVariants(long productId) {
        getProduct(productId);
        Map<Long, String> colors = attributeNames(AttributeKind.COLOR);
        Map<Long, String> sizes = attributeNames(AttributeKind.SIZE);
        return variants.findByIdSanPhamOrderByIdBienTheAsc(productId).stream()
                .map(v -> new VariantItem(v, colors.getOrDefault(v.getIdMauSac(), "—"),
                        sizes.getOrDefault(v.getIdKichThuoc(), "—"))).toList();
    }

    @Transactional
    public Product saveProduct(Long id, ProductForm f) {
        validateProduct(f);
        Product p = id == null ? new Product() : getProduct(id);
        p.setTenSanPham(f.getTenSanPham().trim());
        p.setIdLoai(f.getIdLoai());
        p.setIdThuongHieu(f.getIdThuongHieu());
        p.setIdNhaCungCap(f.getIdNhaCungCap());
        p.setIdChatLieu(f.getIdChatLieu());
        p.setTrongLuongGram(f.getTrongLuongGram());
        p.setMoTa(f.getMoTa() == null || f.getMoTa().isBlank() ? null : f.getMoTa().trim());
        p.setNgaySanXuat(f.getNgaySanXuat());
        p.setTrangThai(f.isTrangThai());
        return products.save(p);
    }

    @Transactional
    public void setProductActive(long id, boolean active) {
        Product p = getProduct(id);
        p.setTrangThai(active);
        products.save(p);
    }

    private ProductVariant getVariantForUpdate(long productId, long variantId) {
        ProductVariant v = variants.findForUpdate(variantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay bien the"));
        if (v.getIdSanPham() != productId)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bien the khong thuoc san pham");
        return v;
    }

    @Transactional
    public ProductVariant saveVariant(long productId, Long id, VariantForm f) {
        getProduct(productId);
        validateVariant(f);
        variants.findByIdSanPhamAndIdMauSacAndIdKichThuoc(productId, f.getIdMauSac(), f.getIdKichThuoc())
                .ifPresent(existing -> {
                    if (id == null || !existing.getIdBienThe().equals(id))
                        throw new IllegalArgumentException("Màu sắc và kích thước này đã tồn tại trong sản phẩm.");
                });
        ProductVariant v = id == null ? new ProductVariant() : getVariantForUpdate(productId, id);
        // A POS sale may have changed stock after the admin opened the edit form.
        if (id != null && (f.getExpectedStock() == null
                || !f.getExpectedStock().equals(v.getSoLuongTon())))
            throw new IllegalArgumentException("Ton kho da thay doi. Hay tai lai form truoc khi luu.");
        if (id != null && (!v.getIdMauSac().equals(f.getIdMauSac())
                || !v.getIdKichThuoc().equals(f.getIdKichThuoc()))) {
            Long used = jdbc.queryForObject("""
                    SELECT COUNT_BIG(*) FROM dbo.don_hang_chi_tiet WHERE id_bien_the=:id
                    """, java.util.Map.of("id", id), Long.class);
            if (used != null && used > 0)
                throw new IllegalArgumentException("Biến thể đã có đơn hàng: không được đổi màu/kích thước. Hãy giữ nguyên hoặc tạo biến thể khác.");
        }
        v.setIdSanPham(productId);
        v.setIdMauSac(f.getIdMauSac());
        v.setIdKichThuoc(f.getIdKichThuoc());
        v.setGiaBan(f.getGiaBan());
        v.setSoLuongTon(f.getSoLuongTon());
        v.setTrangThai(f.isTrangThai());
        return variants.saveAndFlush(v); // Phat hien vi pham unique ngay trong transaction.
    }

    @Transactional
    public void setVariantActive(long productId, long variantId, boolean active) {
        ProductVariant v = getVariantForUpdate(productId, variantId);
        v.setTrangThai(active);
        variants.save(v);
    }

    private Map<Long, String> attributeNames(AttributeKind kind) {
        Map<Long, String> map = new HashMap<>();
        for (AttributeService.Option opt : attributes.options(kind)) map.put(opt.id(), opt.label());
        return map;
    }

    private void validateProduct(ProductForm f) {
        if (f.getTenSanPham() == null || f.getTenSanPham().isBlank() || f.getTenSanPham().trim().length() > 200)
            throw new IllegalArgumentException("Tên sản phẩm bắt buộc và không quá 200 ký tự.");
        if (!attributes.exists(AttributeKind.CATEGORY, f.getIdLoai()))
            throw new IllegalArgumentException("Vui lòng chọn loại sản phẩm hợp lệ.");
        if (f.getIdThuongHieu() != null && !attributes.exists(AttributeKind.BRAND, f.getIdThuongHieu()))
            throw new IllegalArgumentException("Thương hiệu không tồn tại.");
        if (f.getIdNhaCungCap() != null && !attributes.exists(AttributeKind.SUPPLIER, f.getIdNhaCungCap()))
            throw new IllegalArgumentException("Nhà cung cấp không tồn tại.");
        if (f.getIdChatLieu() != null && !attributes.exists(AttributeKind.MATERIAL, f.getIdChatLieu()))
            throw new IllegalArgumentException("Chất liệu không tồn tại.");
        if (f.getTrongLuongGram() != null &&
                (f.getTrongLuongGram().compareTo(BigDecimal.ZERO) <= 0 || f.getTrongLuongGram().scale() > 3
                        || f.getTrongLuongGram().precision() - f.getTrongLuongGram().scale() > 7))
            throw new IllegalArgumentException("Trọng lượng phải dương và phù hợp DECIMAL(10,3).");
    }

    private void validateVariant(VariantForm f) {
        if (!attributes.exists(AttributeKind.COLOR, f.getIdMauSac()))
            throw new IllegalArgumentException("Vui lòng chọn màu sắc hợp lệ.");
        if (!attributes.exists(AttributeKind.SIZE, f.getIdKichThuoc()))
            throw new IllegalArgumentException("Vui lòng chọn kích thước hợp lệ.");
        if (f.getGiaBan() == null || f.getGiaBan().compareTo(BigDecimal.ZERO) < 0
                || f.getGiaBan().scale() > 2 || f.getGiaBan().precision() - f.getGiaBan().scale() > 16)
            throw new IllegalArgumentException("Giá bán phải >= 0 và phù hợp DECIMAL(18,2).");
        if (f.getSoLuongTon() == null || f.getSoLuongTon() < 0)
            throw new IllegalArgumentException("Số lượng tồn phải >= 0.");
    }
}
