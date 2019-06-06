package io.choerodon.kb.api.dao;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Zenger on 2019/6/6.
 */
public class MigrationDTO {

    @ApiModelProperty(value = "Wiki页面路径")
    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
