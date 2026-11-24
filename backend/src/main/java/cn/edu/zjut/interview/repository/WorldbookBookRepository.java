package cn.edu.zjut.interview.repository;

import cn.edu.zjut.interview.domain.WorldbookBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** {@code worldbook_book}：岗位知识库的「书」列表。 */
public interface WorldbookBookRepository extends JpaRepository<WorldbookBook, Long> {

    Optional<WorldbookBook> findByCode(String code);

    /** 按 code 排序，列表接口顺序稳定。 */
    List<WorldbookBook> findAllByOrderByCodeAsc();
}
