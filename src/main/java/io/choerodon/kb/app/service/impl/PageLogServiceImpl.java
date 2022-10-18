package io.choerodon.kb.app.service.impl;

import static org.hzero.core.base.BaseConstants.ErrorCode.FORBIDDEN;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.vo.PageLogVO;
import io.choerodon.kb.app.service.PageLogService;
import io.choerodon.kb.app.service.WorkSpacePageService;
import io.choerodon.kb.domain.repository.IamRemoteRepository;
import io.choerodon.kb.domain.repository.PageRepository;
import io.choerodon.kb.domain.service.PermissionCheckDomainService;
import io.choerodon.kb.infra.dto.PageLogDTO;
import io.choerodon.kb.infra.dto.WorkSpacePageDTO;
import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.kb.infra.feign.vo.UserDO;
import io.choerodon.kb.infra.mapper.PageLogMapper;

/**
 * Created by Zenger on 2019/4/30.
 */
@Service
public class PageLogServiceImpl implements PageLogService {

    private static final String ERROR_PAGELOG_INSERT = "error.pageLog.insert";

    @Autowired
    private IamRemoteRepository iamRemoteRepository;
    @Autowired
    private PageLogMapper pageLogMapper;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private WorkSpacePageService workSpacePageService;
    @Autowired
    private PermissionCheckDomainService permissionCheckDomainService;

    @Override
    public PageLogDTO baseCreate(PageLogDTO pageLogDTO) {
        if (pageLogMapper.insert(pageLogDTO) != 1) {
            throw new CommonException(ERROR_PAGELOG_INSERT);
        }
        return pageLogMapper.selectByPrimaryKey(pageLogDTO.getId());
    }

    @Override
    public void deleteByPageId(Long pageId) {
        PageLogDTO pageLogDTO = new PageLogDTO();
        pageLogDTO.setPageId(pageId);
        pageLogMapper.delete(pageLogDTO);
    }

    @Override
    public List<PageLogVO> listByPageId(Long organizationId, Long projectId, Long pageId) {
        // 鉴权-操作历史日志
        WorkSpacePageDTO workSpacePageDTO = workSpacePageService.selectByPageId(pageId);
        Assert.isTrue(permissionCheckDomainService.checkPermission(organizationId,
                projectId,
                PermissionConstants.PermissionTargetBaseType.FILE.toString(),
                null,
                workSpacePageDTO.getWorkspaceId(),
                PermissionConstants.ActionPermission.DOCUMENT_OPERATING_HISTORY.getCode()), FORBIDDEN);
        pageRepository.checkById(organizationId, projectId, pageId);
        List<PageLogVO> logs = new ArrayList<>();
        List<PageLogDTO> pageLogList = pageLogMapper.selectByPageId(pageId);
        if (pageLogList != null && !pageLogList.isEmpty()) {
            List<Long> userIds = pageLogList.stream()
                    .map(PageLogDTO::getCreatedBy)
                    .collect(Collectors.toList());
            List<UserDO> userDOList = iamRemoteRepository.listUsersByIds(userIds, false);
            Map<Long, UserDO> userMap = new HashMap<>(userDOList.size());
            userDOList.forEach(userDO -> userMap.put(userDO.getId(), userDO));
            for (PageLogDTO log : pageLogList) {
                PageLogVO pageLogVO = new PageLogVO();
                pageLogVO.setId(log.getId());
                pageLogVO.setPageId(log.getPageId());
                pageLogVO.setOperation(log.getOperation());
                pageLogVO.setField(log.getField());
                pageLogVO.setOldString(log.getOldString());
                pageLogVO.setOldValue(log.getOldValue());
                pageLogVO.setNewString(log.getNewString());
                pageLogVO.setNewValue(log.getNewString());
                pageLogVO.setUserId(log.getCreatedBy());
                pageLogVO.setCreateUser(userMap.get(log.getCreatedBy()));
                pageLogVO.setLastUpdateDate(log.getLastUpdateDate());
                logs.add(pageLogVO);
            }
        }
        return logs;
    }
}
