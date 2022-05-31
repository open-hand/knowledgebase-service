package io.choerodon.kb.api.vo;

import io.swagger.annotations.ApiModelProperty;
import java.util.List;

/**
 * Created by wangxiang on 2022/5/25
 */
public class OnlineUserVO {

    @ApiModelProperty("在线用户的Id")
    private List<String> ids;

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }
}
