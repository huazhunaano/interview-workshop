package cn.edu.zjut.interview.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 岗位题库的一道题，对应 DDL {@code interview_question}（本项目扩展，课设指标 ①）。
 *
 * <p>和 {@link KnowledgeDoc} 的分工：知识文档是「资料」，切片段后检索注入，供模型参考；
 * 这里是「提问」，驱动面试流程本身。两者不互相替代，也别把题目塞进知识库语料里。
 *
 * <p>{@code code} 是跨重启稳定的业务标识（如 {@code java-jvm-01}），种子脚本按它 MERGE，
 * 所以加了唯一约束；{@code followUps} 存 JSON 数组字符串，读出时再还原成列表。
 */
@Entity
@Table(name = "interview_question")
public class InterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "code", length = 64, nullable = false, unique = true)
    private String code;

    /** 岗位编码，与 {@code interview_script.job_code}、建场次请求的 jobId 同一套值（java / frontend）。 */
    @Column(name = "job_code", length = 32, nullable = false)
    private String jobCode;

    /** 知识点编码，与前端岗位画像的 knowledgePoints[].id 对齐（jvm / concurrent / vue …）。 */
    @Column(name = "kp_code", length = 32)
    private String kpCode;

    /** technical / project / scenario / behavior，接口的 {@code ?type=} 按它过滤。 */
    @Column(name = "type_code", length = 32, nullable = false)
    private String typeCode;

    /** 中文档位（简单 / 中等 / 较难），直接展示给用户，不做枚举映射。 */
    @Column(name = "difficulty", length = 16)
    private String difficulty;

    @Column(name = "question_text", columnDefinition = "CLOB", nullable = false)
    private String questionText;

    @Column(name = "reference_answer", columnDefinition = "CLOB")
    private String referenceAnswer;

    /** 追问提示，JSON 数组字符串；为空表示没有预备追问。 */
    @Column(name = "follow_ups", columnDefinition = "CLOB")
    private String followUps;

    /** 同一岗位内的展示顺序，小的在前；相同则按 id。 */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /** 停用的题不进接口返回，但数据保留，避免临时下架还得删。 */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = Boolean.TRUE;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getJobCode() {
        return jobCode;
    }

    public void setJobCode(String jobCode) {
        this.jobCode = jobCode;
    }

    public String getKpCode() {
        return kpCode;
    }

    public void setKpCode(String kpCode) {
        this.kpCode = kpCode;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getReferenceAnswer() {
        return referenceAnswer;
    }

    public void setReferenceAnswer(String referenceAnswer) {
        this.referenceAnswer = referenceAnswer;
    }

    public String getFollowUps() {
        return followUps;
    }

    public void setFollowUps(String followUps) {
        this.followUps = followUps;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
