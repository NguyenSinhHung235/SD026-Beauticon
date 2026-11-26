package com.example.vongtay.catalog;

import java.util.Arrays;
import java.util.List;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/** SQL identifiers ONLY come from these constants; no table/column names from the HTTP request. */
public enum AttributeKind {
    CATEGORY("loai", "Loại sản phẩm", "loai_san_pham", "id_loai", "ten_loai", List.of()),
    BRAND("thuong-hieu", "Thương hiệu", "thuong_hieu", "id_thuong_hieu", "ten_thuong_hieu", List.of("quoc_gia")),
    SUPPLIER("nha-cung-cap", "Nhà cung cấp", "nha_cung_cap", "id_nha_cung_cap", "ten_nha_cung_cap", List.of("sdt", "email", "dia_chi")),
    MATERIAL("chat-lieu", "Chất liệu", "chat_lieu", "id_chat_lieu", "ten_chat_lieu", List.of()),
    COLOR("mau-sac", "Màu sắc", "bien_the_mau_sac", "id_mau_sac", "ten_mau_sac", List.of("ma_mau")),
    SIZE("kich-thuoc", "Kích thước", "bien_the_kich_thuoc", "id_kich_thuoc", "ten_kich_thuoc", List.of());

    public final String slug, title, table, idColumn, nameColumn;
    public final List<String> extras;

    AttributeKind(String slug, String title, String table, String idColumn, String nameColumn, List<String> extras) {
        this.slug = slug;
        this.title = title;
        this.table = table;
        this.idColumn = idColumn;
        this.nameColumn = nameColumn;
        this.extras = extras;
    }

    public static AttributeKind fromSlug(String slug) {
        return Arrays.stream(values()).filter(k -> k.slug.equals(slug))
                .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public String getSlug() { return slug; }
    public String getTitle() { return title; }
    public boolean has(String column) { return extras.contains(column); }
}
