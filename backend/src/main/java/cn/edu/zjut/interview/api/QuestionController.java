package cn.edu.zjut.interview.api;

import cn.edu.zjut.interview.domain.InterviewQuestion;
import cn.edu.zjut.interview.dto.QuestionView;
import cn.edu.zjut.interview.question.QuestionType;
import cn.edu.zjut.interview.repository.InterviewQuestionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

/**
 * 岗位题库只读接口（issue #1 的「题库」部分）。
 *
 * <p>题目原本只写在前端 {@code mock.ts}，后端看不到也加不了；这个接口把题库变成可维护的数据，
 * 前端面试房间改为从这里取题（取不到时仍回退本地题库，保证断网还能演示）。
 *
 * <p>错误语义：{@code jobCode} 缺失或空白 → 400；{@code type} 不是四种题型之一 → 400（中文提示）。
 * 岗位没有题时返回空数组而不是 404 —— 「这个岗位还没录题」是正常状态，不是接口不存在。
 */
@RestController
@RequestMapping("/api")
public class QuestionController {

    /** 不传 limit 时给 20 道：issue #1 的验收要求就是「每个岗位 ≥20 题」。 */
    private static final int DEFAULT_LIMIT = 20;
    /** 上限防止前端一次把整库拉走；题量真的过千时再改成游标分页。 */
    private static final int MAX_LIMIT = 100;

    private static final Logger log = LoggerFactory.getLogger(QuestionController.class);

    private final InterviewQuestionRepository repository;
    private final ObjectMapper objectMapper;

    public QuestionController(InterviewQuestionRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    /**
     * 按岗位取题。
     *
     * @param jobCode 岗位编码（java / frontend），必填
     * @param type    题型，可选；英文 code 或中文标签都认
     * @param limit   返回条数，默认 20、最多 100
     */
    @GetMapping("/questions")
    public List<QuestionView> list(
            @RequestParam(name = "jobCode", required = false) String jobCode,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "limit", required = false) Integer limit) {

        if (jobCode == null || jobCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "jobCode 不能为空（例：java）");
        }

        QuestionType questionType;
        try {
            questionType = QuestionType.parse(type);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        String code = jobCode.trim();
        List<InterviewQuestion> rows = questionType == null
                ? repository.findByJobCodeAndEnabledTrueOrderBySortOrderAscIdAsc(code)
                : repository.findByJobCodeAndTypeCodeAndEnabledTrueOrderBySortOrderAscIdAsc(
                        code, questionType.code());

        int size = Math.min(limit == null || limit <= 0 ? DEFAULT_LIMIT : limit, MAX_LIMIT);
        List<QuestionView> views = new ArrayList<>(Math.min(rows.size(), size));
        for (InterviewQuestion row : rows.subList(0, Math.min(rows.size(), size))) {
            views.add(QuestionView.of(row, parseFollowUps(row)));
        }
        return views;
    }

    /** follow_ups 是 JSON 数组字符串；脏数据只影响这一条，不让整个列表 500。 */
    private List<String> parseFollowUps(InterviewQuestion question) {
        String json = question.getFollowUps();
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            log.warn("[Question] code={} 的 follow_ups 不是合法 JSON 数组，按空列表返回：{}",
                    question.getCode(), e.getMessage());
            return List.of();
        }
    }
}
