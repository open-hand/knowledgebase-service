package io.choerodon.kb.infra.persistence.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.kb.repository.PageContentRepository;
import io.choerodon.kb.infra.dataobject.PageContentDO;
import io.choerodon.kb.infra.mapper.PageContentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author shinan.chen
 * @since 2019/5/16
 */
@Service
public class PageContentRepositoryImpl implements PageContentRepository {

    @Autowired
    private PageContentMapper pageContentMapper;

    private static final String ERROR_PAGECONTENT_ILLEGAL = "error.pageContent.illegal";
    private static final String ERROR_PAGECONTENT_CREATE = "error.pageContent.create";
    private static final String ERROR_PAGECONTENT_DELETE = "error.pageContent.delete";
    private static final String ERROR_PAGECONTENT_NOTFOUND = "error.pageContent.notFound";
    private static final String ERROR_PAGECONTENT_UPDATE = "error.pageContent.update";

    @Override
    public void deleteByPageId(Long pageId) {
        pageContentMapper.deleteByPageId(pageId);
    }

    @Override
    public PageContentDO create(PageContentDO create) {
        if (pageContentMapper.insert(create) != 1) {
            throw new CommonException(ERROR_PAGECONTENT_CREATE);
        }
        return pageContentMapper.selectByVersionId(create.getId());
    }

    @Override
    public void delete(Long id) {
        if (pageContentMapper.deleteByPrimaryKey(id) != 1) {
            throw new CommonException(ERROR_PAGECONTENT_DELETE);
        }
    }

    @Override
    public void update(PageContentDO update) {
        if (pageContentMapper.updateByPrimaryKeySelective(update) != 1) {
            throw new CommonException(ERROR_PAGECONTENT_UPDATE);
        }
    }

    @Override
    public PageContentDO selectByVersionId(Long versionId, Long pageId) {
        PageContentDO content = pageContentMapper.selectByVersionId(versionId);
        if (content == null) {
            throw new CommonException(ERROR_PAGECONTENT_NOTFOUND);
        }
        if (!content.getPageId().equals(pageId)) {
            throw new CommonException(ERROR_PAGECONTENT_ILLEGAL);
        }
        return content;
    }

    @Override
    public PageContentDO selectLatestByPageId(Long pageId) {
        PageContentDO content = pageContentMapper.selectLatestByPageId(pageId);
        if (content == null) {
            throw new CommonException(ERROR_PAGECONTENT_NOTFOUND);
        }
        return content;
    }
}
