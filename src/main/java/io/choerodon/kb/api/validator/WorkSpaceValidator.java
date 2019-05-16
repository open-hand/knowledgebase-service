package io.choerodon.kb.api.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.dao.PageUpdateDTO;
import io.choerodon.kb.domain.kb.repository.WorkSpacePageRepository;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.common.utils.TypeUtil;
import io.choerodon.kb.infra.dataobject.WorkSpacePageDO;

/**
 * Created by Zenger on 2019/4/30.
 */
@Component
public class WorkSpaceValidator {

    @Autowired
    private WorkSpacePageRepository workSpacePageRepository;

    public WorkSpacePageDO checkUpdatePage(PageUpdateDTO pageUpdateDTO, Long id) {
        WorkSpacePageDO workSpacePageDO = workSpacePageRepository.selectByWorkSpaceId(id);
        if (workSpacePageDO == null) {
            throw new CommonException("error.workSpacePage.select");
        }
        if (BaseStage.SELF.equals(workSpacePageDO.getReferenceType())) {
            if (pageUpdateDTO.getContent() != null && pageUpdateDTO.getMinorEdit() == null) {
                throw new CommonException("error.parameter.update");
            }
        }
        if (BaseStage.REFERENCE_PAGE.equals(workSpacePageDO.getReferenceType())) {
            throw new CommonException("error.referenceType.updated");
        }
        if (BaseStage.REFERENCE_URL.equals(workSpacePageDO.getReferenceType())) {
            if (TypeUtil.objToString(pageUpdateDTO.getReferenceUrl()).isEmpty()) {
                throw new CommonException("error.parameter.update");
            }
        }
        return workSpacePageDO;
    }
}
