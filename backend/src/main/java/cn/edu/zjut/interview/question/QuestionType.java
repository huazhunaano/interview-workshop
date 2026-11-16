package cn.edu.zjut.interview.question;

/**
 * 题库的题型（本项目扩展，课设指标 ①）。
 *
 * <p>落库与接口参数用 {@code code}（英文，稳定、可做过滤参数），展示用 {@code label}（中文）。
 * 之所以做成枚举而不是配置表：题型是固定的四档、前后端都要用，配置化会让前端多一次请求，
 * 收益为零；真需要加题型时改这里 + 前端映射即可。
 */
public enum QuestionType {

    /** 技术知识：概念、原理、源码。 */
    TECHNICAL("technical", "技术知识"),
    /** 项目深挖：从简历/项目出发追问实现细节与取舍。 */
    PROJECT("project", "项目深挖"),
    /** 场景题：给定业务场景做设计或排查。 */
    SCENARIO("scenario", "场景题"),
    /** 行为题：协作、冲突、复盘等软素质。 */
    BEHAVIOR("behavior", "行为题");

    private final String code;
    private final String label;

    QuestionType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    /**
     * 解析接口的 {@code ?type=}：接受英文 code，也接受中文标签（前端同学手输中文时不至于 400）。
     *
     * @return null 表示没传（不加题型过滤）
     * @throws IllegalArgumentException 未知取值，由调用方转成 400 的中文提示
     */
    public static QuestionType parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String text = raw.trim();
        for (QuestionType type : values()) {
            if (type.code.equalsIgnoreCase(text) || type.label.equals(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException(
                "未知的题型：" + text + "（可选 technical / project / scenario / behavior，或对应中文）");
    }

    /** code → 中文标签；库里存了未知 code 时原样返回，避免列表接口因一条脏数据整体 500。 */
    public static String labelOf(String code) {
        if (code != null) {
            for (QuestionType type : values()) {
                if (type.code.equalsIgnoreCase(code.trim())) {
                    return type.label;
                }
            }
        }
        return code;
    }
}
