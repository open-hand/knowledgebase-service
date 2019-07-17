package io.choerodon.kb.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.vo.PageLogVO;
import io.choerodon.kb.app.service.PageLogService;
import io.choerodon.kb.domain.kb.repository.PageRepository;
import io.choerodon.kb.infra.dto.PageLogDTO;
import io.choerodon.kb.infra.feign.vo.UserDO;
import io.choerodon.kb.infra.feign.UserFeignClient;
import io.choerodon.kb.infra.mapper.PageLogMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final String ERROR_PAGELOG_INSERT = "error.pageLog.insert";

    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PageLogMapper pageLogMapper;
    @Autowired
    private PageRepository pageRepository;

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
        pageRepository.checkById(organizationId, projectId, pageId);
        List<PageLogVO> logs = new ArrayList<>();
        List<PageLogDTO> pageLogList = pageLogMapper.selectByPageId(pageId);
        if (pageLogList != null && !pageLogList.isEmpty()) {
            List<Long> userIds = pageLogList.stream().map(PageLogDTO::getCreatedBy).distinct()
                    .collect(Collectors.toList());
            Long[] ids = new Long[userIds.size()];
            userIds.toArray(ids);
            List<UserDO> userDOList = userFeignClient.listUsersByIds(ids, false).getBody();
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
