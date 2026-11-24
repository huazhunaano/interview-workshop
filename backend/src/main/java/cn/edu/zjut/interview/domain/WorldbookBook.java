package cn.edu.zjut.interview.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 世界书「一本书」，对应 DDL {@code worldbook_book}（issue #1）。
 *
 * <p>一个岗位一本书：{@code code} 与文件 {@code game_data/worldbooks/<code>.yml} 同名，
 * 启动扫描时按它幂等入库。注入本身仍走 {@code interviewer.worldbook} 那份 YAML 全文，
 * 这张表是给「看得到、列得出、查得到」用的管理视图，不改变注入链路。
 */
@Entity
@Table(name = "worldbook_book")
public class WorldbookBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 与文件名同名（java / frontend），也是接口路径里的标识。 */
    @Column(name = "code", length = 64, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 128, nullable = false)
    private String name;

    /** 岗位编码，与 {@code interview_question.job_code} 同一套值。 */
    @Column(name = "job_code", length = 32)
    private String jobCode;

    @Column(name = "description", length = 512)
    private String description;

    /** 来源文件相对路径，方便知道这本书是从哪份 yml 导进来的。 */
    @Column(name = "source_path", length = 255)
    private String sourcePath;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJobCode() {
        return jobCode;
    }

    public void setJobCode(String jobCode) {
        this.jobCode = jobCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
