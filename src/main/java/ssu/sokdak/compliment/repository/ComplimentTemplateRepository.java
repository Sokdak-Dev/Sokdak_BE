package ssu.sokdak.compliment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ssu.sokdak.compliment.domain.ComplimentTemplate;

public interface ComplimentTemplateRepository extends JpaRepository<ComplimentTemplate, Long> {
    // 템플릿 검색/필터 등만 여기에 둡니다 (예: findByCategory_IdAndActiveTrue ...)
}
