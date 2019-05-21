package io.choerodon.kb.infra.persistence.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.kb.repository.PageAttachmentRepository;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.common.annotation.DataLog;
import io.choerodon.kb.infra.dataobject.PageAttachmentDO;
import io.choerodon.kb.infra.mapper.PageAttachmentMapper;

/**
 * Created by Zenger on 2019/4/29.
 */
@Service
public class PageAttachmentRepositoryImpl implements PageAttachmentRepository {

    private static final String ERROR_PAGE_ATTACHMENT_INSERT = "error.page.attachment.insert";
    private static final String ERROR_PAGE_ATTACHMENT_DELETE = "error.page.attachment.delete";
    private static final String ERROR_PAGE_ATTACHMENT_GET = "error.page.attachment.get";

    private PageAttachmentMapper pageAttachmentMapper;

    public PageAttachmentRepositoryImpl(PageAttachmentMapper pageAttachmentMapper) {
        this.pageAttachmentMapper = pageAttachmentMapper;
    }

    @Override
    @DataLog(type = BaseStage.ATTACHMENT_CREATE)
    public PageAttachmentDO insert(PageAttachmentDO pageAttachmentDO) {
        if (pageAttachmentMapper.insert(pageAttachmentDO) != 1) {
            throw new CommonException(ERROR_PAGE_ATTACHMENT_INSERT);
        }
        return pageAttachmentMapper.selectByPrimaryKey(pageAttachmentDO.getId());
    }

    @Override
    public PageAttachmentDO selectById(Long id) {
        PageAttachmentDO pageAttachmentDO = pageAttachmentMapper.selectByPrimaryKey(id);
        if (pageAttachmentDO == null) {
            throw new CommonException(ERROR_PAGE_ATTACHMENT_GET);
        }
        return pageAttachmentDO;
    }

    @Override
    public List<PageAttachmentDO> selectByIds(List<Long> ids) {
        return pageAttachmentMapper.selectByIds(ids);
    }

    @Override
    public List<PageAttachmentDO> selectByPageId(Long pageId) {
        return pageAttachmentMapper.selectByPageId(pageId);
    }

    @Override
    @DataLog(type = BaseStage.ATTACHMENT_DELETE)
    public void delete(Long id) {
        if (pageAttachmentMapper.deleteByPrimaryKey(id) != 1) {
            throw new CommonException(ERROR_PAGE_ATTACHMENT_DELETE);
        }
    }

    @Override
    public void deleteByPageId(Long pageId) {
        PageAttachmentDO pageAttachmentDO = new PageAttachmentDO();
        pageAttachmentDO.setPageId(pageId);
        pageAttachmentMapper.delete(pageAttachmentDO);
    }

}
