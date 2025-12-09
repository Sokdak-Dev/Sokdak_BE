package ssu.sokdak.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ssu.sokdak.user.domain.UserCategorySelection;

import java.util.List;

public interface UserCategorySelectionRepository extends JpaRepository<UserCategorySelection, Long> {

    @Query("""
       select ucs.user.id as userId, ucs.option.label as label
       from UserCategorySelection ucs
       where ucs.category.id = :categoryId
         and ucs.user.id in :userIds
       """)
    List<UserOptionView> findOptionLabelsByUsersAndCategory(@Param("userIds") List<Long> userIds,
                                                            @Param("categoryId") Long categoryId);

    @Query("""
        select ucs from UserCategorySelection ucs
        join fetch ucs.category
        join fetch ucs.option
        where ucs.user.id = :userId
        order by ucs.category.id
        """)
    List<UserCategorySelection> findByUserIdWithCategoryAndOption(@Param("userId") Long userId);

    interface UserOptionView {
        Long getUserId();
        String getLabel();
    }
}