package io.choerodon.kb.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.app.service.PageContentService;
import io.choerodon.kb.infra.dto.PageContentDTO;
import io.choerodon.kb.infra.mapper.PageContentMapper;
import io.choerodon.mybatis.helper.OptionalHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * @author shinan.chen
 * @since 2019/5/16
 */
@Service
public class PageContentServiceImpl implements PageContentService {

    private static final String ERROR_PAGECONTENT_ILLEGAL = "error.pageContent.illegal";
    private static final String ERROR_PAGECONTENT_CREATE = "error.pageContent.create";
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
        OptionalHelper.optional(Arrays.asList(fields));
        if (pageContentMapper.updateOptional(update) != 1) {
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
