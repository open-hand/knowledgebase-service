package io.choerodon.kb.infra.persistence.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.kb.repository.PageLogRepository;
import io.choerodon.kb.infra.dto.PageLogDTO;
import io.choerodon.kb.infra.mapper.PageLogMapper;
import org.springframework.stereotype.Service;

/**
 * Created by Zenger on 2019/4/29.
 */
@Service
public class PageLogRepositoryImpl implements PageLogRepository {

    private static final String ERROR_PAGE_LOG_INSERT = "error.page.log.insert";

    private PageLogMapper pageLogMapper;

    public PageLogRepositoryImpl(PageLogMapper pageLogMapper) {
        this.pageLogMapper = pageLogMapper;
    }

    @Override
    public PageLogDTO baseCreate(PageLogDTO pageLogDTO) {
        if (pageLogMapper.insert(pageLogDTO) != 1) {
            throw new CommonException(ERROR_PAGE_LOG_INSERT);
        }
        return pageLogMapper.selectByPrimaryKey(pageLogDTO.getId());
    }

    @Override
    public void deleteByPageId(Long pageId) {
        PageLogDTO pageLogDTO = new PageLogDTO();
        pageLogDTO.setPageId(pageId);
        pageLogMapper.delete(pageLogDTO);
    }
}
