package io.choerodon.kb.app.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import io.choerodon.kb.api.dao.PageLogDTO;
import io.choerodon.kb.app.service.PageLogService;
import io.choerodon.kb.domain.kb.repository.IamRepository;
import io.choerodon.kb.domain.kb.repository.PageLogRepository;
import io.choerodon.kb.infra.dataobject.PageLogDO;
import io.choerodon.kb.infra.dataobject.iam.UserDO;

/**
 * Created by Zenger on 2019/4/30.
 */
@Service
public class PageLogServiceImpl implements PageLogService {

    private IamRepository iamRepository;
    private PageLogRepository pageLogRepository;

    public PageLogServiceImpl(IamRepository iamRepository,
                              PageLogRepository pageLogRepository) {
        this.iamRepository = iamRepository;
        this.pageLogRepository = pageLogRepository;
    }

    @Override
    public List<PageLogDTO> listByPageId(Long pageId) {
        List<PageLogDTO> logs = new ArrayList<>();
        List<PageLogDO> pageLogList = pageLogRepository.selectByPageId(pageId);
        if (pageLogList != null && !pageLogList.isEmpty()) {
            List<Long> userIds = pageLogList.stream().map(PageLogDO::getCreatedBy).distinct()
                    .collect(Collectors.toList());
            Long[] ids = new Long[userIds.size()];
            userIds.toArray(ids);
            List<UserDO> userDOList = iamRepository.userDOList(ids);
            Map<Long, UserDO> userMap = new HashMap<>();
            userDOList.forEach(userDO -> userMap.put(userDO.getId(), userDO));
            for (PageLogDO log : pageLogList) {
                PageLogDTO pageLogDTO = new PageLogDTO();
                pageLogDTO.setId(log.getId());
                pageLogDTO.setPageId(log.getPageId());
                pageLogDTO.setOperation(log.getOperation());
                pageLogDTO.setField(log.getField());
                pageLogDTO.setOldString(log.getOldString());
                pageLogDTO.setOldValue(log.getOldValue());
                pageLogDTO.setNewString(log.getNewString());
                pageLogDTO.setNewValue(log.getNewString());
                pageLogDTO.setUserId(log.getCreatedBy());
                pageLogDTO.setLoginName(userMap.get(log.getCreatedBy()).getLoginName());
                pageLogDTO.setRealName(userMap.get(log.getCreatedBy()).getRealName());
                pageLogDTO.setEmail(userMap.get(log.getCreatedBy()).getEmail());
                pageLogDTO.setImageUrl(userMap.get(log.getCreatedBy()).getImageUrl());
                pageLogDTO.setUserName(userMap.get(log.getCreatedBy()).getLoginName() + userMap.get(log.getCreatedBy()).getRealName());
                pageLogDTO.setLastUpdateDate(log.getLastUpdateDate());
                logs.add(pageLogDTO);
            }
        }
        return logs;
    }
}
