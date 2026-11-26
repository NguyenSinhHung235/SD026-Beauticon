package com.example.vongtay.catalog;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VariantRepository extends JpaRepository<ProductVariant, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM ProductVariant v WHERE v.idBienThe = :id")
    java.util.Optional<ProductVariant> findForUpdate(@Param("id") Long id);

    List<ProductVariant> findByIdSanPhamOrderByIdBienTheAsc(Long productId);

    java.util.Optional<ProductVariant> findByIdSanPhamAndIdMauSacAndIdKichThuoc(
            Long idSanPham, Long idMauSac, Long idKichThuoc);

    @Query("""
        SELECT v.idSanPham, COUNT(v), COALESCE(SUM(v.soLuongTon), 0)
        FROM ProductVariant v WHERE v.idSanPham IN :ids GROUP BY v.idSanPham
        """)
    List<Object[]> summarize(@Param("ids") Collection<Long> ids);
}
