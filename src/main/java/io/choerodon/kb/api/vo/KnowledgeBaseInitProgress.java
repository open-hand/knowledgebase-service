package io.choerodon.kb.api.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author superlee
 * @since 2023-03-03
 */
public class KnowledgeBaseInitProgress {

    private static final String WEBSOCKET_COPY_KNOWLEDGE_BASE = "knowledge-copy-knowledge-base-";

    @Encrypt
    private Long knowledgeBaseId;

    private String status;

    private Double progress;

    private String websocketKey;

    private String uuid;

    @JsonIgnore
    private Integer total;

    @JsonIgnore
    private Integer pointer;
    @JsonIgnore
    private Double lastProgress;

    public KnowledgeBaseInitProgress() {
    }

    public KnowledgeBaseInitProgress(Long knowledgeBaseId,
                                     String uuid) {
        this.websocketKey = WEBSOCKET_COPY_KNOWLEDGE_BASE + uuid;
        this.status = Status.DOING.toString().toLowerCase();
        this.pointer = 0;
        this.total = 0;
        this.progress = 0D;
        this.lastProgress = 0D;
        this.knowledgeBaseId = knowledgeBaseId;
        this.uuid = uuid;
    }

    public boolean increasePointer() {
        pointer++;
        if (total == 0) {
            status = Status.SUCCEED.toString().toLowerCase();
            progress = 1.00D;
            return true;
        } else {
            BigDecimal pointer = new BigDecimal(this.pointer);
            BigDecimal total = new BigDecimal(this.total);
            progress = pointer.divide(total, 4, RoundingMode.HALF_UP).doubleValue();
            if (progress - lastProgress > 0.1D) {
                lastProgress = progress;
                return true;
            }
            return false;
        }
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Integer getPointer() {
        return pointer;
    }

    public KnowledgeBaseInitProgress setPointer(Integer pointer) {
        this.pointer = pointer;
        return this;
    }

    public String getWebsocketKey() {
        return websocketKey;
    }

    public KnowledgeBaseInitProgress setWebsocketKey(String websocketKey) {
        this.websocketKey = websocketKey;
        return this;
    }

    public Integer getTotal() {
        return total;
    }

    public KnowledgeBaseInitProgress setTotal(Integer total) {
        this.total = total;
        return this;
    }

    public Long getKnowledgeBaseId() {
        return knowledgeBaseId;
    }

    public KnowledgeBaseInitProgress setKnowledgeBaseId(Long knowledgeBaseId) {
        this.knowledgeBaseId = knowledgeBaseId;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public KnowledgeBaseInitProgress setStatus(String status) {
        this.status = status;
        return this;
    }

    public Double getProgress() {
        return progress;
    }

    public KnowledgeBaseInitProgress setProgress(Double progress) {
        this.progress = progress;
        return this;
    }

    public Double getLastProgress() {
        return lastProgress;
    }

    public void setLastProgress(Double lastProgress) {
        this.lastProgress = lastProgress;
    }

    public enum Status {
        DOING, SUCCEED, FAILED
    }
}
