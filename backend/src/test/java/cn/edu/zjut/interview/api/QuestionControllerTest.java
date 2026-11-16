package cn.edu.zjut.interview.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * issue #1 的「题库」验收：两个岗位各 ≥20 题、按题型过滤、缺参数与非法参数有明确 400。
 *
 * <p>用 {@code limit=100} 取全量再断言条数：默认 limit 是 20，直接断言 20 会在
 * 「正好 20 题」和「题被截断」两种情况下都通过，测不出问题。
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /** issue 原文：至少两种岗位的题库。种子是 java / frontend 各 20 题。 */
    @Test
    void Java岗位至少二十题() throws Exception {
        mockMvc.perform(get("/api/questions").param("jobCode", "java").param("limit", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(20)))
                .andExpect(jsonPath("$[0].jobCode").value("java"))
                .andExpect(jsonPath("$[0].typeLabel").value("技术知识"))
                .andExpect(jsonPath("$[0].text").isNotEmpty())
                .andExpect(jsonPath("$[0].followUps").isArray())
                .andExpect(jsonPath("$[0].followUps", not(empty())));
    }

    @Test
    void 前端岗位至少二十题() throws Exception {
        mockMvc.perform(get("/api/questions").param("jobCode", "frontend").param("limit", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(20)))
                .andExpect(jsonPath("$[0].jobCode").value("frontend"));
    }

    /** 评论给的验收示例就是 {@code ?jobCode=java-backend&type=technical}，英文 code 必须认。 */
    @Test
    void 按英文题型过滤() throws Exception {
        mockMvc.perform(get("/api/questions")
                        .param("jobCode", "java")
                        .param("type", "technical")
                        .param("limit", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[*].typeCode").value(everyItem(is("technical"))));
    }

    /** 手输中文题型也认，免得前端同学按界面文案传参就 400。 */
    @Test
    void 按中文题型过滤() throws Exception {
        mockMvc.perform(get("/api/questions")
                        .param("jobCode", "java")
                        .param("type", "场景题")
                        .param("limit", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[*].typeLabel").value(everyItem(is("场景题"))));
    }

    /** 缺 jobCode 是调用方错误，返回 400 而不是「整库返回」。 */
    @Test
    void 缺jobCode返回400() throws Exception {
        mockMvc.perform(get("/api/questions"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 未知题型返回400() throws Exception {
        mockMvc.perform(get("/api/questions").param("jobCode", "java").param("type", "不存在的题型"))
                .andExpect(status().isBadRequest());
    }

    /** 没录题的岗位是「空数组」而不是 404：接口存在，只是这个岗位还没有题。 */
    @Test
    void 没有题库的岗位返回空数组() throws Exception {
        mockMvc.perform(get("/api/questions").param("jobCode", "no-such-job"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    /** limit 必须真的截断，否则前端「只要 5 道」的请求会白拿 20 道。 */
    @Test
    void limit生效() throws Exception {
        mockMvc.perform(get("/api/questions").param("jobCode", "java").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }
}
