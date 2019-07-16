package io.choerodon.kb.domain.kb.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.kb.api.dao.WorkSpaceShareVO;
import io.choerodon.kb.infra.dataobject.WorkSpaceShareDO;

/**
 * Created by Zenger on 2019/6/10.
 */
@Component
public class WorkSpaceShareConvertor implements ConvertorI<Object, WorkSpaceShareDO, WorkSpaceShareVO> {

    @Override
    public WorkSpaceShareVO doToDto(WorkSpaceShareDO dataObject) {
        WorkSpaceShareVO workSpaceShareVO = new WorkSpaceShareVO();
        BeanUtils.copyProperties(dataObject, workSpaceShareVO);
        return workSpaceShareVO;
    }
}
