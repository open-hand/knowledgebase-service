package io.choerodon.kb.app.service.impl;

import java.util.List;
import java.util.Objects;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.app.service.WorkSpacePageService;
import io.choerodon.kb.domain.repository.PageRepository;
import io.choerodon.kb.infra.dto.WorkSpacePageDTO;
import io.choerodon.kb.infra.mapper.WorkSpacePageMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Zenger on 2019/6/10.
 */
@Service
public class WorkSpacePageServiceImpl implements WorkSpacePageService {

    private final Logger logger = LoggerFactory.getLogger(WorkSpaceServiceImpl.class);

    private static final String ERROR_WORKSPACEPAGE_INSERT = "error.workSpacePage.insert";
    private static final String ERROR_WORKSPACEPAGE_SELECT = "error.workSpacePage.select";
    private static final String ERROR_WORKSPACEPAGE_DELETE = "error.workSpacePage.delete";

    @Autowired
    private WorkSpacePageMapper workSpacePageMapper;
    @Autowired
    private PageRepository pageRepository;

    @Override
    public WorkSpacePageDTO baseCreate(WorkSpacePageDTO workSpacePageDTO) {
        if (workSpacePageMapper.insert(workSpacePageDTO) != 1) {
            throw new CommonException(ERROR_WORKSPACEPAGE_INSERT);
        }
        return workSpacePageMapper.selectByPrimaryKey(workSpacePageDTO.getId());
    }

    @Override
    public WorkSpacePageDTO selectByWorkSpaceId(Long workSpaceId) {
        WorkSpacePageDTO workSpacePageDTO = new WorkSpacePageDTO();
        workSpacePageDTO.setWorkspaceId(workSpaceId);
        WorkSpacePageDTO workSpacePage = workSpacePageMapper.selectOne(workSpacePageDTO);
        if (workSpacePage == null) {
            throw new CommonException(ERROR_WORKSPACEPAGE_SELECT);
        }
        return workSpacePage;
    }

    @Override
    public WorkSpacePageDTO selectByPageId(Long pageId) {
        WorkSpacePageDTO workSpacePageDTO = new WorkSpacePageDTO();
        workSpacePageDTO.setPageId(pageId);
        // TODO 后续页面可以关联多个空间时，这里需要做调整
        List<WorkSpacePageDTO> selects = workSpacePageMapper.select(workSpacePageDTO);
        if (selects.isEmpty()) {
            throw new CommonException(ERROR_WORKSPACEPAGE_SELECT);
        }
        return selects.get(0);
    }

    @Override
    public void baseDelete(Long id) {
        if (workSpacePageMapper.deleteByPrimaryKey(id) != 1) {
            throw new CommonException(ERROR_WORKSPACEPAGE_DELETE);
        }
    }

    @Override
    public void createOrUpdateEs(Long workSpaceId) {
        WorkSpacePageDTO workSpacePageDTO = new WorkSpacePageDTO();
        workSpacePageDTO.setWorkspaceId(workSpaceId);
        WorkSpacePageDTO workSpacePage = workSpacePageMapper.selectOne(workSpacePageDTO);
        if (Objects.isNull(workSpacePage)) {
            logger.error("未查询到page关联，因此未更新page的es搜索");
            return;
        }
        pageRepository.createOrUpdateEs(workSpacePage.getPageId());
    }

    @Override
    public void deleteEs(Long workSpaceId) {
        WorkSpacePageDTO workSpacePageDTO = new WorkSpacePageDTO();
        workSpacePageDTO.setWorkspaceId(workSpaceId);
        WorkSpacePageDTO workSpacePage = workSpacePageMapper.selectOne(workSpacePageDTO);
        if (Objects.isNull(workSpacePage)) {
            logger.error("未查询到page关联，因此未更新page的es搜索");
            return;
        }
        pageRepository.deleteEs(workSpacePage.getPageId());
    }
}
