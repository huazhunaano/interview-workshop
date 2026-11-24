package cn.edu.zjut.interview.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 世界书里的一个条目，对应 DDL {@code worldbook_entry}（issue #1）。
 *
 * <p>{@code entryId} 是 YAML 里的 {@code id}，同一本书内唯一；导入时按它做增改删，
 * 所以删掉 YAML 里的条目再重启，库里的对应行也会消失。
 */
@Entity
@Table(name = "worldbook_entry")
public class WorldbookEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "entry_id", length = 64, nullable = false)
    private String entryId;

    @Column(name = "name", length = 128)
    private String name;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = Boolean.TRUE;

    /** 越大越靠前，只在同一批注入的条目之间排序（对齐原版 render）。 */
    @Column(name = "priority")
    private Integer priority = 0;

    /** always 常驻 / keyword 命中注入。 */
    @Column(name = "activation_type", length = 16, nullable = false)
    private String activationType;

    /** 关键词数组的 JSON 字符串（always 条目为空）。 */
    @Column(name = "entry_keys", columnDefinition = "CLOB")
    private String entryKeys;

    @Column(name = "content", columnDefinition = "CLOB")
    private String content;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getActivationType() {
        return activationType;
    }

    public void setActivationType(String activationType) {
        this.activationType = activationType;
    }

    public String getEntryKeys() {
        return entryKeys;
    }

    public void setEntryKeys(String entryKeys) {
        this.entryKeys = entryKeys;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
