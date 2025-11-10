package ssu.sokdak.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ssu.sokdak.user.domain.UserCategorySelection;

import java.util.Collection;
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

    public interface UserOptionView {
        Long getUserId();
        String getLabel();
    }

}
