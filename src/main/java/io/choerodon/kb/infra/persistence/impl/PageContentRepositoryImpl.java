package io.choerodon.kb.infra.persistence.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.kb.repository.PageContentRepository;
import io.choerodon.kb.infra.dto.PageContentDTO;
import io.choerodon.kb.infra.mapper.PageContentMapper;
import io.choerodon.mybatis.entity.Criteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author shinan.chen
 * @since 2019/5/16
 */
@Service
public class PageContentRepositoryImpl implements PageContentRepository {

    private static final String ERROR_PAGECONTENT_ILLEGAL = "error.pageContent.illegal";
    private static final String ERROR_PAGECONTENT_CREATE = "error.pageContent.create";
    private static final String ERROR_PAGECONTENT_DELETE = "error.pageContent.delete";
    private static final String ERROR_PAGECONTENT_NOTFOUND = "error.pageContent.notFound";
    private static final String ERROR_PAGECONTENT_UPDATE = "error.pageContent.update";

    @Autowired
    private PageContentMapper pageContentMapper;

    @Override
    public PageContentDTO baseCreate(PageContentDTO create) {
        if (pageContentMapper.insert(create) != 1) {
            throw new CommonException(ERROR_PAGECONTENT_CREATE);
        }
        return pageContentMapper.selectByVersionId(create.getId());
    }

    @Override
    public void baseUpdate(PageContentDTO update) {
        if (pageContentMapper.updateByPrimaryKeySelective(update) != 1) {
            throw new CommonException(ERROR_PAGECONTENT_UPDATE);
        }
    }

    @Override
    public void baseUpdateOptions(PageContentDTO update, String... fields) {
        Criteria criteria = new Criteria();
        criteria.update(fields);
        if (pageContentMapper.updateByPrimaryKeyOptions(update, criteria) != 1) {
            throw new CommonException(ERROR_PAGECONTENT_UPDATE);
        }
    }

    @Override
    public PageContentDTO selectByVersionId(Long versionId, Long pageId) {
        PageContentDTO content = pageContentMapper.selectByVersionId(versionId);
        if (content == null) {
            throw new CommonException(ERROR_PAGECONTENT_NOTFOUND);
        }
        if (!content.getPageId().equals(pageId)) {
            throw new CommonException(ERROR_PAGECONTENT_ILLEGAL);
        }
        return content;
    }
}
