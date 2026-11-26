package com.example.vongtay.people;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class CustomerAddressService {
    private final NamedParameterJdbcTemplate jdbc;
    private final CustomerService customers;
    public CustomerAddressService(NamedParameterJdbcTemplate jdbc,CustomerService customers) {
        this.jdbc=jdbc;this.customers=customers;
    }
    public List<Map<String,Object>> list(long customerId) {
        customers.get(customerId);
        return jdbc.queryForList("""
                SELECT id_dia_chi AS id,ten_nguoi_nhan AS recipient,sdt AS phone,
                       dia_chi_chi_tiet AS detail,mac_dinh AS isDefault,trang_thai AS active
                FROM dbo.dia_chi_giao_hang WHERE id_khach_hang=:customerId
                ORDER BY mac_dinh DESC,trang_thai DESC,id_dia_chi DESC
                """,new MapSqlParameterSource("customerId",customerId));
    }
    public Map<String,Object> get(long customerId,long addressId) {
        List<Map<String,Object>> rows=jdbc.queryForList("""
                SELECT id_dia_chi AS id,ten_nguoi_nhan AS recipient,sdt AS phone,
                       dia_chi_chi_tiet AS detail,mac_dinh AS isDefault,trang_thai AS active
                FROM dbo.dia_chi_giao_hang WHERE id_khach_hang=:customerId AND id_dia_chi=:id
                """,new MapSqlParameterSource("customerId",customerId).addValue("id",addressId));
        if(rows.isEmpty()) throw new IllegalArgumentException("Khong tim thay dia chi thuoc khach hang.");
        return rows.get(0);
    }
    public AddressForm form(long customerId,long addressId) {
        Map<String,Object> r=get(customerId,addressId);
        AddressForm f=new AddressForm();
        f.setTenNguoiNhan((String)r.get("recipient"));f.setSdt((String)r.get("phone"));
        f.setDiaChiChiTiet((String)r.get("detail"));f.setMacDinh(Boolean.TRUE.equals(r.get("isDefault")));
        return f;
    }
    @Transactional
    public void save(long customerId,Long addressId,AddressForm f) {
        Map<String,Object> customer=customers.get(customerId);
        if(!Boolean.TRUE.equals(customer.get("active")))
            throw new IllegalArgumentException("Khach hang dang bi khoa.");
        String recipient=PeopleValidation.name(f.getTenNguoiNhan(),"Ten nguoi nhan");
        String phone=PeopleValidation.phone(f.getSdt(),true);
        String detail=PeopleValidation.detail(f.getDiaChiChiTiet());
        if(addressId!=null && !Boolean.TRUE.equals(get(customerId,addressId).get("active")))
            throw new IllegalArgumentException("Dia chi da an. Can khoi phuc truoc khi sua.");
        // Serialize changes to the default address for this customer, protecting the filtered unique index.
        jdbc.queryForList("SELECT id_khach_hang FROM dbo.khach_hang WITH (UPDLOCK,HOLDLOCK) WHERE id_khach_hang=:id",
                new MapSqlParameterSource("id",customerId));
        MapSqlParameterSource p=new MapSqlParameterSource("customerId",customerId)
                .addValue("recipient",recipient).addValue("phone",phone)
                .addValue("detail",detail).addValue("default",f.isMacDinh());
        if(f.isMacDinh()) jdbc.update("""
                UPDATE dbo.dia_chi_giao_hang SET mac_dinh=0
                WHERE id_khach_hang=:customerId AND mac_dinh=1
                """,p);
        if(addressId==null) jdbc.update("""
                INSERT INTO dbo.dia_chi_giao_hang(id_khach_hang,ten_nguoi_nhan,sdt,dia_chi_chi_tiet,mac_dinh,trang_thai)
                VALUES(:customerId,:recipient,:phone,:detail,:default,1)
                """,p);
        else {
            p.addValue("id",addressId);
            jdbc.update("""
                    UPDATE dbo.dia_chi_giao_hang
                    SET ten_nguoi_nhan=:recipient,sdt=:phone,dia_chi_chi_tiet=:detail,mac_dinh=:default
                    WHERE id_dia_chi=:id AND id_khach_hang=:customerId
                    """,p);
        }
    }
    @Transactional
    public void setActive(long customerId,long addressId,boolean active) {
        get(customerId,addressId);
        // Never hard delete: historical order references retain their foreign key.
        jdbc.update("""
                UPDATE dbo.dia_chi_giao_hang
                SET trang_thai=:active,mac_dinh=CASE WHEN :active=0 THEN 0 ELSE mac_dinh END
                WHERE id_dia_chi=:id AND id_khach_hang=:customerId
                """,new MapSqlParameterSource("active",active).addValue("id",addressId)
                .addValue("customerId",customerId));
    }
}
