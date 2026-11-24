package cn.edu.zjut.interview.api;

import cn.edu.zjut.interview.service.WorldbookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * issue #1 的知识库验收：{@code GET /api/worldbooks} 能列出两个岗位的书，
 * 每本不少于 100 条，未知 code 返回 404，重复导入条目数不变。
 *
 * <p>数据来自仓库里的 {@code game_data/worldbooks/*.yml}，与世界书注入链路共用同一份文件。
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class WorldbookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WorldbookService worldbookService;

    /** issue 原文：访问 /api/worldbooks 能列出 2 本书。 */
    @Test
    void 列出两个岗位的知识库() throws Exception {
        mockMvc.perform(get("/api/worldbooks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].code", hasItems("java", "frontend")))
                .andExpect(jsonPath("$[*].jobCode", hasItems("java", "frontend")));
    }

    /** 每个岗位不少于 100 条（issue 第三步备料）。 */
    @Test
    void 每本书不少于一百条() throws Exception {
        for (String code : new String[]{"java", "frontend"}) {
            mockMvc.perform(get("/api/worldbooks/" + code))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.entryCount").value(greaterThanOrEqualTo(100)))
                    .andExpect(jsonPath("$.entries", hasSize(greaterThanOrEqualTo(100))))
                    .andExpect(jsonPath("$.entries[0].activationType").isNotEmpty())
                    .andExpect(jsonPath("$.entries[0].keys").isArray())
                    .andExpect(jsonPath("$.entries[0].content").isNotEmpty());
        }
    }

    @Test
    void 未知知识库返回404() throws Exception {
        mockMvc.perform(get("/api/worldbooks/no-such-book"))
                .andExpect(status().isNotFound());
    }

    /** 重启（重复导入）不应把条目越堆越多。 */
    @Test
    void 重复导入条目数不变() throws Exception {
        worldbookService.importFromDisk();
        worldbookService.importFromDisk();
        mockMvc.perform(get("/api/worldbooks/java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entryCount").value(100));
    }
}
