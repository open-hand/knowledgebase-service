package io.choerodon.kb.infra.persistence.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.kb.repository.PageAttachmentRepository;
import io.choerodon.kb.infra.dataobject.PageAttachmentDO;
import io.choerodon.kb.infra.mapper.PageAttachmentMapper;

/**
 * Created by Zenger on 2019/4/29.
 */
@Service
public class PageAttachmentRepositoryImpl implements PageAttachmentRepository {

    private PageAttachmentMapper pageAttachmentMapper;

    public PageAttachmentRepositoryImpl(PageAttachmentMapper pageAttachmentMapper) {
        this.pageAttachmentMapper = pageAttachmentMapper;
    }

    @Override
    public PageAttachmentDO insert(PageAttachmentDO pageAttachmentDO) {
        if (pageAttachmentMapper.insert(pageAttachmentDO) != 1) {
            throw new CommonException("error.page.attachment.insert");
        }
        return pageAttachmentMapper.selectByPrimaryKey(pageAttachmentDO.getId());
    }

    @Override
    public PageAttachmentDO selectById(Long id) {
        return pageAttachmentMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<PageAttachmentDO> selectByPageId(Long pageId) {
        return pageAttachmentMapper.selectByPageId(pageId);
    }

    @Override
    public void delete(Long id) {
        if (pageAttachmentMapper.deleteByPrimaryKey(id) != 1) {
            throw new CommonException("error.page.attachment.delete");
        }
    }

    @Override
    public void deleteByPageId(Long pageId) {
        PageAttachmentDO pageAttachmentDO = new PageAttachmentDO();
        pageAttachmentDO.setPageId(pageId);
        pageAttachmentMapper.delete(pageAttachmentDO);
    }

}
