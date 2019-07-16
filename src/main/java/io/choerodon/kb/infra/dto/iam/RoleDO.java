package io.choerodon.kb.infra.dto.iam;

import java.util.List;

/**
 * Created by Zenger on 2019/4/29.
 */
public class RoleDO {

    private Long id;
    private String name;
    private String code;
    private String description;
    private String level;
    private Boolean enabled;
    private List<LabelDO> labels;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public List<LabelDO> getLabels() {
        return labels;
    }

    public void setLabels(List<LabelDO> labels) {
        this.labels = labels;
    }
}
