package com.example.vongtay.orders;

import com.example.vongtay.sales.SaleSupport;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {
    private static final int PAGE_SIZE = 15;
    private final NamedParameterJdbcTemplate jdbc;

    public OrderService(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    public record Listing(List<Map<String, Object>> rows, long count, int page, int pageCount,
                          String channel, String status, String query, boolean invoicesOnly) {}

    public record Detail(Map<String, Object> order, List<Map<String,Object>> lines,
                         List<Map<String,Object>> payments, List<Map<String,Object>> history,
                         boolean paidInFull, boolean hasPendingRefund, boolean mayCancel,
                         String nextStatus, String nextStatusLabel) {}

    public List<Map<String,Object>> staff() {
        return jdbc.queryForList("""
                SELECT id_nhan_vien AS id, ho_ten AS name FROM dbo.nhan_vien
                WHERE trang_thai=1 ORDER BY ho_ten, id_nhan_vien
                """, new MapSqlParameterSource());
    }

    public Listing list(String channel, String status, String query, Integer requestedPage, boolean invoicesOnly) {
        if (!"ALL".equals(channel) && !"ONLINE".equals(channel) && !"OFFLINE".equals(channel))
            throw new IllegalArgumentException("Kenh ban khong hop le.");
        if (!"ALL".equals(status) && !List.of("CHUA_XU_LY", "DANG_XU_LY", "DA_XU_LY", "HOAN_THANH", "DA_HUY").contains(status))
            throw new IllegalArgumentException("Trang thai loc khong hop le.");
        String q = query == null ? "" : query.trim();
        if (q.length() > 80) throw new IllegalArgumentException("Tu khoa khong qua 80 ky tu.");
        // Escape SQL LIKE wildcards; names and order IDs remain plain text search.
        String pattern = "%" + q.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_").replace("[", "\\[") + "%";
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("channel", channel).addValue("status", status)
                .addValue("q", q).addValue("pattern", pattern);
        String filter = """
                FROM dbo.don_hang d
                LEFT JOIN dbo.khach_hang k ON k.id_khach_hang=d.id_khach_hang
                WHERE (:channel='ALL' OR d.kenh_ban=:channel)
                  AND (:status='ALL' OR d.trang_thai=:status)
                  AND (:q='' OR CAST(d.id_don_hang AS VARCHAR(20)) LIKE :pattern ESCAPE '\\'
                      OR k.ho_ten LIKE :pattern ESCAPE '\\'
                      OR d.ten_nguoi_nhan LIKE :pattern ESCAPE '\\')
                """ + (invoicesOnly ? " AND d.trang_thai='HOAN_THANH'\n" : "");
        Long n = jdbc.queryForObject("SELECT COUNT_BIG(*) " + filter, p, Long.class);
        long total = n == null ? 0 : n;
        int pages = Math.max(1, (int)Math.min(Integer.MAX_VALUE, (total + PAGE_SIZE - 1) / PAGE_SIZE));
        int page = Math.min(Math.max(requestedPage == null ? 1 : requestedPage, 1), pages);
        p.addValue("offset", (page-1)*PAGE_SIZE).addValue("size", PAGE_SIZE);
        List<Map<String,Object>> rows = jdbc.query("""
                SELECT d.id_don_hang AS id, d.kenh_ban AS channel, d.trang_thai AS status,
                    d.ngay_dat AS orderAt, d.tong_tien AS total,
                    COALESCE(k.ho_ten,d.ten_nguoi_nhan,N'Khách vãng lai') AS customer,
                    COALESCE(nv.ho_ten,N'Chưa phân công') AS staff,
                    COALESCE(pay.paid,0)-COALESCE(pay.refunded,0) AS paidNet
                """ + filter.replace("LEFT JOIN dbo.khach_hang k ON k.id_khach_hang=d.id_khach_hang",
                """
                   LEFT JOIN dbo.khach_hang k ON k.id_khach_hang=d.id_khach_hang
                   LEFT JOIN dbo.nhan_vien nv ON nv.id_nhan_vien=d.id_nhan_vien
                   OUTER APPLY (
                     SELECT SUM(CASE WHEN t.loai_giao_dich='THANH_TOAN' THEN t.so_tien ELSE 0 END) AS paid,
                            SUM(CASE WHEN t.loai_giao_dich='HOAN_TIEN' THEN t.so_tien ELSE 0 END) AS refunded
                     FROM dbo.thanh_toan t
                     WHERE t.id_don_hang=d.id_don_hang AND t.trang_thai='THANH_CONG'
                   ) pay""") + " ORDER BY d.ngay_dat DESC,d.id_don_hang DESC OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY",
                p, (rs, index) -> {
                    Map<String,Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getLong("id"));
                    row.put("channel", rs.getString("channel"));
                    row.put("status", rs.getString("status"));
                    row.put("statusLabel", OrderWorkflow.label(rs.getString("status")));
                    row.put("orderAt", SaleSupport.toDisplay(rs.getTimestamp("orderAt")));
                    row.put("total", rs.getBigDecimal("total"));
                    row.put("paidNet", rs.getBigDecimal("paidNet"));
                    row.put("customer", rs.getString("customer"));
                    row.put("staff", rs.getString("staff"));
                    return row;
                });
        return new Listing(rows, total, page, pages, channel, status, q, invoicesOnly);
    }

    public Detail detail(long id) {
        Map<String,Object> o = findOrder(id, false);
        BigDecimal paidNet = (BigDecimal)o.get("paidNet");
        BigDecimal total = (BigDecimal)o.get("total");
        List<Map<String,Object>> lines = jdbc.queryForList("""
                SELECT ct.id_dh_chi_tiet AS id, s.ten_san_pham AS product,
                    m.ten_mau_sac AS color,
                    COALESCE(ct.ten_kich_thuoc_chot,z.ten_kich_thuoc) AS size,
                    ct.kieu_kich_thuoc AS sizeMode,ct.chu_vi_co_tay_cm AS wristCm,
                    ct.do_rong_deo AS fit,ct.ghi_chu_kich_thuoc AS sizeNote,
                    ct.trang_thai_xac_nhan_size AS sizeConfirmation,
                    ct.so_luong AS quantity, ct.gia_ban AS price,
                    ct.so_luong * ct.gia_ban AS lineTotal
                FROM dbo.don_hang_chi_tiet ct
                JOIN dbo.san_pham_bien_the b ON b.id_bien_the=ct.id_bien_the
                JOIN dbo.san_pham s ON s.id_san_pham=b.id_san_pham
                JOIN dbo.bien_the_mau_sac m ON m.id_mau_sac=b.id_mau_sac
                JOIN dbo.bien_the_kich_thuoc z ON z.id_kich_thuoc=b.id_kich_thuoc
                WHERE ct.id_don_hang=:id ORDER BY ct.id_dh_chi_tiet
                """, idParam(id));
        List<Map<String,Object>> payments = jdbc.query("""
                SELECT t.loai_giao_dich AS type,t.phuong_thuc AS method,
                       t.so_tien AS amount,t.trang_thai AS status,
                       t.ma_giao_dich AS code,t.ma_doi_soat AS externalReference,
                       t.ngay_thanh_toan AS paidAt
                FROM dbo.thanh_toan t WHERE t.id_don_hang=:id ORDER BY t.id_thanh_toan
                """, idParam(id), (rs, idx) -> {
                    Map<String,Object> m = new LinkedHashMap<>();
                    m.put("type",rs.getString("type")); m.put("method",rs.getString("method"));
                    m.put("amount",rs.getBigDecimal("amount")); m.put("status",rs.getString("status"));
                    m.put("code",rs.getString("code")); m.put("externalReference",rs.getString("externalReference"));
                    Timestamp ts=rs.getTimestamp("paidAt");
                    m.put("paidAt", ts == null ? "—" : SaleSupport.toDisplay(ts));
                    return m;
                });
        List<Map<String,Object>> history = jdbc.query("""
                SELECT l.su_kien AS event,l.trang_thai_cu AS oldStatus,
                    l.trang_thai_moi AS newStatus,l.ghi_chu AS note,
                    l.ngay_tao AS at,COALESCE(n.ho_ten,N'Hệ thống') AS staff
                FROM dbo.lich_su_don_hang l
                LEFT JOIN dbo.nhan_vien n ON n.id_nhan_vien=l.id_nhan_vien
                WHERE l.id_don_hang=:id ORDER BY l.id_lich_su DESC
                """, idParam(id), (rs, idx) -> {
                    Map<String,Object> m = new LinkedHashMap<>();
                    m.put("event",rs.getString("event")); m.put("oldStatus",rs.getString("oldStatus"));
                    m.put("newStatus",rs.getString("newStatus")); m.put("note",rs.getString("note"));
                    m.put("staff",rs.getString("staff")); m.put("at",SaleSupport.toDisplay(rs.getTimestamp("at")));
                    return m;
                });
        Long pending = jdbc.queryForObject("""
                SELECT COUNT_BIG(*) FROM dbo.thanh_toan
                WHERE id_don_hang=:id AND loai_giao_dich='HOAN_TIEN' AND trang_thai='CHO_XU_LY'
                """, idParam(id), Long.class);
        String next = OrderWorkflow.next((String)o.get("status"));
        boolean online = "ONLINE".equals(o.get("channel"));
        return new Detail(o, lines, payments, history, paidNet.compareTo(total) >= 0,
                pending != null && pending > 0,
                online && OrderWorkflow.mayCancel((String)o.get("status")),
                online ? next : null, next == null ? "" : OrderWorkflow.label(next));
    }

    @Transactional
    public void advance(long id, long employeeId) {
        requireStaff(employeeId);
        Map<String,Object> o = findOrder(id, true);
        onlineOnly(o);
        String previous = (String)o.get("status");
        String next = OrderWorkflow.next(previous);
        if (next == null) throw new IllegalArgumentException("Don khong o trang thai duoc chuyen tiep.");
        Long unconfirmed=jdbc.queryForObject("""
                SELECT COUNT_BIG(*) FROM dbo.don_hang_chi_tiet
                WHERE id_don_hang=:id AND trang_thai_xac_nhan_size='CHO_XAC_NHAN'
                """,idParam(id),Long.class);
        if (unconfirmed!=null && unconfirmed>0)
            throw new IllegalArgumentException("Hãy xác nhận kích thước đặt riêng trước khi chuyển trạng thái đơn.");
        if (OrderWorkflow.COMPLETED.equals(next)) {
            BigDecimal net = (BigDecimal)o.get("paidNet");
            if (net.compareTo((BigDecimal)o.get("total")) < 0)
                throw new IllegalArgumentException("Chi hoan thanh khi da nhan du so tien thanh toan.");
        }
        reserveIfNeeded(o, id);
        int updated = jdbc.update("""
                UPDATE dbo.don_hang SET trang_thai=:next,
                    id_nhan_vien=COALESCE(id_nhan_vien,:staff),
                    ngay_cap_nhat=SYSUTCDATETIME(),
                    ngay_hoan_thanh=CASE WHEN :next='HOAN_THANH' THEN SYSUTCDATETIME() ELSE ngay_hoan_thanh END
                WHERE id_don_hang=:id AND trang_thai=:previous
                """, idParam(id).addValue("next", next).addValue("previous",previous).addValue("staff",employeeId));
        if (updated != 1) throw new IllegalArgumentException("Trang thai don da thay doi. Tai lai trang.");
        audit(id,employeeId,"CHUYEN_TRANG_THAI",previous,next,"Cap nhat trang thai online.");
    }

    /** Employees confirm measurements on a single ordered line, never on the product itself. */
    @Transactional
    public void confirmSize(long orderId,long lineId,long employeeId) {
        requireStaff(employeeId);
        Map<String,Object> o=findOrder(orderId,true);
        onlineOnly(o);
        String status=(String)o.get("status");
        if (OrderWorkflow.CANCELLED.equals(status) || OrderWorkflow.COMPLETED.equals(status))
            throw new IllegalArgumentException("Không thể xác nhận kích thước trên đơn đã đóng.");
        int updated=jdbc.update("""
                UPDATE dbo.don_hang_chi_tiet
                SET trang_thai_xac_nhan_size='DA_XAC_NHAN'
                WHERE id_dh_chi_tiet=:line AND id_don_hang=:id
                  AND kieu_kich_thuoc='THEO_YEU_CAU'
                  AND trang_thai_xac_nhan_size='CHO_XAC_NHAN'
                """,idParam(orderId).addValue("line",lineId));
        if (updated!=1) throw new IllegalArgumentException("Yêu cầu kích thước không tồn tại hoặc đã được xác nhận.");
        audit(orderId,employeeId,"XAC_NHAN_KICH_THUOC",status,status,
                "Đã xác nhận số đo cho dòng sản phẩm #"+lineId+" trước khi thực hiện đơn hàng.");
    }

    @Transactional
    public void cancel(long id, long employeeId, String reason) {
        requireStaff(employeeId);
        String why = reason == null ? "" : reason.trim();
        if (why.length() < 5 || why.length() > 500)
            throw new IllegalArgumentException("Ly do huy tu 5 den 500 ky tu.");
        Map<String,Object> o = findOrder(id,true);
        onlineOnly(o);
        String previous = (String)o.get("status");
        if (!OrderWorkflow.mayCancel(previous))
            throw new IllegalArgumentException("Khong the huy don da hoan thanh hoac da huy.");
        if (Boolean.TRUE.equals(o.get("stockReserved"))) {
            List<Map<String,Object>> products = orderLines(id);
            for (Map<String,Object> item: products) {
                int n = jdbc.update("""
                    UPDATE dbo.san_pham_bien_the
                    SET so_luong_ton=so_luong_ton+:qty
                    WHERE id_bien_the=:variant AND so_luong_ton<=2147483647-:qty
                    """, new MapSqlParameterSource("qty", item.get("quantity"))
                      .addValue("variant", item.get("variant")));
                if (n != 1) throw new IllegalArgumentException("Khong hoan duoc ton kho. Huy bi rollback.");
            }
        }
        if (Boolean.TRUE.equals(o.get("voucherUsed")) && o.get("voucherId") != null) {
            int n = jdbc.update("""
                    UPDATE dbo.ma_giam_gia SET so_luong_da_dung=so_luong_da_dung-1
                    WHERE id_ma_giam_gia=:id AND so_luong_da_dung>0
                    """, new MapSqlParameterSource("id",o.get("voucherId")));
            if (n != 1) throw new IllegalArgumentException("So luot voucher khong hop le. Huy bi rollback.");
        }
        jdbc.update("""
                UPDATE dbo.don_hang SET trang_thai='DA_HUY',ngay_huy=SYSUTCDATETIME(),
                    ngay_cap_nhat=SYSUTCDATETIME(),ly_do_huy=:reason,
                    ton_kho_da_tru=0,voucher_da_su_dung=0,
                    id_nhan_vien=COALESCE(id_nhan_vien,:staff)
                WHERE id_don_hang=:id
                """, idParam(id).addValue("reason",why).addValue("staff",employeeId));
        BigDecimal net = (BigDecimal)o.get("paidNet");
        if (net.signum() > 0) {
            Long existing = jdbc.queryForObject("""
                    SELECT COUNT_BIG(*) FROM dbo.thanh_toan
                    WHERE id_don_hang=:id AND loai_giao_dich='HOAN_TIEN' AND trang_thai='CHO_XU_LY'
                    """,idParam(id),Long.class);
            if (existing != null && existing > 0)
                throw new IllegalArgumentException("Da co yeu cau hoan tien dang cho. Vui long kiem tra.");
            String method = jdbc.queryForObject("""
                    SELECT TOP(1) phuong_thuc FROM dbo.thanh_toan
                    WHERE id_don_hang=:id AND loai_giao_dich='THANH_TOAN' AND trang_thai='THANH_CONG'
                    ORDER BY id_thanh_toan DESC
                    """, idParam(id), String.class);
            // A pending refund is an obligation, NOT proof that money was returned.
            jdbc.update("""
                    INSERT dbo.thanh_toan(id_don_hang,phuong_thuc,loai_giao_dich,so_tien,
                        trang_thai,ma_giao_dich)
                    VALUES(:id,:method,'HOAN_TIEN',:amount,'CHO_XU_LY',:code)
                    """, idParam(id).addValue("method",method).addValue("amount",net)
                      .addValue("code","RFD-"+id));
        }
        audit(id,employeeId,"HUY_DON",previous,OrderWorkflow.CANCELLED,why);
    }

    @Transactional
    public void confirmReceipt(long id, long employeeId, String method, boolean moneyReceived, String reference) {
        requireStaff(employeeId);
        if (!moneyReceived) throw new IllegalArgumentException("Phai xac nhan da thuc su nhan tien.");
        if (!"TIEN_MAT".equals(method) && !"CHUYEN_KHOAN".equals(method))
            throw new IllegalArgumentException("Chi ho tro ghi nhan thu tien mat hoac chuyen khoan thu cong.");
        String code = validateReference(reference, "CHUYEN_KHOAN".equals(method));
        Map<String,Object> o = findOrder(id,true);
        onlineOnly(o);
        if (!OrderWorkflow.mayCancel((String)o.get("status")))
            throw new IllegalArgumentException("Khong thu tien cho don da dong/da huy.");
        BigDecimal remaining = ((BigDecimal)o.get("total")).subtract((BigDecimal)o.get("paidNet"));
        if (remaining.signum() <= 0) throw new IllegalArgumentException("Don da thanh toan du. Khong ghi trung.");
        jdbc.update("""
                INSERT INTO dbo.thanh_toan(id_don_hang,phuong_thuc,loai_giao_dich,so_tien,
                    trang_thai,ma_giao_dich,ma_doi_soat,ngay_thanh_toan)
                VALUES(:id,:method,'THANH_TOAN',:amount,'THANH_CONG',:tx,:external,SYSUTCDATETIME())
                """,idParam(id).addValue("method",method).addValue("amount",remaining)
                    .addValue("tx","MANUAL-"+UUID.randomUUID()).addValue("external",code));
        jdbc.update("UPDATE dbo.don_hang SET id_nhan_vien=COALESCE(id_nhan_vien,:staff),ngay_cap_nhat=SYSUTCDATETIME() WHERE id_don_hang=:id",
                idParam(id).addValue("staff",employeeId));
        audit(id,employeeId,"XAC_NHAN_THU_TIEN",(String)o.get("status"),(String)o.get("status"),
                "Da doi soat thu "+remaining+" VND, "+method+(code.isEmpty()?"":" - "+code));
    }

    @Transactional
    public void confirmRefund(long id, long employeeId, boolean moneyRefunded, String reference) {
        requireStaff(employeeId);
        if (!moneyRefunded) throw new IllegalArgumentException("Chi xac nhan sau khi da thuc su hoan tien.");
        Map<String,Object> o = findOrder(id,true);
        onlineOnly(o);
        if (!OrderWorkflow.CANCELLED.equals(o.get("status")))
            throw new IllegalArgumentException("Chi hoan tien cho don da huy.");
        List<Map<String,Object>> pending = jdbc.queryForList("""
                SELECT id_thanh_toan AS id,phuong_thuc AS method,so_tien AS amount
                FROM dbo.thanh_toan WITH (UPDLOCK,HOLDLOCK)
                WHERE id_don_hang=:id AND loai_giao_dich='HOAN_TIEN' AND trang_thai='CHO_XU_LY'
                """,idParam(id));
        if (pending.size()!=1) throw new IllegalArgumentException("Khong co dung mot giao dich cho hoan tien.");
        Map<String,Object> tx = pending.get(0);
        String external = validateReference(reference, !"TIEN_MAT".equals(tx.get("method")));
        BigDecimal amount = (BigDecimal)tx.get("amount");
        if (amount.compareTo((BigDecimal)o.get("paidNet")) > 0)
            throw new IllegalArgumentException("So tien hoan vuot qua tien thuc da nhan.");
        int updated = jdbc.update("""
                UPDATE dbo.thanh_toan SET trang_thai='THANH_CONG',ngay_thanh_toan=SYSUTCDATETIME(),
                    ma_doi_soat=:external
                WHERE id_thanh_toan=:tx AND trang_thai='CHO_XU_LY'
                """,new MapSqlParameterSource("tx",tx.get("id")).addValue("external",external));
        if(updated!=1) throw new IllegalArgumentException("Giao dich hoan tien da thay doi.");
        audit(id,employeeId,"XAC_NHAN_HOAN_TIEN",OrderWorkflow.CANCELLED,OrderWorkflow.CANCELLED,
                "Da doi soat hoan "+amount+" VND"+(external.isEmpty()?"":" - "+external));
    }

    private void reserveIfNeeded(Map<String,Object> o, long id) {
        if (Boolean.TRUE.equals(o.get("stockReserved"))) return;
        List<Map<String,Object>> rows = orderLines(id);
        if(rows.isEmpty()) throw new IllegalArgumentException("Don hang khong co san pham.");
        for(Map<String,Object> item: rows) {
            int n = jdbc.update("""
                    UPDATE dbo.san_pham_bien_the SET so_luong_ton=so_luong_ton-:qty
                    WHERE id_bien_the=:variant AND so_luong_ton>=:qty AND trang_thai=1
                    """, new MapSqlParameterSource("qty",item.get("quantity"))
                      .addValue("variant",item.get("variant")));
            if(n!=1) throw new IllegalArgumentException("Khong du ton kho cho bien the #"+item.get("variant")+". Khong chuyen trang thai.");
        }
        jdbc.update("UPDATE dbo.don_hang SET ton_kho_da_tru=1 WHERE id_don_hang=:id",idParam(id));
    }

    private List<Map<String,Object>> orderLines(long id) {
        return jdbc.queryForList("""
                SELECT id_bien_the AS variant,so_luong AS quantity
                FROM dbo.don_hang_chi_tiet WHERE id_don_hang=:id ORDER BY id_bien_the
                """, idParam(id));
    }

    private Map<String,Object> findOrder(long id, boolean lock) {
        String hint = lock ? " WITH (UPDLOCK,HOLDLOCK)" : "";
        List<Map<String,Object>> rows = jdbc.queryForList("""
                SELECT d.id_don_hang AS id,d.id_ma_giam_gia AS voucherId,
                    d.kenh_ban AS channel,d.trang_thai AS status,d.ngay_dat AS orderAt,
                    d.tong_tien AS total,d.tien_hang AS subtotal,d.tien_giam AS discount,
                    d.phi_van_chuyen AS shipping,d.ten_nguoi_nhan AS recipient,
                    d.sdt_nguoi_nhan AS phone,d.dia_chi_giao_snapshot AS shippingAddress,
                    d.ly_do_huy AS cancelReason,d.ghi_chu AS note,
                    d.ton_kho_da_tru AS stockReserved,d.voucher_da_su_dung AS voucherUsed,
                    COALESCE(k.ho_ten,d.ten_nguoi_nhan,N'Khách vãng lai') AS customer,
                    COALESCE(nv.ho_ten,N'Chưa phân công') AS staff,
                    COALESCE(v.code,N'') AS voucher,
                    COALESCE(p.paid,0) AS paidAmount,COALESCE(p.refunded,0) AS refundedAmount,
                    COALESCE(p.paid,0)-COALESCE(p.refunded,0) AS paidNet
                FROM dbo.don_hang d %s
                LEFT JOIN dbo.khach_hang k ON k.id_khach_hang=d.id_khach_hang
                LEFT JOIN dbo.nhan_vien nv ON nv.id_nhan_vien=d.id_nhan_vien
                LEFT JOIN dbo.ma_giam_gia v ON v.id_ma_giam_gia=d.id_ma_giam_gia
                OUTER APPLY (
                    SELECT SUM(CASE WHEN loai_giao_dich='THANH_TOAN' THEN so_tien ELSE 0 END) AS paid,
                           SUM(CASE WHEN loai_giao_dich='HOAN_TIEN' THEN so_tien ELSE 0 END) AS refunded
                    FROM dbo.thanh_toan WHERE id_don_hang=d.id_don_hang AND trang_thai='THANH_CONG'
                ) p
                WHERE d.id_don_hang=:id
                """.formatted(hint), idParam(id));
        if(rows.isEmpty()) throw new IllegalArgumentException("Khong tim thay don hang #"+id+".");
        Map<String,Object> m = new LinkedHashMap<>(rows.get(0));
        m.put("orderAtText", SaleSupport.toDisplay(m.get("orderAt")));
        m.put("statusLabel",OrderWorkflow.label((String)m.get("status")));
        return m;
    }

    private void requireStaff(long id) {
        Long n = jdbc.queryForObject("""
                SELECT COUNT_BIG(*) FROM dbo.nhan_vien WHERE id_nhan_vien=:id AND trang_thai=1
                """,idParam(id),Long.class);
        if(n==null||n!=1) throw new IllegalArgumentException("Hay chon nhan vien dang hoat dong.");
    }

    private void audit(long order,long staff,String event,String from,String to,String message) {
        jdbc.update("""
                INSERT dbo.lich_su_don_hang(id_don_hang,id_nhan_vien,su_kien,
                    trang_thai_cu,trang_thai_moi,ghi_chu)
                VALUES(:id,:staff,:event,:from,:to,:note)
                """,idParam(order).addValue("staff",staff).addValue("event",event)
                   .addValue("from",from).addValue("to",to).addValue("note",message));
    }

    private String validateReference(String raw,boolean required) {
        String s=raw==null?"":raw.trim();
        if(required && s.isBlank()) throw new IllegalArgumentException("Chuyen khoan/hoan tien can ma doi soat thuc te.");
        if(s.length()>120 || !s.matches("[\\p{L}\\p{N}_./ :#-]*"))
            throw new IllegalArgumentException("Ma doi soat khong hop le (toi da 120 ky tu).");
        return s;
    }

    private static MapSqlParameterSource idParam(long id) {
        return new MapSqlParameterSource("id",id);
    }
    private static void onlineOnly(Map<String,Object> order) {
        if(!"ONLINE".equals(order.get("channel")))
            throw new IllegalArgumentException("Chi duoc thao tac nghiep vu online tren man hinh nay.");
    }
}
