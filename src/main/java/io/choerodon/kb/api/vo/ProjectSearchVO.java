package io.choerodon.kb.api.vo;

import java.util.Set;

/**
 * Copyright (c) 2022. Zknow Enterprise Solution. All right reserved.
 *
 * @author zongqi.hao@zknow.com
 * @since 2022/9/22
 */
public class ProjectSearchVO {

    /**
     * 当前项目id
     */
    private String param;
    private Set<Long> topProjectIds;
    private Set<Long> ignoredProjectIds;

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public Set<Long> getTopProjectIds() {
        return topProjectIds;
    }

    public void setTopProjectIds(Set<Long> topProjectIds) {
        this.topProjectIds = topProjectIds;
    }

    public Set<Long> getIgnoredProjectIds() {
        return ignoredProjectIds;
    }

    public void setIgnoredProjectIds(Set<Long> ignoredProjectIds) {
        this.ignoredProjectIds = ignoredProjectIds;
    }
}
