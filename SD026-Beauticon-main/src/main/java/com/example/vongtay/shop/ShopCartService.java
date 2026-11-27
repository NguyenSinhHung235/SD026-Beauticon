package com.example.vongtay.shop;

import com.example.vongtay.sales.PromotionService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShopCartService {
    public static final int MAX_QUANTITY = 20;
    public static final int MAX_VARIANTS = 20;
    private final NamedParameterJdbcTemplate jdbc;
    private final PromotionService promotions;

    public ShopCartService(NamedParameterJdbcTemplate jdbc, PromotionService promotions) {
        this.jdbc = jdbc;
        this.promotions = promotions;
    }

    public record Line(long id, long variantId, long productId, String name, String color,
                       String size, String mode, BigDecimal wristCm, String fit, String sizeNote,
                       String image, int quantity, int stock, boolean available,
                       BigDecimal price, BigDecimal lineTotal) {
        public String sizeDescription() {
            return new SizeSelection.Choice(mode, wristCm, fit, sizeNote).description(size);
        }
        public String fitText() { return SizeSelection.fitText(fit); }
        public boolean customSize() { return SizeSelection.CUSTOM.equals(mode); }
        public String priceText() { return ShopMoney.format(price); }
        public String lineTotalText() { return ShopMoney.format(lineTotal); }
    }

    public record Basket(List<Line> lines, int count, BigDecimal subtotal, boolean canCheckout) {
        public String subtotalText() { return ShopMoney.format(subtotal); }
    }

    private record VariantMeta(int stock, String sizeName, BigDecimal freeMin, BigDecimal freeMax) {}
    private record ExistingLine(long id, int qty) {}
    private record ChangeCheck(long variantId, int stock, boolean active, boolean productActive) {}

    public int count(long customerId) {
        Integer count = jdbc.queryForObject("""
                SELECT COALESCE(SUM(c.so_luong),0) FROM dbo.chi_tiet_gio_hang c
                JOIN dbo.gio_hang g ON g.id_gio_hang=c.id_gio_hang
                WHERE g.id_khach_hang=:customer
                """, new MapSqlParameterSource("customer", customerId), Integer.class);
        return count == null ? 0 : count;
    }

    public Basket basket(long customerId) {
        var rows = jdbc.queryForList("""
                SELECT ct.id_chi_tiet_gh AS lineId, ct.id_bien_the AS variantId,
                    ct.so_luong AS quantity, b.so_luong_ton AS stock, b.gia_ban AS price,
                    COALESCE(b.phu_phi_bien_the, 0) AS surcharge,
                    ct.kieu_kich_thuoc AS sizeMode, ct.chu_vi_co_tay_cm AS wristCm,
                    ct.do_rong_deo AS fit, ct.ghi_chu_kich_thuoc AS sizeNote,
                    b.trang_thai AS variantActive, s.trang_thai AS productActive,
                    s.id_san_pham AS productId, s.ten_san_pham AS name,
                    s.trong_luong_gram AS weightGram,
                    s.gram_moi_cm AS gramMoiCm,
                    COALESCE(s.chi_phi_che_tac, 0) AS craftCost,
                    COALESCE(cl.don_gia_gram, 0) AS materialRate,
                    s.freesize_min_cm AS freeMin, s.freesize_max_cm AS freeMax,
                    m.ten_mau_sac AS color, k.ten_kich_thuoc AS size,
                    img.duong_dan_anh AS image
                FROM dbo.chi_tiet_gio_hang ct
                JOIN dbo.gio_hang g ON g.id_gio_hang=ct.id_gio_hang
                JOIN dbo.san_pham_bien_the b ON b.id_bien_the=ct.id_bien_the
                JOIN dbo.san_pham s ON s.id_san_pham=b.id_san_pham
                LEFT JOIN dbo.chat_lieu cl ON cl.id_chat_lieu=s.id_chat_lieu
                JOIN dbo.bien_the_mau_sac m ON m.id_mau_sac=b.id_mau_sac
                JOIN dbo.bien_the_kich_thuoc k ON k.id_kich_thuoc=b.id_kich_thuoc
                OUTER APPLY (SELECT TOP(1) i.duong_dan_anh FROM dbo.hinh_anh_sp i
                    WHERE i.id_san_pham=s.id_san_pham
                    ORDER BY i.anh_chinh DESC,i.id_anh) img
                WHERE g.id_khach_hang=:customer ORDER BY ct.id_chi_tiet_gh
                """, new MapSqlParameterSource("customer", customerId));
        if (rows.isEmpty()) return new Basket(List.of(), 0, BigDecimal.ZERO.setScale(2), false);
        Set<Long> ids = rows.stream().map(r -> ((Number) r.get("variantId")).longValue()).collect(Collectors.toSet());
        var rules = promotions.activeRules(ids, LocalDateTime.now(ZoneOffset.UTC));
        List<Line> lines = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO.setScale(2);
        int count = 0;
        boolean available = true;
        Map<Long, Integer> perVariant = new HashMap<>();
        for (var r : rows) {
            long variant = ((Number) r.get("variantId")).longValue();
            perVariant.merge(variant, ((Number) r.get("quantity")).intValue(), Integer::sum);
        }
        for (var r : rows) {
            long id = ((Number) r.get("variantId")).longValue();
            int quantity = ((Number) r.get("quantity")).intValue();
            int stock = ((Number) r.get("stock")).intValue();
            String mode = (String) r.get("sizeMode");
            BigDecimal wrist = (BigDecimal) r.get("wristCm");
            String fit = (String) r.get("fit");
            String sizeName = (String) r.get("size");
            boolean ok = Boolean.TRUE.equals(r.get("productActive"))
                    && Boolean.TRUE.equals(r.get("variantActive")) && stock >= perVariant.get(id)
                    && (!("FREESIZE".equals(mode))
                    || (r.get("freeMin") != null && r.get("freeMax") != null));
            BigDecimal baseUnitPrice = CustomCmPricing.resolvePrice(
                    mode, wrist, fit, sizeName,
                    (BigDecimal) r.get("gramMoiCm"),
                    (BigDecimal) r.get("weightGram"),
                    (BigDecimal) r.get("materialRate"),
                    (BigDecimal) r.get("craftCost"),
                    (BigDecimal) r.get("surcharge"),
                    (BigDecimal) r.get("price"));
            BigDecimal price = PromotionService.effectivePrice(baseUnitPrice, rules.get(id));
            BigDecimal total = price.multiply(BigDecimal.valueOf(quantity));
            lines.add(new Line(((Number) r.get("lineId")).longValue(), id,
                    ((Number) r.get("productId")).longValue(), (String) r.get("name"),
                    (String) r.get("color"), sizeName,
                    mode, wrist,
                    fit, (String) r.get("sizeNote"), (String) r.get("image"),
                    quantity, stock, ok, price, total));
            subtotal = subtotal.add(total);
            count += quantity;
            if (!ok) available = false;
        }
        return new Basket(lines, count, subtotal, available && subtotal.signum() > 0);
    }

    @Transactional
    public void add(long customerId, long variantId, int quantity) {
        add(customerId, variantId, quantity, "", "", "", "");
    }

    @Transactional
    public void add(long customerId, long variantId, int quantity,
                    String postedMode, String rawWrist, String rawFit, String rawNote) {
        validateQuantity(quantity);
        long cart = lockedCart(customerId);
        List<VariantMeta> variants = jdbc.query("""
                SELECT b.so_luong_ton AS stock, k.ten_kich_thuoc AS sizeName,
                    s.freesize_min_cm AS freeMin, s.freesize_max_cm AS freeMax
                FROM dbo.san_pham_bien_the b
                JOIN dbo.san_pham s ON s.id_san_pham=b.id_san_pham
                JOIN dbo.bien_the_kich_thuoc k ON k.id_kich_thuoc=b.id_kich_thuoc
                WHERE b.id_bien_the=:variant AND b.trang_thai=1 AND s.trang_thai=1
                """,
                Map.of("variant", variantId),
                (rs, rowNum) -> new VariantMeta(
                        rs.getInt("stock"),
                        rs.getString("sizeName"),
                        rs.getBigDecimal("freeMin"),
                        rs.getBigDecimal("freeMax")));
        if (variants.isEmpty()) throw new IllegalArgumentException("Phiên bản sản phẩm không còn bán.");
        VariantMeta variant = variants.get(0);
        SizeSelection.Choice choice = SizeSelection.validate(variant.sizeName(),
                postedMode, rawWrist, rawFit, rawNote,
                variant.freeMin(), variant.freeMax());
        int stock = variant.stock();

        MapSqlParameterSource p = new MapSqlParameterSource("cart", cart)
                .addValue("variant", variantId)
                .addValue("mode", choice.mode())
                .addValue("wrist", choice.wristCm())
                .addValue("fit", choice.fit())
                .addValue("note", choice.note());

        List<ExistingLine> existing = jdbc.query("""
                SELECT id_chi_tiet_gh AS id, so_luong AS qty FROM dbo.chi_tiet_gio_hang WITH (UPDLOCK,HOLDLOCK)
                WHERE id_gio_hang=:cart AND id_bien_the=:variant AND kieu_kich_thuoc=:mode
                  AND (chu_vi_co_tay_cm=:wrist OR (chu_vi_co_tay_cm IS NULL AND :wrist IS NULL))
                  AND (do_rong_deo=:fit OR (do_rong_deo IS NULL AND :fit IS NULL))
                """,
                p,
                (rs, rowNum) -> new ExistingLine(rs.getLong("id"), rs.getInt("qty")));

        int wanted = (existing.isEmpty() ? 0 : existing.get(0).qty()) + quantity;
        if (wanted > MAX_QUANTITY)
            throw new IllegalArgumentException("Mỗi lựa chọn kích thước chỉ được đặt tối đa 20 chiếc.");
        Integer totalForVariant = jdbc.queryForObject("""
                SELECT COALESCE(SUM(so_luong),0) FROM dbo.chi_tiet_gio_hang
                WHERE id_gio_hang=:cart AND id_bien_the=:variant
                """, p, Integer.class);
        if ((totalForVariant == null ? 0 : totalForVariant) + quantity > stock)
            throw new IllegalArgumentException("Số lượng cộng các kích thước vượt tồn kho của phiên bản này.");
        if (existing.isEmpty()) {
            Long lines = jdbc.queryForObject("SELECT COUNT_BIG(*) FROM dbo.chi_tiet_gio_hang WHERE id_gio_hang=:cart", p, Long.class);
            if (lines != null && lines >= MAX_VARIANTS)
                throw new IllegalArgumentException("Giỏ hàng tối đa 20 dòng sản phẩm.");
            jdbc.update("""
                    INSERT INTO dbo.chi_tiet_gio_hang(id_gio_hang,id_bien_the,so_luong,
                        kieu_kich_thuoc,chu_vi_co_tay_cm,do_rong_deo,ghi_chu_kich_thuoc)
                    VALUES(:cart,:variant,:qty,:mode,:wrist,:fit,:note)
                    """, p.addValue("qty", quantity));
        } else {
            jdbc.update("""
                    UPDATE dbo.chi_tiet_gio_hang SET so_luong=:qty,ghi_chu_kich_thuoc=:note
                    WHERE id_chi_tiet_gh=:line AND id_gio_hang=:cart
                    """, p.addValue("qty", wanted).addValue("line", existing.get(0).id()));
        }
        touch(cart);
    }

    @Transactional
    public void change(long customerId, long lineId, int quantity) {
        validateQuantity(quantity);
        long cart = lockedCart(customerId);
        List<ChangeCheck> rows = jdbc.query("""
                SELECT ct.id_bien_the AS variant, b.so_luong_ton AS stock,
                    b.trang_thai AS active, s.trang_thai AS productActive
                FROM dbo.chi_tiet_gio_hang ct WITH (UPDLOCK,HOLDLOCK)
                JOIN dbo.san_pham_bien_the b ON b.id_bien_the=ct.id_bien_the
                JOIN dbo.san_pham s ON s.id_san_pham=b.id_san_pham
                WHERE ct.id_gio_hang=:cart AND ct.id_chi_tiet_gh=:line
                """,
                Map.of("cart", cart, "line", lineId),
                (rs, rowNum) -> new ChangeCheck(
                        rs.getLong("variant"),
                        rs.getInt("stock"),
                        rs.getBoolean("active"),
                        rs.getBoolean("productActive")));
        if (rows.isEmpty()) throw new IllegalArgumentException("Sản phẩm không có trong giỏ hàng.");
        ChangeCheck info = rows.get(0);
        if (!info.active() || !info.productActive() || quantity > info.stock())
            throw new IllegalArgumentException("Sản phẩm đã hết hàng hoặc không còn bán.");
        Integer otherQty = jdbc.queryForObject("""
                SELECT COALESCE(SUM(so_luong),0) FROM dbo.chi_tiet_gio_hang
                WHERE id_gio_hang=:cart AND id_bien_the=:variant AND id_chi_tiet_gh<>:line
                """,
                Map.of("cart", cart, "variant", info.variantId(), "line", lineId),
                Integer.class);
        if (quantity + (otherQty == null ? 0 : otherQty) > info.stock())
            throw new IllegalArgumentException("Tổng số lượng các kích thước của mẫu này vượt tồn kho.");
        jdbc.update("UPDATE dbo.chi_tiet_gio_hang SET so_luong=:qty WHERE id_gio_hang=:cart AND id_chi_tiet_gh=:line",
                Map.of("qty", quantity, "cart", cart, "line", lineId));
        touch(cart);
    }

    @Transactional
    public void remove(long customerId, long lineId) {
        long cart = lockedCart(customerId);
        int changed = jdbc.update("DELETE FROM dbo.chi_tiet_gio_hang WHERE id_gio_hang=:cart AND id_chi_tiet_gh=:line",
                Map.of("cart", cart, "line", lineId));
        if (changed != 1) throw new IllegalArgumentException("Sản phẩm không có trong giỏ hàng.");
        touch(cart);
    }

    private void touch(long cart) {
        jdbc.update("UPDATE dbo.gio_hang SET ngay_cap_nhat=SYSUTCDATETIME() WHERE id_gio_hang=:cart",
                Map.of("cart", cart));
    }

    private long lockedCart(long customerId) {
        List<Long> owners = jdbc.queryForList("""
                SELECT id_khach_hang FROM dbo.khach_hang WITH (UPDLOCK,HOLDLOCK)
                WHERE id_khach_hang=:id AND trang_thai=1
                """, new MapSqlParameterSource("id", customerId), Long.class);
        if (owners.isEmpty()) throw new IllegalArgumentException("Tài khoản khách hàng không hoạt động.");
        List<Long> carts = jdbc.queryForList("""
                SELECT id_gio_hang FROM dbo.gio_hang WITH (UPDLOCK,HOLDLOCK)
                WHERE id_khach_hang=:id
                """, new MapSqlParameterSource("id", customerId), Long.class);
        if (!carts.isEmpty()) return carts.get(0);
        Long newId = jdbc.queryForObject("""
                INSERT dbo.gio_hang(id_khach_hang) OUTPUT INSERTED.id_gio_hang VALUES(:id)
                """, new MapSqlParameterSource("id", customerId), Long.class);
        if (newId == null) throw new IllegalStateException("Không tạo được giỏ hàng.");
        return newId;
    }

    public static void validateQuantity(int quantity) {
        if (quantity < 1 || quantity > MAX_QUANTITY)
            throw new IllegalArgumentException("Số lượng phải từ 1 đến 20.");
    }
}