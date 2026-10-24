package com.example.vongtay.shop;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShopAddressService {
    private final NamedParameterJdbcTemplate jdbc;
    public ShopAddressService(NamedParameterJdbcTemplate jdbc) { this.jdbc=jdbc; }

    public record Address(long id,String recipient,String phone,String text,boolean isDefault) {}

    public List<Address> list(long customerId) {
        return jdbc.query("""
                SELECT id_dia_chi,ten_nguoi_nhan,sdt,dia_chi_chi_tiet,mac_dinh
                FROM dbo.dia_chi_giao_hang
                WHERE id_khach_hang=:customer AND trang_thai=1
                ORDER BY mac_dinh DESC,id_dia_chi DESC
                """,new MapSqlParameterSource("customer",customerId), (rs,i)->new Address(
                rs.getLong("id_dia_chi"),rs.getString("ten_nguoi_nhan"),rs.getString("sdt"),
                rs.getString("dia_chi_chi_tiet"),rs.getBoolean("mac_dinh")));
    }

    public static String addressText(String raw) {
        String address=raw==null?"":raw.trim().replaceAll("\\s+"," ");
        if(address.length()<10 || address.length()>500)
            throw new IllegalArgumentException("Địa chỉ giao hàng cần từ 10 đến 500 ký tự.");
        return address;
    }

    @Transactional
    public long add(long customerId,String name,String phone,String address,boolean requestedDefault) {
        String cleanName=ShopAuthService.fullName(name);
        String cleanPhone=ShopAuthService.phone(phone,true);
        String cleanAddress=addressText(address);
        lockCustomer(customerId);
        Long count=jdbc.queryForObject("""
                SELECT COUNT_BIG(*) FROM dbo.dia_chi_giao_hang
                WHERE id_khach_hang=:customer AND trang_thai=1
                """,new MapSqlParameterSource("customer",customerId),Long.class);
        if(count!=null && count>=10)
            throw new IllegalArgumentException("Moi tai khoan chi luu toi da 10 dia chi.");
        boolean isDefault=requestedDefault || count==null || count==0;
        if(isDefault) jdbc.update("""
                UPDATE dbo.dia_chi_giao_hang SET mac_dinh=0 WHERE id_khach_hang=:customer
                """,new MapSqlParameterSource("customer",customerId));
        Long created=jdbc.queryForObject("""
                INSERT INTO dbo.dia_chi_giao_hang(id_khach_hang,ten_nguoi_nhan,sdt,dia_chi_chi_tiet,mac_dinh,trang_thai)
                OUTPUT INSERTED.id_dia_chi VALUES(:customer,:name,:phone,:address,:isDefault,1)
                """,new MapSqlParameterSource("customer",customerId).addValue("name",cleanName)
                .addValue("phone",cleanPhone).addValue("address",cleanAddress)
                .addValue("isDefault",isDefault),Long.class);
        if(created==null) throw new IllegalStateException("Khong luu duoc dia chi.");
        return created;
    }

    @Transactional
    public void remove(long customerId,long addressId) {
        lockCustomer(customerId);
        MapSqlParameterSource p=new MapSqlParameterSource("customer",customerId).addValue("id",addressId);
        List<Map<String,Object>> current=jdbc.queryForList("""
                SELECT mac_dinh AS isDefault FROM dbo.dia_chi_giao_hang WITH (UPDLOCK,HOLDLOCK)
                WHERE id_dia_chi=:id AND id_khach_hang=:customer AND trang_thai=1
                """,p);
        if(current.isEmpty()) throw new IllegalArgumentException("Địa chỉ không tồn tại.");
        jdbc.update("""
                UPDATE dbo.dia_chi_giao_hang SET trang_thai=0,mac_dinh=0
                WHERE id_dia_chi=:id AND id_khach_hang=:customer
                """,p);
        if(Boolean.TRUE.equals(current.get(0).get("isDefault"))) {
            jdbc.update("""
                    UPDATE dbo.dia_chi_giao_hang SET mac_dinh=1
                    WHERE id_dia_chi=(SELECT TOP(1) id_dia_chi FROM dbo.dia_chi_giao_hang
                                       WHERE id_khach_hang=:customer AND trang_thai=1
                                       ORDER BY id_dia_chi DESC)
                    """,p);
        }
    }

    public Address belongingTo(long customerId,long addressId) {
        List<Address> rows=jdbc.query("""
                SELECT id_dia_chi,ten_nguoi_nhan,sdt,dia_chi_chi_tiet,mac_dinh
                FROM dbo.dia_chi_giao_hang WITH (UPDLOCK,HOLDLOCK)
                WHERE id_dia_chi=:id AND id_khach_hang=:customer AND trang_thai=1
                """,new MapSqlParameterSource("id",addressId).addValue("customer",customerId),
                (rs,i)->new Address(rs.getLong("id_dia_chi"),rs.getString("ten_nguoi_nhan"),
                    rs.getString("sdt"),rs.getString("dia_chi_chi_tiet"),rs.getBoolean("mac_dinh")));
        if(rows.isEmpty()) throw new IllegalArgumentException("Địa chỉ không thuộc tài khoản của bạn.");
        return rows.get(0);
    }

    private void lockCustomer(long id) {
        List<Long> locked=jdbc.queryForList("""
                SELECT id_khach_hang FROM dbo.khach_hang WITH (UPDLOCK,HOLDLOCK)
                WHERE id_khach_hang=:id AND trang_thai=1
                """,new MapSqlParameterSource("id",id),Long.class);
        if(locked.isEmpty())throw new IllegalArgumentException("Tai khoan khong hop le.");
    }
}
