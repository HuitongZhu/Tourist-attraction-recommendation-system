package com.travel.travelweb.repo;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.travel.travelweb.entity.Landscape;

public interface LandscapeRepository extends JpaRepository<Landscape, String> {

    List<Landscape> findByAuditStateOrderByPublishTimeDesc(String auditState, Pageable pageable);

    @Query("""
            SELECT l FROM Landscape l
            WHERE l.auditState = :audit
            AND (:kw IS NULL OR :kw = '' OR l.title LIKE CONCAT('%', :kw, '%')
                 OR l.address LIKE CONCAT('%', :kw, '%') OR l.content LIKE CONCAT('%', :kw, '%'))
            AND (:city IS NULL OR :city = '' OR :city = 'all' OR l.address LIKE CONCAT('%', :city, '%'))
            AND (:level IS NULL OR :level = '' OR :level = 'all' OR l.level = :level)
            ORDER BY l.publishTime DESC
            """)
    List<Landscape> searchApproved(
            @Param("audit") String audit,
            @Param("kw") String keyword,
            @Param("city") String city,
            @Param("level") String level);

    @Query("""
            SELECT l FROM Landscape l
            WHERE (:auditState IS NULL OR :auditState = '' OR l.auditState = :auditState)
            AND (:kw IS NULL OR :kw = '' OR l.title LIKE CONCAT('%', :kw, '%')
                 OR l.address LIKE CONCAT('%', :kw, '%'))
            ORDER BY l.publishTime DESC
            """)
    List<Landscape> searchForAdmin(
            @Param("auditState") String auditState,
            @Param("kw") String keyword);

    @Query("""
            SELECT l FROM Landscape l
            WHERE l.userId = :userId
            ORDER BY l.publishTime DESC
            """)
    List<Landscape> findByUserId(@Param("userId") String userId);

    void deleteByUserId(String userId);
    
    @Query("""
            SELECT COUNT(l) FROM Landscape l
            WHERE l.title = :title
            AND l.userId = :userId
            """)
    int countByTitleAndUserId(@Param("title") String title, @Param("userId") String userId);
    
    @Query("""
            SELECT COUNT(l) FROM Landscape l
            WHERE l.title = :title
            """)
    int countByTitle(@Param("title") String title);
    
    @Query("""
            SELECT COUNT(l) FROM Landscape l
            WHERE l.address LIKE CONCAT('%', :address, '%') OR :address LIKE CONCAT('%', l.address, '%')
            """)
    int countByAddressContaining(@Param("address") String address);
    
    @Query("""
            SELECT COUNT(l) FROM Landscape l
            WHERE l.address LIKE CONCAT('%', :address, '%') OR :address LIKE CONCAT('%', l.address, '%')
            AND l.userId = :userId
            """)
    int countByAddressContainingAndUserId(@Param("address") String address, @Param("userId") String userId);
}
