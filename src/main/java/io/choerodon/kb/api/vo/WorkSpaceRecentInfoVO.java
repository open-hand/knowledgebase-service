package io.choerodon.kb.api.vo;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author shinan.chen
 * @since 2019/10/10
 */
public class WorkSpaceRecentInfoVO {
    @ApiModelProperty(value = "页面最后修改日期字符串")
    private String lastUpdateDateStr;
    @ApiModelProperty(value = "排序日期")
    private String sortDateStr;
    @ApiModelProperty(value = "空间共享对象列表")
    private List<WorkSpaceRecentVO> workSpaceRecents;

    public WorkSpaceRecentInfoVO() {
    }

    public WorkSpaceRecentInfoVO(String lastUpdateDateStr, String sortDateStr, List<WorkSpaceRecentVO> workSpaceRecents) {
        this.lastUpdateDateStr = lastUpdateDateStr;
        this.sortDateStr = sortDateStr;
        this.workSpaceRecents = workSpaceRecents;
    }

    public String getLastUpdateDateStr() {
        return lastUpdateDateStr;
    }

    public void setLastUpdateDateStr(String lastUpdateDateStr) {
        this.lastUpdateDateStr = lastUpdateDateStr;
    }

    public String getSortDateStr() {
        return sortDateStr;
    }

    public void setSortDateStr(String sortDateStr) {
        this.sortDateStr = sortDateStr;
    }

    public List<WorkSpaceRecentVO> getWorkSpaceRecents() {
        return workSpaceRecents;
    }

    public void setWorkSpaceRecents(List<WorkSpaceRecentVO> workSpaceRecents) {
        this.workSpaceRecents = workSpaceRecents;
    }
}
