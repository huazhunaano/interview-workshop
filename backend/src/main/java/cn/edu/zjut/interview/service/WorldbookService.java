package cn.edu.zjut.interview.service;

import cn.edu.zjut.interview.domain.WorldbookBook;
import cn.edu.zjut.interview.domain.WorldbookEntry;
import cn.edu.zjut.interview.dto.WorldbookDetailView;
import cn.edu.zjut.interview.dto.WorldbookEntryView;
import cn.edu.zjut.interview.dto.WorldbookView;
import cn.edu.zjut.interview.repository.WorldbookBookRepository;
import cn.edu.zjut.interview.repository.WorldbookEntryRepository;
import cn.edu.zjut.interview.worldbook.Worldbook;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 世界书 / 岗位知识库的入库与查询（issue #1）。
 *
 * <p><b>入库</b>：应用启动后扫描 {@code game_data/worldbooks/*.yml}，文件名即书的 code，
 * 一本书一个文件。导入是幂等的：按 {@code entryId} 对账——YAML 里有的更新、缺的删掉，
 * 所以「改文件 → 重启」就能把书改到最新，不需要手工清库。
 *
 * <p><b>为什么还要入库</b>：注入用的是 {@code interviewer.worldbook} 里那份 YAML 全文，
 * 不依赖这两张表；但只有落库之后，{@code GET /api/worldbooks} 才能把「有哪些书、每本多少条」
 * 说清楚，也才便于排查「书加载了但没注入」这类问题。
 *
 * <p>解析复用 {@link Worldbook#parse}，保证与注入链路对同一份文件的格式理解完全一致。
 */
@Service
public class WorldbookService {

    private static final Logger log = LoggerFactory.getLogger(WorldbookService.class);

    /** 扫描目录不存在或某本书格式错误时跳过并 warn，不阻断启动。 */
    private final WorldbookBookRepository bookRepository;
    private final WorldbookEntryRepository entryRepository;
    private final ObjectMapper objectMapper;
    private final Path root;

    public WorldbookService(WorldbookBookRepository bookRepository,
                            WorldbookEntryRepository entryRepository,
                            ObjectMapper objectMapper,
                            @Value("${app.worldbooks-dir:game_data/worldbooks}") String worldbooksDir) {
        this.bookRepository = bookRepository;
        this.entryRepository = entryRepository;
        this.objectMapper = objectMapper;
        this.root = resolveRoot(worldbooksDir);
    }

    // ============================================================
    // 入库
    // ============================================================

    /** 启动后导入一次；失败只影响「列表接口有没有书」，不影响面试主流程。 */
    @EventListener(ApplicationReadyEvent.class)
    public void importOnStartup() {
        importFromDisk();
    }

    /**
     * 扫描目录并逐本导入。
     *
     * @return 成功导入的书数
     */
    public int importFromDisk() {
        if (!Files.isDirectory(root)) {
            log.warn("[Worldbook] 知识库目录不存在，跳过导入：{}", root);
            return 0;
        }
        List<Path> files;
        try (Stream<Path> children = Files.list(root)) {
            files = children
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".yml"))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            log.warn("[Worldbook] 读取知识库目录失败 {}：{}", root, e.getMessage());
            return 0;
        }
        int imported = 0;
        for (Path file : files) {
            try {
                importBook(file);
                imported++;
            } catch (Exception e) {
                log.warn("[Worldbook] 跳过 {}：{}", file.getFileName(), e.toString());
            }
        }
        log.info("[Worldbook] 知识库导入完成：{} 本 / 目录 {}", imported, root);
        return imported;
    }

    /** 单本书：先解析，再按 entryId 对账写入。 */
    private void importBook(Path file) throws IOException {
        String fileName = file.getFileName().toString();
        String code = fileName.substring(0, fileName.length() - ".yml".length());
        String text = Files.readString(file, StandardCharsets.UTF_8);

        Worldbook parsed = Worldbook.parse(text);
        if (parsed == null) {
            throw new IllegalArgumentException("YAML 解析失败或 entries 为空");
        }
        Map<?, ?> meta = readMeta(text);

        WorldbookBook book = bookRepository.findByCode(code).orElseGet(WorldbookBook::new);
        book.setCode(code);
        book.setName(firstNonBlank(parsed.name(), optionalString(meta.get("name")), code));
        book.setJobCode(firstNonBlank(optionalString(meta.get("job_code")), code));
        book.setDescription(truncate(optionalString(meta.get("description")), 512));
        book.setSourcePath(root.relativize(file).toString().replace('\\', '/'));
        book.setUpdatedAt(LocalDateTime.now());
        book = bookRepository.save(book);

        List<WorldbookEntry> existing = entryRepository.findByBookIdOrderBySortOrderAscIdAsc(book.getId());
        Map<String, WorldbookEntry> byEntryId = new LinkedHashMap<>();
        for (WorldbookEntry entry : existing) {
            byEntryId.put(entry.getEntryId(), entry);
        }

        List<WorldbookEntry> toSave = new ArrayList<>();
        int order = 0;
        int created = 0;
        for (Worldbook.Entry source : parsed.entries()) {
            WorldbookEntry entry = byEntryId.remove(source.id());
            if (entry == null) {
                entry = new WorldbookEntry();
                entry.setBookId(book.getId());
                entry.setEntryId(source.id());
                created++;
            }
            entry.setName(source.name());
            entry.setEnabled(source.enabled());
            entry.setPriority(source.priority());
            entry.setActivationType(source.activation().isAlways() ? "always" : "keyword");
            entry.setEntryKeys(toJson(source.activation().keys()));
            entry.setContent(source.content());
            entry.setSortOrder(order++);
            toSave.add(entry);
        }
        entryRepository.saveAll(toSave);
        // YAML 里删掉的条目，库里同步删掉，保证「重启后与文件一致」
        if (!byEntryId.isEmpty()) {
            entryRepository.deleteAll(byEntryId.values());
        }
        log.info("[Worldbook] 导入 {}：共 {} 条（新增 {} 条，删除 {} 条）",
                code, toSave.size(), created, byEntryId.size());
    }

    /** 只读顶层 meta，条目解析仍交给 {@link Worldbook#parse}，避免两套格式理解。 */
    private static Map<?, ?> readMeta(String text) {
        Object loaded = new Yaml().load(text);
        return loaded instanceof Map<?, ?> map ? map : Map.of();
    }

    // ============================================================
    // 查询
    // ============================================================

    public List<WorldbookView> list() {
        return bookRepository.findAllByOrderByCodeAsc().stream()
                .map(book -> WorldbookView.of(book, entryRepository.countByBookIdAndEnabledTrue(book.getId())))
                .toList();
    }

    public WorldbookDetailView detail(String code) {
        WorldbookBook book = bookRepository.findByCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "没有这本书：" + code));
        List<WorldbookEntryView> entries = entryRepository.findByBookIdOrderBySortOrderAscIdAsc(book.getId())
                .stream()
                .map(entry -> new WorldbookEntryView(
                        entry.getEntryId(),
                        entry.getName(),
                        Boolean.TRUE.equals(entry.getEnabled()),
                        Optional.ofNullable(entry.getPriority()).orElse(0),
                        entry.getActivationType(),
                        fromJson(entry.getEntryKeys()),
                        entry.getContent()))
                .toList();
        return new WorldbookDetailView(
                book.getCode(),
                book.getName(),
                book.getJobCode(),
                book.getDescription(),
                entryRepository.countByBookIdAndEnabledTrue(book.getId()),
                entries);
    }

    // ============================================================
    // 小工具
    // ============================================================

    /** 关键词数组存 JSON 字符串，和 {@code interview_question.follow_ups} 保持一致。 */
    private String toJson(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(keys);
        } catch (Exception e) {
            log.warn("[Worldbook] 关键词序列化失败，按空处理：{}", e.getMessage());
            return null;
        }
    }

    private List<String> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            log.warn("[Worldbook] 关键词不是合法 JSON 数组，按空处理：{}", e.getMessage());
            return List.of();
        }
    }

    private static String optionalString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static String firstNonBlank(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate;
            }
        }
        return null;
    }

    private static String truncate(String text, int max) {
        if (text == null || text.length() <= max) {
            return text;
        }
        return text.substring(0, max);
    }

    /**
     * 目录解析与 {@link CharacterCardService} 同一策略：先按配置的路径，
     * 找不到再试上一级（后端 jar 与单测都在 backend/ 下启动）。
     */
    private static Path resolveRoot(String worldbooksDir) {
        Path direct = Path.of(worldbooksDir).toAbsolutePath().normalize();
        if (Files.isDirectory(direct)) {
            return direct;
        }
        Path fromParent = Path.of("..").resolve(worldbooksDir).toAbsolutePath().normalize();
        if (Files.isDirectory(fromParent)) {
            return fromParent;
        }
        log.warn("[Worldbook] 知识库目录 {} 不存在（也未在上一级找到），按字面量使用", direct);
        return direct;
    }
}
