package com.example.vongtay.showroom;

import java.math.BigDecimal;
import com.example.vongtay.shop.SizeSelection;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Public read-only catalog. It does not expose draft/hidden products or staff data. */
@Service
public class ShowroomService {
    private final NamedParameterJdbcTemplate jdbc;
    public static final int PAGE_SIZE = 12;

    public ShowroomService(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    public record Category(long id, String name, long products) {}
    public record Card(long id, String name, String category, String material,
                       String image, String price, long stock) {}
    public record Variant(long id, String color, String size, String mode, String price, long stock) {}
    public record Detail(long id, String name, String category, String material,
                         String brand, String description, String image, String price,
                         long stock, BigDecimal freesizeMinCm, BigDecimal freesizeMaxCm,
                         List<String> gallery, List<Variant> variants) {}
    public record Collection(List<Card> products, List<Category> categories, long total,
                             int page, int pages, String keyword, Long category) {}

    /** Remove surrounding whitespace and cap user-provided text before querying SQL Server. */
    public static String cleanKeyword(String value) {
        if (value == null) return "";
        String s = value.trim().replaceAll("[\\p{Cntrl}]", "");
        return s.length() <= 80 ? s : s.substring(0, 80);
    }
    public static int cleanPage(Integer value) {
        return value == null ? 1 : Math.max(1, Math.min(value, 1000));
    }

    public Collection collection(String input, Long requestedCategory, Integer requestedPage) {
        String q = cleanKeyword(input);
        int page = cleanPage(requestedPage);
        // A missing/inactive category cannot expose hidden data; treat it as no matches.
        Long category = requestedCategory != null && requestedCategory > 0 ? requestedCategory : null;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("q", q).addValue("category", category)
                .addValue("skip", (page - 1) * PAGE_SIZE).addValue("take", PAGE_SIZE);
        String joins = """
            FROM dbo.san_pham sp
            JOIN dbo.loai_san_pham l ON l.id_loai=sp.id_loai AND l.trang_thai=1
            LEFT JOIN dbo.chat_lieu cl ON cl.id_chat_lieu=sp.id_chat_lieu
            """;
        String where = """
            WHERE sp.trang_thai=1
              AND EXISTS (SELECT 1 FROM dbo.san_pham_bien_the b
                          WHERE b.id_san_pham=sp.id_san_pham AND b.trang_thai=1)
            """;
        // Only append constant SQL fragments; all user input is a bound parameter.
        if (category != null) where += " AND sp.id_loai=:category ";
        if (!q.isEmpty()) where += " AND CHARINDEX(LOWER(:q),LOWER(sp.ten_san_pham)) > 0 ";
        Long total = jdbc.queryForObject("SELECT COUNT_BIG(*) " + joins + where, params, Long.class);
        long count = total == null ? 0 : total;
        int pages = Math.max(1, (int) ((count + PAGE_SIZE - 1) / PAGE_SIZE));
        if (page > pages) page = pages;
        params.addValue("skip", (page - 1) * PAGE_SIZE);

        List<Card> products = jdbc.query("""
            SELECT sp.id_san_pham, sp.ten_san_pham, l.ten_loai,
                   COALESCE(cl.ten_chat_lieu, N'') AS material,
                   img.duong_dan_anh, v.price, v.stock
            """ + joins + """
            OUTER APPLY (SELECT TOP(1) a.duong_dan_anh FROM dbo.hinh_anh_sp a
                         WHERE a.id_san_pham=sp.id_san_pham
                         ORDER BY a.anh_chinh DESC, a.id_anh ASC) img
            OUTER APPLY (SELECT MIN(b.gia_ban) AS price, SUM(CAST(b.so_luong_ton AS BIGINT)) AS stock
                         FROM dbo.san_pham_bien_the b
                         WHERE b.id_san_pham=sp.id_san_pham AND b.trang_thai=1) v
            """ + where + """
            ORDER BY sp.ngay_tao DESC, sp.id_san_pham DESC
            OFFSET :skip ROWS FETCH NEXT :take ROWS ONLY
            """, params, (rs, i) -> new Card(
                rs.getLong("id_san_pham"), rs.getString("ten_san_pham"),
                rs.getString("ten_loai"), rs.getString("material"),
                rs.getString("duong_dan_anh"), money(rs.getBigDecimal("price")),
                rs.getLong("stock")));
        List<Category> categories = jdbc.query("""
            SELECT l.id_loai, l.ten_loai, COUNT_BIG(DISTINCT sp.id_san_pham) AS products
            FROM dbo.loai_san_pham l
            JOIN dbo.san_pham sp ON sp.id_loai=l.id_loai AND sp.trang_thai=1
            WHERE l.trang_thai=1 AND EXISTS (
                SELECT 1 FROM dbo.san_pham_bien_the b
                WHERE b.id_san_pham=sp.id_san_pham AND b.trang_thai=1)
            GROUP BY l.id_loai, l.ten_loai ORDER BY l.ten_loai
            """, Map.of(), (rs,i)-> new Category(
                rs.getLong("id_loai"),rs.getString("ten_loai"),rs.getLong("products")));
        return new Collection(products,categories,count,page,pages,q,category);
    }

    public Detail detail(long id) {
        if (id <= 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        List<Detail> result = jdbc.query("""
            SELECT sp.id_san_pham, sp.ten_san_pham, sp.mo_ta, sp.freesize_min_cm, sp.freesize_max_cm, l.ten_loai,
                   COALESCE(cl.ten_chat_lieu,N'') AS material,
                   COALESCE(th.ten_thuong_hieu,N'') AS brand
            FROM dbo.san_pham sp
            JOIN dbo.loai_san_pham l ON l.id_loai=sp.id_loai AND l.trang_thai=1
            LEFT JOIN dbo.chat_lieu cl ON cl.id_chat_lieu=sp.id_chat_lieu
            LEFT JOIN dbo.thuong_hieu th ON th.id_thuong_hieu=sp.id_thuong_hieu
            WHERE sp.id_san_pham=:id AND sp.trang_thai=1 AND EXISTS (
                SELECT 1 FROM dbo.san_pham_bien_the b WHERE b.id_san_pham=sp.id_san_pham AND b.trang_thai=1)
            """,Map.of("id",id),(rs,i)->new Detail(rs.getLong("id_san_pham"),
            rs.getString("ten_san_pham"),rs.getString("ten_loai"),
            rs.getString("material"),rs.getString("brand"),
            rs.getString("mo_ta"),null,null,0,rs.getBigDecimal("freesize_min_cm"),
            rs.getBigDecimal("freesize_max_cm"),List.of(),List.of()));
        if (result.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Detail base = result.get(0);
        List<String> gallery = jdbc.query("""
            SELECT duong_dan_anh FROM dbo.hinh_anh_sp WHERE id_san_pham=:id
            ORDER BY anh_chinh DESC,id_anh
            """,Map.of("id",id),(rs,i)->rs.getString("duong_dan_anh"));
        List<Variant> variants = jdbc.query("""
            SELECT b.id_bien_the, m.ten_mau_sac, s.ten_kich_thuoc, b.gia_ban, b.so_luong_ton
            FROM dbo.san_pham_bien_the b
            JOIN dbo.bien_the_mau_sac m ON m.id_mau_sac=b.id_mau_sac
            JOIN dbo.bien_the_kich_thuoc s ON s.id_kich_thuoc=b.id_kich_thuoc
            WHERE b.id_san_pham=:id AND b.trang_thai=1
            ORDER BY b.gia_ban,b.id_bien_the
            """,Map.of("id",id),(rs,i)->new Variant(rs.getLong("id_bien_the"),
                rs.getString("ten_mau_sac"),rs.getString("ten_kich_thuoc"),
                SizeSelection.modeFor(rs.getString("ten_kich_thuoc")),money(rs.getBigDecimal("gia_ban")),rs.getLong("so_luong_ton")));
        String price = variants.isEmpty() ? "" : variants.get(0).price();
        long stock = variants.stream().mapToLong(Variant::stock).sum();
        return new Detail(base.id(),base.name(),base.category(),base.material(),
                base.brand(),base.description(),gallery.isEmpty()?null:gallery.get(0),
                price,stock,base.freesizeMinCm(),base.freesizeMaxCm(),gallery,variants);
    }

    private static String money(BigDecimal value) {
        if(value==null)return "";
        NumberFormat f=NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        f.setMaximumFractionDigits(0);
        return f.format(value)+" \u20ab";
    }
}
