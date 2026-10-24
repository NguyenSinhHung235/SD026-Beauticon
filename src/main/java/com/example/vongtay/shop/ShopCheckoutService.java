package com.example.vongtay.shop;

import com.example.vongtay.sales.PromotionService;
import com.example.vongtay.sales.SaleSupport;
import com.example.vongtay.sales.VoucherService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShopCheckoutService {
    private final NamedParameterJdbcTemplate jdbc;
    private final ShopCartService carts;
    private final ShopAddressService addresses;
    private final PromotionService promotions;
    private final VoucherService vouchers;

    public ShopCheckoutService(NamedParameterJdbcTemplate jdbc,ShopCartService carts,
                               ShopAddressService addresses,PromotionService promotions,VoucherService vouchers) {
        this.jdbc=jdbc;this.carts=carts;this.addresses=addresses;
        this.promotions=promotions;this.vouchers=vouchers;
    }

    public record Quote(BigDecimal subtotal,BigDecimal discount,BigDecimal shipping,
                        BigDecimal total,String voucherCode) {
        public String subtotalText() {return ShopMoney.format(subtotal);}
        public String discountText() {return ShopMoney.format(discount);}
        public String shippingText() {return ShopMoney.format(shipping);}
        public String totalText() {return ShopMoney.format(total);}
    }
    public record Result(long orderId,BigDecimal total) {}
    private record Item(long variant,int quantity,BigDecimal price,
                        SizeSelection.Choice sizing, String sizeName) {}

    public Quote preview(long customerId,String code) {
        ShopCartService.Basket basket=carts.basket(customerId);
        if(basket.lines().isEmpty())throw new IllegalArgumentException("Giỏ hàng đang trống.");
        if(!basket.canCheckout())throw new IllegalArgumentException("Sản phẩm trong giỏ đã hết hàng hoặc ngừng bán.");
        BigDecimal subtotal=SaleSupport.amount(basket.subtotal(),"Tien hang",true);
        LocalDateTime now=LocalDateTime.now(ZoneOffset.UTC);
        VoucherService.Redemption voucher=vouchers.preview(code,subtotal,now);
        return quote(subtotal,voucher.discount(),code);
    }

    private static Quote quote(BigDecimal subtotal,BigDecimal discount,String code) {
        BigDecimal shipping=ShopMoney.shipping(subtotal);
        BigDecimal total=SaleSupport.amount(subtotal.subtract(discount).add(shipping),"Tong thanh toan",true);
        return new Quote(subtotal,discount,shipping,total,code==null?"":code.trim());
    }

    /** Stock, voucher usage, order and pending payment are committed atomically. */
    @Transactional
    public Result place(long customerId,ShopCheckoutForm form) {
        if(form==null)throw new IllegalArgumentException("Thong tin dat hang khong hop le.");
        String token=canonicalToken(form.getMaYeuCau());
        MapSqlParameterSource customerParam=new MapSqlParameterSource("customer",customerId);
        List<Long> customers=jdbc.queryForList("""
                SELECT id_khach_hang FROM dbo.khach_hang WITH (UPDLOCK,HOLDLOCK)
                WHERE id_khach_hang=:customer AND trang_thai=1
                """,customerParam,Long.class);
        if(customers.isEmpty())throw new IllegalArgumentException("Tai khoan da bi khoa hoac khong ton tai.");

        // Repeated POST with same token returns the original order, never charges stock twice.
        List<Map<String,Object>> previous=jdbc.queryForList("""
                SELECT id_don_hang AS id,tong_tien AS total
                FROM dbo.don_hang WITH (UPDLOCK,HOLDLOCK)
                WHERE ma_yeu_cau_online=:token AND id_khach_hang=:customer AND kenh_ban='ONLINE'
                """,customerParam.addValue("token",token));
        if(!previous.isEmpty())return new Result(((Number)previous.get(0).get("id")).longValue(),
                (BigDecimal)previous.get(0).get("total"));

        String method=form.getPhuongThuc();
        if(!"TIEN_MAT".equals(method) && !"CHUYEN_KHOAN".equals(method))
            throw new IllegalArgumentException("Chi ho tro COD hoac chuyen khoan doi soat thu cong.");
        String note=form.getGhiChu()==null?"":form.getGhiChu().trim();
        if(note.length()>1000)throw new IllegalArgumentException("Ghi chu khong qua 1000 ky tu.");

        List<Long> cartIds=jdbc.queryForList("""
                SELECT id_gio_hang FROM dbo.gio_hang WITH (UPDLOCK,HOLDLOCK)
                WHERE id_khach_hang=:customer
                """,customerParam,Long.class);
        if(cartIds.isEmpty())throw new IllegalArgumentException("Giỏ hàng đang trống.");
        long cartId=cartIds.get(0);
        List<Map<String,Object>> cartLines=jdbc.queryForList("""
                SELECT ct.id_bien_the AS variant,ct.so_luong AS quantity,
                       ct.kieu_kich_thuoc AS sizeMode,ct.chu_vi_co_tay_cm AS wristCm,
                       ct.do_rong_deo AS fit,ct.ghi_chu_kich_thuoc AS sizeNote,
                       k.ten_kich_thuoc AS sizeName,
                       s.freesize_min_cm AS freeMin,s.freesize_max_cm AS freeMax
                FROM dbo.chi_tiet_gio_hang ct WITH (UPDLOCK,HOLDLOCK)
                JOIN dbo.san_pham_bien_the b ON b.id_bien_the=ct.id_bien_the
                JOIN dbo.san_pham s ON s.id_san_pham=b.id_san_pham
                JOIN dbo.bien_the_kich_thuoc k ON k.id_kich_thuoc=b.id_kich_thuoc
                WHERE ct.id_gio_hang=:cart ORDER BY ct.id_chi_tiet_gh
                """,new MapSqlParameterSource("cart",cartId));
        if(cartLines.isEmpty() || cartLines.size()>ShopCartService.MAX_VARIANTS)
            throw new IllegalArgumentException("Gio hang trong hoac vuot so loai cho phep.");
        TreeMap<Long,Integer> requested=new TreeMap<>();
        List<SizeSelection.Choice> selections=new ArrayList<>();
        for(Map<String,Object> row:cartLines) {
            int quantity=((Number)row.get("quantity")).intValue();
            ShopCartService.validateQuantity(quantity);
            long variant=((Number)row.get("variant")).longValue();
            requested.merge(variant,quantity,Integer::sum);
            BigDecimal wrist=(BigDecimal)row.get("wristCm");
            SizeSelection.Choice choice=SizeSelection.validate((String)row.get("sizeName"),
                    (String)row.get("sizeMode"),wrist==null?"":wrist.toPlainString(),
                    (String)row.get("fit"),(String)row.get("sizeNote"),
                    (BigDecimal)row.get("freeMin"),(BigDecimal)row.get("freeMax"));
            selections.add(choice);
        }
        LocalDateTime now=LocalDateTime.now(ZoneOffset.UTC);
        Map<Long,List<PromotionService.Rule>> rules=promotions.activeRules(requested.keySet(),now);
        List<Item> items=new ArrayList<>();
        java.util.Map<Long,BigDecimal> prices=new java.util.HashMap<>();
        BigDecimal subtotal=BigDecimal.ZERO.setScale(2);
        for(Map.Entry<Long,Integer> req:requested.entrySet()) {
            List<Map<String,Object>> records=jdbc.queryForList("""
                    SELECT b.gia_ban AS price,b.so_luong_ton AS stock,
                           b.trang_thai AS active,s.trang_thai AS productActive
                    FROM dbo.san_pham_bien_the b WITH (UPDLOCK,ROWLOCK)
                    JOIN dbo.san_pham s ON s.id_san_pham=b.id_san_pham
                    WHERE b.id_bien_the=:variant
                    """,new MapSqlParameterSource("variant",req.getKey()));
            if(records.isEmpty() || !Boolean.TRUE.equals(records.get(0).get("active"))
                    || !Boolean.TRUE.equals(records.get(0).get("productActive")))
                throw new IllegalArgumentException("Mot san pham trong gio da ngung ban.");
            Map<String,Object> row=records.get(0);
            if(((Number)row.get("stock")).intValue()<req.getValue())
                throw new IllegalArgumentException("Khong du ton kho cho bien the #"+req.getKey());
            BigDecimal price=PromotionService.effectivePrice((BigDecimal)row.get("price"),rules.get(req.getKey()));
            if(price.signum()<0)throw new IllegalArgumentException("Gia ban khong hop le.");
            prices.put(req.getKey(),price);
            subtotal=subtotal.add(price.multiply(BigDecimal.valueOf(req.getValue())));
        }
        // Preserve every distinct wrist measurement as its own order line.
        for (int i=0; i<cartLines.size(); i++) {
            Map<String,Object> line=cartLines.get(i);
            long variant=((Number)line.get("variant")).longValue();
            items.add(new Item(variant,((Number)line.get("quantity")).intValue(),
                    prices.get(variant),selections.get(i),(String)line.get("sizeName")));
        }
        subtotal=SaleSupport.amount(subtotal,"Tien hang",true);
        VoucherService.Redemption voucher=vouchers.consume(form.getMaVoucher(),subtotal,now);
        Quote finalPrice=quote(subtotal,voucher.discount(),form.getMaVoucher());

        final long addressId;
        final ShopAddressService.Address address;
        if(form.getIdDiaChi()!=null && form.getIdDiaChi()>0) {
            addressId=form.getIdDiaChi();
            address=addresses.belongingTo(customerId,addressId);
        } else {
            addressId=addresses.add(customerId,form.getTenNguoiNhan(),form.getSoDienThoai(),
                    form.getDiaChi(),false);
            address=addresses.belongingTo(customerId,addressId);
        }
        Long order=jdbc.queryForObject("""
                INSERT INTO dbo.don_hang(id_khach_hang,id_ma_giam_gia,id_dia_chi_giao_hang,
                    kenh_ban,trang_thai,tien_hang,tien_giam,phi_van_chuyen,
                    ten_nguoi_nhan,sdt_nguoi_nhan,dia_chi_giao_snapshot,ghi_chu,
                    ma_yeu_cau_online,ton_kho_da_tru,voucher_da_su_dung)
                OUTPUT INSERTED.id_don_hang VALUES(:customer,:voucher,:address,
                    'ONLINE','CHUA_XU_LY',:subtotal,:discount,:shipping,
                    :recipient,:phone,:snapshot,:note,:token,1,:voucherUsed)
                """,customerParam.addValue("voucher",voucher.voucherId())
                .addValue("address",addressId).addValue("subtotal",subtotal)
                .addValue("discount",voucher.discount()).addValue("shipping",finalPrice.shipping())
                .addValue("recipient",address.recipient()).addValue("phone",address.phone())
                .addValue("snapshot",address.text()).addValue("note",note)
                .addValue("voucherUsed",voucher.voucherId()!=null),Long.class);
        if(order==null)throw new IllegalStateException("Khong tao duoc don hang online.");
        for(Item line:items) {
            int changed=jdbc.update("""
                    UPDATE dbo.san_pham_bien_the SET so_luong_ton=so_luong_ton-:quantity
                    WHERE id_bien_the=:variant AND so_luong_ton>=:quantity AND trang_thai=1
                    """,new MapSqlParameterSource("variant",line.variant()).addValue("quantity",line.quantity()));
            if(changed!=1)throw new IllegalArgumentException("Tồn kho vừa thay đổi, vui lòng thử lại.");
            jdbc.update("""
                    INSERT INTO dbo.don_hang_chi_tiet(id_don_hang,id_bien_the,so_luong,gia_ban,
                        kieu_kich_thuoc,chu_vi_co_tay_cm,do_rong_deo,ghi_chu_kich_thuoc,
                        ten_kich_thuoc_chot,trang_thai_xac_nhan_size)
                    VALUES(:order,:variant,:quantity,:price,:mode,:wrist,:fit,:note,:sizeName,:confirmation)
                    """,new MapSqlParameterSource("order",order).addValue("variant",line.variant())
                    .addValue("quantity",line.quantity()).addValue("price",line.price())
                    .addValue("mode",line.sizing().mode()).addValue("wrist",line.sizing().wristCm())
                    .addValue("fit",line.sizing().fit()).addValue("note",line.sizing().note())
                    .addValue("sizeName",line.sizeName())
                    .addValue("confirmation",SizeSelection.CUSTOM.equals(line.sizing().mode())
                            ? "CHO_XAC_NHAN" : "KHONG_CAN"));
        }
        jdbc.update("""
                INSERT INTO dbo.thanh_toan(id_don_hang,phuong_thuc,loai_giao_dich,so_tien,trang_thai)
                VALUES(:order,:method,'THANH_TOAN',:amount,'CHO_XU_LY')
                """,new MapSqlParameterSource("order",order).addValue("method",method)
                .addValue("amount",finalPrice.total()));
        jdbc.update("""
                INSERT INTO dbo.lich_su_don_hang(id_don_hang,su_kien,trang_thai_cu,trang_thai_moi,ghi_chu)
                VALUES(:order,'CHUYEN_TRANG_THAI',NULL,'CHUA_XU_LY',N'Khach dat hang online')
                """,new MapSqlParameterSource("order",order));
        jdbc.update("DELETE FROM dbo.chi_tiet_gio_hang WHERE id_gio_hang=:cart",
                new MapSqlParameterSource("cart",cartId));
        jdbc.update("UPDATE dbo.gio_hang SET ngay_cap_nhat=SYSUTCDATETIME() WHERE id_gio_hang=:cart",
                new MapSqlParameterSource("cart",cartId));
        return new Result(order,finalPrice.total());
    }

    public static String canonicalToken(String raw) {
        try {
            String canonical=UUID.fromString(raw).toString();
            if(!canonical.equals(raw))throw new IllegalArgumentException("Invalid token");
            return canonical;
        } catch(RuntimeException ex) {
            throw new IllegalArgumentException("Phien dat hang het han. Hay tai lai trang thanh toan.");
        }
    }
}
