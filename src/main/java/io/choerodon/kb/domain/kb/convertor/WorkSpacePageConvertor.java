package io.choerodon.kb.domain.kb.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.kb.domain.kb.entity.WorkSpacePageE;
import io.choerodon.kb.infra.dataobject.WorkSpacePageDO;

/**
 * Created by Zenger on 2019/4/30.
 */
@Component
public class WorkSpacePageConvertor implements ConvertorI<WorkSpacePageE, WorkSpacePageDO, Object> {

    @Override
    public WorkSpacePageE doToEntity(WorkSpacePageDO workSpacePageDO) {
        WorkSpacePageE workSpacePageE = new WorkSpacePageE();
        BeanUtils.copyProperties(workSpacePageDO, workSpacePageE);
        return workSpacePageE;
    }

    @Override
    public WorkSpacePageDO entityToDo(WorkSpacePageE entity) {
        WorkSpacePageDO workSpacePageDO = new WorkSpacePageDO();
        BeanUtils.copyProperties(entity, workSpacePageDO);
        return workSpacePageDO;
    }
}
