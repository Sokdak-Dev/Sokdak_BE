package ssu.sokdak.user.service;

import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssu.sokdak.category.domain.Category;
import ssu.sokdak.category.domain.CategoryOption;
import ssu.sokdak.category.repository.CategoryOptionRepository;
import ssu.sokdak.category.repository.CategoryRepository;
import ssu.sokdak.user.domain.User;
import ssu.sokdak.user.domain.UserCategorySelection;
import ssu.sokdak.user.dto.MemberDtos;
import ssu.sokdak.user.repository.UserCategorySelectionRepository;
import ssu.sokdak.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryOptionRepository categoryOptionRepository;
    private final UserCategorySelectionRepository userCategorySelectionRepository;

    @Transactional
    public User register(MemberDtos.RegisterReq req){
        if (userRepository.existsByEmail(req.email()))
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        String hash = BCrypt.hashpw(req.password(), BCrypt.gensalt(12));
        User user = User.builder()
                .email(req.email())
                .passwordHash(hash)
                .name(req.name())
                .nickname(req.nickname())
                .avatarUrl(req.avatarUrl())
                .status("active")
                .build();
        User saved = userRepository.save(user);

        persistCategorySelections(saved, req.selections());
        return saved;
    }

    private void persistCategorySelections(User user, List<MemberDtos.CategorySelectionReq> selections) {
        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            throw new IllegalStateException("카테고리가 설정되지 않았습니다.");
        }
        if (selections == null || selections.size() != categories.size()) {
            throw new IllegalArgumentException("모든 카테고리에 대한 선택을 제출해야 합니다.");
        }

        var categoryByCode = categories.stream()
                .collect(Collectors.toMap(Category::getCode, c -> c));

        List<UserCategorySelection> entities = new ArrayList<>();
        var seenCodes = new java.util.HashSet<String>();
        for (MemberDtos.CategorySelectionReq selection : selections) {
            if (!seenCodes.add(selection.categoryCode())) {
                throw new IllegalArgumentException("카테고리 중복 선택이 감지되었습니다: " + selection.categoryCode());
            }
            Category category = categoryByCode.get(selection.categoryCode());
            if (category == null) {
                throw new IllegalArgumentException("존재하지 않는 카테고리입니다: " + selection.categoryCode());
            }
            CategoryOption option = categoryOptionRepository.findByCategoryAndLabel(category, selection.optionLabel())
                    .orElseThrow(() -> new IllegalArgumentException("카테고리에 해당 옵션이 없습니다: " + selection.optionLabel()));

            UserCategorySelection entity = UserCategorySelection.builder()
                    .user(user)
                    .category(category)
                    .option(option)
                    .rank(selection.rank())
                    .selectedAt(LocalDateTime.now())
                    .build();
            entities.add(entity);
        }

        userCategorySelectionRepository.saveAll(entities);
    }

    public List<MemberDtos.CategorySelectionRes> getCategorySelections(Long userId) {
        get(userId); // 존재 여부 확인
        return userCategorySelectionRepository.findByUserIdWithCategoryAndOption(userId).stream()
                .map(MemberDtos.CategorySelectionRes::from)
                .toList();
    }

    public User login(MemberDtos.LoginReq req){
        User u = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));
        if (!BCrypt.checkpw(req.password(), u.getPasswordHash()))
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        if (!"active".equals(u.getStatus()))
            throw new IllegalStateException("비활성화된 계정입니다.");
        return u;
    }

    public User get(Long id){
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
    }

    @Transactional
    public User update(Long id, MemberDtos.UpdateReq req){
        User u = get(id);
        User updated = User.builder()
                .id(u.getId())
                .email(u.getEmail())
                .passwordHash(u.getPasswordHash())
                .name(req.name()!=null ? req.name() : u.getName())
                .nickname(req.nickname()!=null ? req.nickname() : u.getNickname())
                .avatarUrl(req.avatarUrl()!=null ? req.avatarUrl() : u.getAvatarUrl())
                .status(u.getStatus())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
        return userRepository.save(updated);
    }

    @Transactional
    public void deactivate(Long id){
        User u = get(id);
        User deactivated = User.builder()
                .id(u.getId())
                .email(u.getEmail())
                .passwordHash(u.getPasswordHash())
                .name(u.getName())
                .nickname(u.getNickname())
                .avatarUrl(u.getAvatarUrl())
                .status("deactivated")
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
        userRepository.save(deactivated);
    }
}
