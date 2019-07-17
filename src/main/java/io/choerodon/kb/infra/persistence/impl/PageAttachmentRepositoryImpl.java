package io.choerodon.kb.infra.persistence.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.kb.repository.PageAttachmentRepository;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.annotation.DataLog;
import io.choerodon.kb.infra.dto.PageAttachmentDTO;
import io.choerodon.kb.infra.mapper.PageAttachmentMapper;
import org.springframework.stereotype.Service;

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
    public PageAttachmentDTO baseCreate(PageAttachmentDTO pageAttachmentDTO) {
        if (pageAttachmentMapper.insert(pageAttachmentDTO) != 1) {
            throw new CommonException(ERROR_PAGE_ATTACHMENT_INSERT);
        }
        return pageAttachmentMapper.selectByPrimaryKey(pageAttachmentDTO.getId());
    }

    @Override
    public PageAttachmentDTO baseQueryById(Long id) {
        PageAttachmentDTO pageAttachmentDTO = pageAttachmentMapper.selectByPrimaryKey(id);
        if (pageAttachmentDTO == null) {
            throw new CommonException(ERROR_PAGE_ATTACHMENT_GET);
        }
        return pageAttachmentDTO;
    }

    @Override
    @DataLog(type = BaseStage.ATTACHMENT_DELETE)
    public void baseDelete(Long id) {
        if (pageAttachmentMapper.deleteByPrimaryKey(id) != 1) {
            throw new CommonException(ERROR_PAGE_ATTACHMENT_DELETE);
        }
    }
}
