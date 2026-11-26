package com.example.vongtay.catalog;

import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductImageService {
    public record ProductImage(long id, String url, boolean main) {}
    private final NamedParameterJdbcTemplate jdbc;
    private final ProductRepository products;

    public ProductImageService(NamedParameterJdbcTemplate jdbc, ProductRepository products) {
        this.jdbc = jdbc;
        this.products = products;
    }

    public List<ProductImage> list(long productId) {
        checkProduct(productId);
        return jdbc.query("""
                SELECT id_anh, duong_dan_anh, anh_chinh FROM dbo.hinh_anh_sp
                WHERE id_san_pham = :p ORDER BY anh_chinh DESC, id_anh
                """, Map.of("p", productId), (rs, n) -> new ProductImage(
                rs.getLong("id_anh"), rs.getString("duong_dan_anh"), rs.getBoolean("anh_chinh")));
    }

    @Transactional
    public void add(long productId, String url) {
        lockProductForImageChange(productId);
        String safeUrl = validateUrl(url);
        Long count = jdbc.queryForObject("SELECT COUNT_BIG(*) FROM dbo.hinh_anh_sp WHERE id_san_pham=:p AND anh_chinh=1",
                Map.of("p", productId), Long.class);
        jdbc.update("""
                INSERT INTO dbo.hinh_anh_sp(id_san_pham, duong_dan_anh, anh_chinh)
                VALUES(:p, :url, :main)
                """, new MapSqlParameterSource().addValue("p", productId).addValue("url", safeUrl)
                .addValue("main", count != null && count == 0));
    }

    @Transactional
    public void updateUrl(long productId, long imageId, String url) {
        owned(productId, imageId);
        jdbc.update("UPDATE dbo.hinh_anh_sp SET duong_dan_anh=:url WHERE id_anh=:id",
                Map.of("url", validateUrl(url), "id", imageId));
    }

    @Transactional
    public void setMain(long productId, long imageId) {
        lockProductForImageChange(productId);
        owned(productId, imageId);
        // Disable old main first: filtered UNIQUE index allows only one main image per product.
        jdbc.update("UPDATE dbo.hinh_anh_sp SET anh_chinh=0 WHERE id_san_pham=:p", Map.of("p", productId));
        jdbc.update("UPDATE dbo.hinh_anh_sp SET anh_chinh=1 WHERE id_anh=:id", Map.of("id", imageId));
    }

    @Transactional
    public void remove(long productId, long imageId) {
        lockProductForImageChange(productId);
        owned(productId, imageId);
        jdbc.update("DELETE FROM dbo.hinh_anh_sp WHERE id_anh=:id", Map.of("id", imageId));
        Long main = jdbc.queryForObject("""
                SELECT COUNT_BIG(*) FROM dbo.hinh_anh_sp
                WHERE id_san_pham=:p AND anh_chinh=1
                """, Map.of("p", productId), Long.class);
        if (main != null && main == 0) {
            jdbc.update("""
                UPDATE dbo.hinh_anh_sp SET anh_chinh=1 WHERE id_anh =
                 (SELECT MIN(id_anh) FROM dbo.hinh_anh_sp WHERE id_san_pham=:p)
                """, Map.of("p", productId));
        }
    }

    /** Serialize image writes for one product; the filtered main-image index then stays consistent. */
    private void lockProductForImageChange(long productId) {
        List<Long> rows = jdbc.queryForList("""
                SELECT id_san_pham FROM dbo.san_pham WITH (UPDLOCK, HOLDLOCK)
                WHERE id_san_pham=:id
                """, Map.of("id", productId), Long.class);
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    private void checkProduct(long id) {
        if (!products.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    private void owned(long productId, long imageId) {
        Long n = jdbc.queryForObject("""
                SELECT COUNT_BIG(*) FROM dbo.hinh_anh_sp WHERE id_anh=:id AND id_san_pham=:p
                """, Map.of("id", imageId, "p", productId), Long.class);
        if (n == null || n == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    private static String validateUrl(String url) {
        if (url == null || url.isBlank() || url.length() > 1000)
            throw new IllegalArgumentException("Đường dẫn ảnh không được trống hoặc quá 1000 ký tự.");
        String text = url.trim();
        if (text.startsWith("/uploads/") && !text.contains("..") && !text.contains("\\")) return text;
        try {
            URI uri = URI.create(text);
            if ("https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null
                    && uri.getUserInfo() == null) return text;
        } catch (IllegalArgumentException ignored) { }
        throw new IllegalArgumentException("Ảnh phải dùng URL HTTPS hoặc đường dẫn /uploads/...");
    }
}
