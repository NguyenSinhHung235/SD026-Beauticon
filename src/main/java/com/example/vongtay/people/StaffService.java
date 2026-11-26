package com.example.vongtay.people;

import com.example.vongtay.security.StaffUserDetailsService;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class StaffService {
    public record Listing(List<Map<String,Object>> rows,long count,int page,int pages,String query) {}
    private final NamedParameterJdbcTemplate jdbc;
    private final PasswordEncoder passwords;
    public StaffService(NamedParameterJdbcTemplate jdbc, PasswordEncoder passwords) {
        this.jdbc=jdbc;this.passwords=passwords;
    }

    public Listing list(String search,Integer requestedPage) {
        String q = search == null ? "" : search.strip();
        if(q.length()>80) throw new IllegalArgumentException("Tim kiem toi da 80 ky tu.");
        String like = "%" + q.replace("[", "[[]").replace("%", "[%]").replace("_", "[_]") + "%";
        MapSqlParameterSource p = new MapSqlParameterSource("q",q).addValue("like",like);
        String where = " FROM dbo.nhan_vien WHERE (:q='' OR ho_ten LIKE :like OR email LIKE :like) ";
        long count=jdbc.queryForObject("SELECT COUNT_BIG(*)"+where,p,Long.class);
        int pages=Math.max(1,(int)Math.min(Integer.MAX_VALUE,(count+14)/15));
        int page=Math.min(Math.max(requestedPage==null?1:requestedPage,1),pages);
        p.addValue("offset",(page-1)*15).addValue("limit",15);
        List<Map<String,Object>> rows=jdbc.queryForList("""
                SELECT id_nhan_vien AS id, ho_ten AS name, email, chuc_danh AS role,
                       trang_thai AS active
                """+where+" ORDER BY id_nhan_vien DESC OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY",p);
        return new Listing(rows,count,page,pages,q);
    }

    public Map<String,Object> get(long id) {
        List<Map<String,Object>> r=jdbc.queryForList("""
                SELECT id_nhan_vien AS id,ho_ten AS name,email,chuc_danh AS role,trang_thai AS active
                FROM dbo.nhan_vien WHERE id_nhan_vien=:id
                """, new MapSqlParameterSource("id",id));
        if(r.isEmpty()) throw new IllegalArgumentException("Khong tim thay nhan vien.");
        return r.get(0);
    }

    public StaffForm form(long id) {
        Map<String,Object> r=get(id);
        StaffForm f=new StaffForm();
        f.setHoTen((String)r.get("name"));
        f.setEmail((String)r.get("email"));
        f.setChucDanh((String)r.get("role"));
        return f;
    }

    @Transactional
    public void save(Long id,StaffForm f,long actorId) {
        String name=PeopleValidation.name(f.getHoTen(),"Ho ten");
        String email=PeopleValidation.email(f.getEmail(),true);
        String role=f.getChucDanh();
        StaffUserDetailsService.roleOf(role); // reject unrecognized roles
        String newPassword=PeopleValidation.password(f.getMatKhau(),id==null);
        if(id==null) {
            jdbc.update("""
                    INSERT INTO dbo.nhan_vien(ho_ten,email,mat_khau_hash,chuc_danh,trang_thai)
                    VALUES(:name,:email,:hash,:role,1)
                    """, new MapSqlParameterSource("name",name).addValue("email",email)
                    .addValue("hash",passwords.encode(newPassword)).addValue("role",role));
            return;
        }
        Map<String,Object> current=locked(id);
        if(id==actorId && !role.equals(current.get("role")))
            throw new IllegalArgumentException("Khong the tu thay doi vai tro cua chinh minh.");
        if ("QUAN_LY".equals(current.get("role")) && !"QUAN_LY".equals(role) &&
                Boolean.TRUE.equals(current.get("active"))) ensureAnotherAdmin();
        MapSqlParameterSource p=new MapSqlParameterSource("id",id).addValue("name",name)
                .addValue("email",email).addValue("role",role);
        jdbc.update("""
                UPDATE dbo.nhan_vien SET ho_ten=:name,email=:email,chuc_danh=:role
                WHERE id_nhan_vien=:id
                """,p);
        if(newPassword!=null) jdbc.update("""
                UPDATE dbo.nhan_vien SET mat_khau_hash=:hash WHERE id_nhan_vien=:id
                """,new MapSqlParameterSource("id",id).addValue("hash",passwords.encode(newPassword)));
    }

    @Transactional
    public void setActive(long id,boolean active,long actorId) {
        Map<String,Object> r=locked(id);
        if(id==actorId && !active) throw new IllegalArgumentException("Khong the tu khoa tai khoan dang dang nhap.");
        if(!active && Boolean.TRUE.equals(r.get("active")) && "QUAN_LY".equals(r.get("role")))
            ensureAnotherAdmin();
        jdbc.update("UPDATE dbo.nhan_vien SET trang_thai=:active WHERE id_nhan_vien=:id",
                new MapSqlParameterSource("active",active).addValue("id",id));
    }

    private Map<String,Object> locked(long id) {
        List<Map<String,Object>> r=jdbc.queryForList("""
                SELECT id_nhan_vien AS id,chuc_danh AS role,trang_thai AS active
                FROM dbo.nhan_vien WITH (UPDLOCK,HOLDLOCK) WHERE id_nhan_vien=:id
                """,new MapSqlParameterSource("id",id));
        if(r.isEmpty()) throw new IllegalArgumentException("Khong tim thay nhan vien.");
        return r.get(0);
    }
    private void ensureAnotherAdmin() {
        Long adminCount=jdbc.queryForObject("""
                SELECT COUNT_BIG(*) FROM dbo.nhan_vien WITH (UPDLOCK,HOLDLOCK)
                WHERE chuc_danh=N'QUAN_LY' AND trang_thai=1
                  AND mat_khau_hash LIKE '$2[aby]$%'
                """,new MapSqlParameterSource(),Long.class);
        if(adminCount==null || adminCount<=1)
            throw new IllegalArgumentException("Phai giu lai it nhat mot quan ly dang hoat dong.");
    }
}
