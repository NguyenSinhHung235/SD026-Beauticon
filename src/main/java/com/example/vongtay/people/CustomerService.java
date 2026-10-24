package com.example.vongtay.people;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class CustomerService {
    public record Listing(List<Map<String,Object>> rows,long count,int page,int pages,String query) {}
    private final NamedParameterJdbcTemplate jdbc;
    public CustomerService(NamedParameterJdbcTemplate jdbc) { this.jdbc=jdbc; }
    public Listing list(String search,Integer requestedPage) {
        String q=search==null?"":search.strip();
        if(q.length()>80) throw new IllegalArgumentException("Tim kiem toi da 80 ky tu.");
        String like="%"+q.replace("[","[[]").replace("%","[%]").replace("_","[_]")+"%";
        MapSqlParameterSource p=new MapSqlParameterSource("q",q).addValue("like",like);
        String where="""
                FROM dbo.khach_hang k
                WHERE (:q='' OR k.ho_ten LIKE :like OR k.email LIKE :like OR k.sdt LIKE :like)
                """;
        long count=jdbc.queryForObject("SELECT COUNT_BIG(*) "+where,p,Long.class);
        int pages=Math.max(1,(int)Math.min(Integer.MAX_VALUE,(count+14)/15));
        int page=Math.min(Math.max(requestedPage==null?1:requestedPage,1),pages);
        p.addValue("offset",(page-1)*15).addValue("limit",15);
        List<Map<String,Object>> rows=jdbc.queryForList("""
                SELECT k.id_khach_hang AS id,k.ho_ten AS name,k.email,k.sdt AS phone,
                       k.ngay_sinh AS birthday,k.trang_thai AS active,k.ngay_tao AS createdAt,
                       (SELECT COUNT_BIG(*) FROM dbo.don_hang d WHERE d.id_khach_hang=k.id_khach_hang) AS orderCount
                """+where+" ORDER BY k.id_khach_hang DESC OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY",p);
        return new Listing(rows,count,page,pages,q);
    }
    public Map<String,Object> get(long id) {
        List<Map<String,Object>> rows=jdbc.queryForList("""
                SELECT id_khach_hang AS id,ho_ten AS name,email,sdt AS phone,
                       ngay_sinh AS birthday,trang_thai AS active,ngay_tao AS createdAt
                FROM dbo.khach_hang WHERE id_khach_hang=:id
                """,new MapSqlParameterSource("id",id));
        if(rows.isEmpty()) throw new IllegalArgumentException("Khong tim thay khach hang.");
        return rows.get(0);
    }
    public CustomerForm form(long id) {
        Map<String,Object> r=get(id);
        CustomerForm f=new CustomerForm();
        f.setHoTen((String)r.get("name"));f.setEmail((String)r.get("email"));f.setSdt((String)r.get("phone"));
        Object birthday=r.get("birthday");
        if(birthday instanceof LocalDate date) f.setNgaySinh(date);
        else if(birthday instanceof java.sql.Date date) f.setNgaySinh(date.toLocalDate());
        return f;
    }
    @Transactional
    public long save(Long id,CustomerForm f) {
        String name=PeopleValidation.name(f.getHoTen(),"Ho ten");
        String email=PeopleValidation.email(f.getEmail(),false);
        String phone=PeopleValidation.phone(f.getSdt(),false);
        if(email==null && phone==null)
            throw new IllegalArgumentException("Vui long nhap email hoac so dien thoai de lien he.");
        LocalDate birthday=PeopleValidation.birthday(f.getNgaySinh());
        MapSqlParameterSource p=new MapSqlParameterSource("name",name).addValue("email",email)
                .addValue("phone",phone).addValue("birthday",birthday);
        if(id==null) {
            Long created=jdbc.queryForObject("""
                    INSERT INTO dbo.khach_hang(ho_ten,email,sdt,ngay_sinh,trang_thai)
                    OUTPUT INSERTED.id_khach_hang VALUES(:name,:email,:phone,:birthday,1)
                    """,p,Long.class);
            if(created==null) throw new IllegalStateException("Khong tao duoc khach hang.");
            return created;
        }
        get(id);
        p.addValue("id",id);
        jdbc.update("""
                UPDATE dbo.khach_hang
                SET ho_ten=:name,email=:email,sdt=:phone,ngay_sinh=:birthday
                WHERE id_khach_hang=:id
                """,p);
        return id;
    }
    @Transactional
    public void setActive(long id,boolean active) {
        get(id);
        jdbc.update("UPDATE dbo.khach_hang SET trang_thai=:active WHERE id_khach_hang=:id",
                new MapSqlParameterSource("id",id).addValue("active",active));
    }
    public List<Map<String,Object>> recentOrders(long customerId) {
        get(customerId);
        return jdbc.queryForList("""
                SELECT TOP (15) id_don_hang AS id,kenh_ban AS channel,trang_thai AS status,
                       tong_tien AS total,ngay_dat AS orderedAt
                FROM dbo.don_hang WHERE id_khach_hang=:id ORDER BY ngay_dat DESC,id_don_hang DESC
                """,new MapSqlParameterSource("id",customerId));
    }
}
