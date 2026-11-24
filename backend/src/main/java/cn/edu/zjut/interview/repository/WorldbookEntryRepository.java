package cn.edu.zjut.interview.repository;

import cn.edu.zjut.interview.domain.WorldbookEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** {@code worldbook_entry}：书的条目。导入时按 entryId 对账，多余的删掉。 */
public interface WorldbookEntryRepository extends JpaRepository<WorldbookEntry, Long> {

    List<WorldbookEntry> findByBookIdOrderBySortOrderAscIdAsc(Long bookId);

    long countByBookIdAndEnabledTrue(Long bookId);
}
