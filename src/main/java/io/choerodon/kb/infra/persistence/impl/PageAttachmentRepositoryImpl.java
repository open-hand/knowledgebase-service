package io.choerodon.kb.infra.persistence.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.kb.entity.PageAttachmentE;
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
    public PageAttachmentE insert(PageAttachmentE pageAttachmentE) {
        PageAttachmentDO pageAttachmentDO = ConvertHelper.convert(pageAttachmentE, PageAttachmentDO.class);
        if (pageAttachmentMapper.insert(pageAttachmentDO) != 1) {
            throw new CommonException("error.page.attachment.insert");
        }
        return ConvertHelper.convert(pageAttachmentDO, PageAttachmentE.class);
    }

    @Override
    public PageAttachmentE selectById(Long id) {
        return ConvertHelper.convert(pageAttachmentMapper.selectByPrimaryKey(id), PageAttachmentE.class);
    }

    @Override
    public List<PageAttachmentE> selectByPageId(Long pageId) {
        PageAttachmentDO pageAttachmentDO = new PageAttachmentDO();
        pageAttachmentDO.setPageId(pageId);
        return ConvertHelper.convertList(pageAttachmentMapper.select(pageAttachmentDO), PageAttachmentE.class);
    }

    @Override
    public void delete(Long id) {
        if (pageAttachmentMapper.deleteByPrimaryKey(id) != 1) {
            throw new CommonException("error.page.attachment.delete");
        }
    }
}
