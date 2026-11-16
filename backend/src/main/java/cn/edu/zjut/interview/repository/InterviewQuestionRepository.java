package cn.edu.zjut.interview.repository;

import cn.edu.zjut.interview.domain.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** {@code interview_question}：岗位题库。列表接口只查启用中的题，停用的留给后台维护。 */
public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {

    /** 某岗位的全部题目，按展示顺序返回；顺序稳定，前端每次刷新拿到的题序一致。 */
    List<InterviewQuestion> findByJobCodeAndEnabledTrueOrderBySortOrderAscIdAsc(String jobCode);

    /** 某岗位 + 某题型的题目（?type=technical）。 */
    List<InterviewQuestion> findByJobCodeAndTypeCodeAndEnabledTrueOrderBySortOrderAscIdAsc(
            String jobCode, String typeCode);
}
