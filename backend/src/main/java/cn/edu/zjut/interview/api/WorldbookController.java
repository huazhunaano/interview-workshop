package cn.edu.zjut.interview.api;

import cn.edu.zjut.interview.dto.WorldbookDetailView;
import cn.edu.zjut.interview.dto.WorldbookView;
import cn.edu.zjut.interview.service.WorldbookService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 岗位知识库（世界书）只读接口，issue #1 的验收项 {@code GET /api/worldbooks}。
 *
 * <p>数据来自启动时扫描的 {@code game_data/worldbooks/*.yml}，注入链路不受影响：
 * 面试时的注入仍走 {@code interviewer.worldbook}，这里只负责「列得出、查得到」。
 *
 * <p>未知 code 返回 404 中文提示；列表为空是正常状态（目录没放书），返回空数组。
 */
@RestController
@RequestMapping("/api")
public class WorldbookController {

    private final WorldbookService service;

    public WorldbookController(WorldbookService service) {
        this.service = service;
    }

    /** 列书：每本带启用中的条目数，前端据此显示「100 条」这类信息。 */
    @GetMapping("/worldbooks")
    public List<WorldbookView> list() {
        return service.list();
    }

    /** 单本详情：含全部条目（停用的也返回，便于排查）。 */
    @GetMapping("/worldbooks/{code}")
    public WorldbookDetailView detail(@PathVariable("code") String code) {
        return service.detail(code);
    }
}
