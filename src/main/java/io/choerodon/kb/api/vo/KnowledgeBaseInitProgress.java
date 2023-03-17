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

    public static final Double END_PROGRESS =  1.00D;

    @Encrypt
    private Long knowledgeBaseId;

    private String status;

    private Double process;

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
        this.process = 0D;
        this.lastProgress = 0D;
        this.knowledgeBaseId = knowledgeBaseId;
        this.uuid = uuid;
    }

    public boolean increasePointer() {
        pointer++;
        if (total == 0) {
            status = Status.SUCCEED.toString().toLowerCase();
            process = END_PROGRESS;
            return true;
        } else {
            BigDecimal pointer = new BigDecimal(this.pointer);
            BigDecimal total = new BigDecimal(this.total);
            process = pointer.divide(total, 4, RoundingMode.HALF_UP).doubleValue();
            if (process - lastProgress > 0.1D) {
                lastProgress = process;
                return true;
            }
            return false;
        }
    }

    public void toPercent() {
        if (this.process != null) {
            this.process = this.process * 100;
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

    public Double getProcess() {
        return process;
    }

    public KnowledgeBaseInitProgress setProcess(Double process) {
        this.process = process;
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
