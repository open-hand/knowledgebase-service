package io.choerodon.kb.api.vo;

import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

/**
 * @Author: CaiShuangLian
 * @Date: 2022/01/17
 * @Description:覆盖WorkSpaceShareUpdateVo，增加是否分享字段
 */
public class WorkSpaceShareUpdateVO {
    @ApiModelProperty(value = "分享类型")
    private String type;
    @NotNull
    @ApiModelProperty(value = "乐观锁版本号")
    private Long objectVersionNumber;
    @ApiModelProperty("是否分享")
    private Boolean enabled;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
