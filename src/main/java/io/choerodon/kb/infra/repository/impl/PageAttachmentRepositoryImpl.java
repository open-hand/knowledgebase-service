package io.choerodon.kb.infra.repository.impl;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.vo.PageAttachmentVO;
import io.choerodon.kb.domain.repository.PageAttachmentRepository;
import io.choerodon.kb.domain.repository.PageRepository;
import io.choerodon.kb.infra.annotation.DataLog;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.dto.PageAttachmentDTO;
import io.choerodon.kb.infra.mapper.PageAttachmentMapper;
import io.choerodon.kb.infra.utils.FilePathHelper;

/**
 * Created by Zenger on 2019/4/29.
 */
@Service
public class PageAttachmentRepositoryImpl implements PageAttachmentRepository {

    private static final String ERROR_PAGE_ATTACHMENT_INSERT = "error.page.attachment.insert";
    private static final String ERROR_PAGE_ATTACHMENT_DELETE = "error.page.attachment.delete";
    private static final String ERROR_PAGE_ATTACHMENT_GET = "error.page.attachment.get";

    @Autowired
    private PageAttachmentMapper pageAttachmentMapper;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private FilePathHelper filePathHelper;
    @Autowired
    private ModelMapper modelMapper;


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

    @Override
    public List<PageAttachmentVO> queryByList(Long organizationId, Long projectId, Long pageId) {
        pageRepository.baseQueryByIdWithOrg(organizationId, projectId, pageId);
        List<PageAttachmentDTO> pageAttachments = pageAttachmentMapper.selectByPageId(pageId);
        if (pageAttachments != null && !pageAttachments.isEmpty()) {
            pageAttachments.stream().forEach(pageAttachmentDO -> pageAttachmentDO.setUrl(
                    filePathHelper.generateFullPath(pageAttachmentDO.getUrl())));
        }
        return modelMapper.map(pageAttachments, new TypeToken<List<PageAttachmentVO>>() {
        }.getType());
    }
}
