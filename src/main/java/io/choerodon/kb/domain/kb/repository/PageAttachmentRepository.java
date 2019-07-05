package io.choerodon.kb.domain.kb.repository;

import java.util.List;

import io.choerodon.kb.infra.dataobject.PageAttachmentDO;

/**
 * Created by Zenger on 2019/4/29.
 */
public interface PageAttachmentRepository {

    PageAttachmentDO insert(PageAttachmentDO pageAttachmentDO);

    PageAttachmentDO selectById(Long id);

    List<PageAttachmentDO> selectByIds(List<Long> ids);

    List<PageAttachmentDO> selectByPageId(Long pageId);

    void delete(Long id);

    List<PageAttachmentDO> searchAttachment(Long organizationId, Long projectId, String fileName, String attachmentUrl);
}
