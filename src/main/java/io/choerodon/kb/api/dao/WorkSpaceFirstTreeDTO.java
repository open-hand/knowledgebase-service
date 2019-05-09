package io.choerodon.kb.api.dao;

import java.util.Map;

/**
 * Created by Zenger on 2019/5/6.
 */
public class WorkSpaceFirstTreeDTO {

    private Long rootId;
    private Map<Long, WorkSpaceTreeDTO> items;

    public Long getRootId() {
        return rootId;
    }

    public void setRootId(Long rootId) {
        this.rootId = rootId;
    }

    public Map<Long, WorkSpaceTreeDTO> getItems() {
        return items;
    }

    public void setItems(Map<Long, WorkSpaceTreeDTO> items) {
        this.items = items;
    }
}
