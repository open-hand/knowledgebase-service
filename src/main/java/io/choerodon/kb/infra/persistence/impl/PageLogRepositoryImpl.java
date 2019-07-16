package io.choerodon.kb.infra.persistence.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.kb.repository.PageLogRepository;
import io.choerodon.kb.infra.dto.PageLogDTO;
import io.choerodon.kb.infra.mapper.PageLogMapper;

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
    public PageLogDTO insert(PageLogDTO pageLogDTO) {
        if (pageLogMapper.insert(pageLogDTO) != 1) {
            throw new CommonException(ERROR_PAGE_LOG_INSERT);
        }
        return pageLogMapper.selectByPrimaryKey(pageLogDTO.getId());
    }

    @Override
    public List<PageLogDTO> selectByPageId(Long pageId) {
        return pageLogMapper.selectByPageId(pageId);
    }

    @Override
    public void deleteByPageId(Long pageId) {
        PageLogDTO pageLogDTO = new PageLogDTO();
        pageLogDTO.setPageId(pageId);
        pageLogMapper.delete(pageLogDTO);
    }
}
