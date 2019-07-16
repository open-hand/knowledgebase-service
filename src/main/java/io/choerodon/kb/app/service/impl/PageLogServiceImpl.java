package io.choerodon.kb.app.service.impl;

import io.choerodon.kb.api.dao.PageLogVO;
import io.choerodon.kb.app.service.PageLogService;
import io.choerodon.kb.domain.kb.repository.IamRepository;
import io.choerodon.kb.domain.kb.repository.PageLogRepository;
import io.choerodon.kb.infra.dataobject.PageLogDO;
import io.choerodon.kb.infra.dataobject.iam.UserDO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public List<PageLogVO> listByPageId(Long pageId) {
        List<PageLogVO> logs = new ArrayList<>();
        List<PageLogDO> pageLogList = pageLogRepository.selectByPageId(pageId);
        if (pageLogList != null && !pageLogList.isEmpty()) {
            List<Long> userIds = pageLogList.stream().map(PageLogDO::getCreatedBy).distinct()
                    .collect(Collectors.toList());
            Long[] ids = new Long[userIds.size()];
            userIds.toArray(ids);
            List<UserDO> userDOList = iamRepository.userDOList(ids);
            Map<Long, UserDO> userMap = new HashMap<>(userDOList.size());
            userDOList.forEach(userDO -> userMap.put(userDO.getId(), userDO));
            for (PageLogDO log : pageLogList) {
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
                UserDO userDO = userMap.getOrDefault(log.getCreatedBy(), new UserDO());
                pageLogVO.setLoginName(userDO.getLoginName());
                pageLogVO.setRealName(userDO.getRealName());
                pageLogVO.setEmail(userDO.getEmail());
                pageLogVO.setImageUrl(userDO.getImageUrl());
                pageLogVO.setUserName(userDO.getLoginName() + userDO.getRealName());
                pageLogVO.setLastUpdateDate(log.getLastUpdateDate());
                logs.add(pageLogVO);
            }
        }
        return logs;
    }
}
