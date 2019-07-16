package io.choerodon.kb.api.dao;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Zenger on 2019/6/10.
 */
public class WorkSpaceShareUpdateVO {

    @NotNull
    @ApiModelProperty(value = "分享类型")
    private String type;

    @NotNull
    @ApiModelProperty(value = "乐观锁版本号")
    private Long objectVersionNumber;

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
}
