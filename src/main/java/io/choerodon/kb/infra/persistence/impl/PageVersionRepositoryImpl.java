package io.choerodon.kb.infra.persistence.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.kb.repository.PageVersionRepository;
import io.choerodon.kb.infra.dto.PageVersionDTO;
import io.choerodon.kb.infra.mapper.PageVersionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Zenger on 2019/4/29.
 */
@Service
public class PageVersionRepositoryImpl implements PageVersionRepository {

    @Autowired
    private PageVersionMapper pageVersionMapper;

    private static final String ERROR_PAGEVERSION_ILLEGAL = "error.pageVersion.illegal";
    private static final String ERROR_PAGEVERSION_CREATE = "error.pageVersion.create";
    private static final String ERROR_PAGEVERSION_DELETE = "error.pageVersion.delete";
    private static final String ERROR_PAGEVERSION_NOTFOUND = "error.pageVersion.notFound";
    private static final String ERROR_PAGEVERSION_UPDATE = "error.pageVersion.update";

    @Override
    public PageVersionDTO selectOne(PageVersionDTO pageVersionDTO) {
        return pageVersionMapper.selectOne(pageVersionDTO);
    }

    @Override
    public void deleteByPageId(Long pageId) {
        pageVersionMapper.deleteByPageId(pageId);
    }

    @Override
    public PageVersionDTO create(PageVersionDTO create) {
        if (pageVersionMapper.insert(create) != 1) {
            throw new CommonException(ERROR_PAGEVERSION_CREATE);
        }
        return pageVersionMapper.selectByPrimaryKey(create.getId());
    }

    @Override
    public void delete(Long versionId) {
        if (pageVersionMapper.deleteByPrimaryKey(versionId) != 1) {
            throw new CommonException(ERROR_PAGEVERSION_DELETE);
        }
    }

    @Override
    public void update(PageVersionDTO update) {
        if (pageVersionMapper.updateByPrimaryKeySelective(update) != 1) {
            throw new CommonException(ERROR_PAGEVERSION_UPDATE);
        }
    }

    @Override
    public PageVersionDTO queryByVersionId(Long versionId, Long pageId) {
        PageVersionDTO version = pageVersionMapper.selectByPrimaryKey(versionId);
        if (version == null) {
            throw new CommonException(ERROR_PAGEVERSION_NOTFOUND);
        }
        if (!version.getPageId().equals(pageId)) {
            throw new CommonException(ERROR_PAGEVERSION_ILLEGAL);
        }
        return version;
    }

    @Override
    public String selectMaxVersionByPageId(Long pageId) {
        String oldVersionName = pageVersionMapper.selectMaxVersionByPageId(pageId);
        if (oldVersionName == null) {
            throw new CommonException(ERROR_PAGEVERSION_NOTFOUND);
        }
        return oldVersionName;
    }
}
