package com.example.vongtay.shop;

import com.example.vongtay.orders.OrderWorkflow;
import com.example.vongtay.sales.SaleSupport;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ShopOrderService {
    private final NamedParameterJdbcTemplate jdbc;
    public ShopOrderService(NamedParameterJdbcTemplate jdbc) {this.jdbc=jdbc;}

    public record Order(long id,String date,String status,String statusText,
                        BigDecimal total,BigDecimal paid,String method,String recipient,
                        String phone,String address,String note,BigDecimal subtotal,
                        BigDecimal discount,BigDecimal shipping,boolean refundPending) {
        public String totalText() {return ShopMoney.format(total);}
        public String subtotalText() {return ShopMoney.format(subtotal);}
        public String discountText() {return ShopMoney.format(discount);}
        public String shippingText() {return ShopMoney.format(shipping);}
        public boolean paidInFull() {return paid.compareTo(total)>=0;}
        public String paymentText() {
            if (OrderWorkflow.CANCELLED.equals(status)) {
                if (refundPending) return "Da huy - cho hoan tien";
                return "Da huy - doi soat ket thuc";
            }
            return paidInFull() ? "Da xac nhan thanh toan"
                    : ("TIEN_MAT".equals(method) ? "Chua thu tien - COD" : "Cho xac nhan chuyen khoan");
        }
    }
    public record Line(String product,String color,String size,int quantity,
                       BigDecimal price,long productId,String image, String sizeMode,
                       BigDecimal wristCm,String fit,String sizeNote,String sizeConfirmation) {
        public String sizeDescription() {
            return new SizeSelection.Choice(sizeMode,wristCm,fit,sizeNote).description(size);
        }
        public boolean waitingSizeConfirmation() { return "CHO_XAC_NHAN".equals(sizeConfirmation); }
        public String priceText() {return ShopMoney.format(price);}
        public String totalText() {return ShopMoney.format(price.multiply(BigDecimal.valueOf(quantity)));}
    }
    public record Detail(Order order,List<Line> lines) {}

    private static final String ORDER_FROM="""
            FROM dbo.don_hang d
            OUTER APPLY(SELECT TOP(1) t.phuong_thuc AS method FROM dbo.thanh_toan t
                WHERE t.id_don_hang=d.id_don_hang AND t.loai_giao_dich='THANH_TOAN'
                ORDER BY t.id_thanh_toan) method
            OUTER APPLY(SELECT COALESCE(SUM(CASE WHEN t.loai_giao_dich='THANH_TOAN' THEN t.so_tien
                                                  ELSE -t.so_tien END),0) AS paid
                FROM dbo.thanh_toan t WHERE t.id_don_hang=d.id_don_hang AND t.trang_thai='THANH_CONG') receipts
            OUTER APPLY(SELECT COUNT_BIG(*) AS waiting
                FROM dbo.thanh_toan t WHERE t.id_don_hang=d.id_don_hang
                AND t.loai_giao_dich='HOAN_TIEN' AND t.trang_thai='CHO_XU_LY') refund
            WHERE d.id_khach_hang=:customer AND d.kenh_ban='ONLINE'
            """;
    private static final String SELECT_ORDER="""
            SELECT d.id_don_hang AS id,d.ngay_dat AS orderedAt,d.trang_thai AS status,
                d.tong_tien AS total,receipts.paid,method.method,d.ten_nguoi_nhan AS recipient,
                d.sdt_nguoi_nhan AS phone,d.dia_chi_giao_snapshot AS address,d.ghi_chu AS note,
                d.tien_hang AS subtotal,d.tien_giam AS discount,d.phi_van_chuyen AS shipping,
                refund.waiting AS refundPending
            """;

    private static Order from(java.sql.ResultSet rs) throws java.sql.SQLException {
        String status=rs.getString("status");
        return new Order(rs.getLong("id"),SaleSupport.toDisplay(rs.getTimestamp("orderedAt")),
                status,OrderWorkflow.label(status),rs.getBigDecimal("total"),rs.getBigDecimal("paid"),
                rs.getString("method"),rs.getString("recipient"),rs.getString("phone"),
                rs.getString("address"),rs.getString("note"),rs.getBigDecimal("subtotal"),
                rs.getBigDecimal("discount"),rs.getBigDecimal("shipping"),rs.getLong("refundPending")>0);
    }

    public List<Order> mine(long customerId) {
        return jdbc.query(SELECT_ORDER+ORDER_FROM+" ORDER BY d.ngay_dat DESC,d.id_don_hang DESC OFFSET 0 ROWS FETCH NEXT 100 ROWS ONLY",
                new MapSqlParameterSource("customer",customerId), (rs,i)->from(rs));
    }

    public Detail detail(long customerId,long orderId) {
        MapSqlParameterSource p=new MapSqlParameterSource("customer",customerId).addValue("id",orderId);
        List<Order> rows=jdbc.query(SELECT_ORDER+ORDER_FROM+" AND d.id_don_hang=:id",
                p,(rs,i)->from(rs));
        if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Order not found");
        List<Line> lines=jdbc.query("""
                SELECT s.id_san_pham,s.ten_san_pham,m.ten_mau_sac,
                    COALESCE(ct.ten_kich_thuoc_chot,k.ten_kich_thuoc) AS ten_kich_thuoc,
                    ct.kieu_kich_thuoc,ct.chu_vi_co_tay_cm,ct.do_rong_deo,
                    ct.ghi_chu_kich_thuoc,ct.trang_thai_xac_nhan_size,
                    ct.so_luong,ct.gia_ban,img.duong_dan_anh
                FROM dbo.don_hang_chi_tiet ct
                JOIN dbo.don_hang d ON d.id_don_hang=ct.id_don_hang
                JOIN dbo.san_pham_bien_the v ON v.id_bien_the=ct.id_bien_the
                JOIN dbo.san_pham s ON s.id_san_pham=v.id_san_pham
                JOIN dbo.bien_the_mau_sac m ON m.id_mau_sac=v.id_mau_sac
                JOIN dbo.bien_the_kich_thuoc k ON k.id_kich_thuoc=v.id_kich_thuoc
                OUTER APPLY(SELECT TOP(1) a.duong_dan_anh FROM dbo.hinh_anh_sp a
                            WHERE a.id_san_pham=s.id_san_pham
                            ORDER BY a.anh_chinh DESC,a.id_anh) img
                WHERE d.id_don_hang=:id AND d.id_khach_hang=:customer AND d.kenh_ban='ONLINE'
                ORDER BY ct.id_dh_chi_tiet
                """,p,(rs,i)->new Line(rs.getString("ten_san_pham"),
                rs.getString("ten_mau_sac"),rs.getString("ten_kich_thuoc"),rs.getInt("so_luong"),
                rs.getBigDecimal("gia_ban"),rs.getLong("id_san_pham"),rs.getString("duong_dan_anh"),
                rs.getString("kieu_kich_thuoc"),rs.getBigDecimal("chu_vi_co_tay_cm"),
                rs.getString("do_rong_deo"),rs.getString("ghi_chu_kich_thuoc"),
                rs.getString("trang_thai_xac_nhan_size")));
        return new Detail(rows.get(0),lines);
    }
}
