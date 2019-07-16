package io.choerodon.kb.app.service;

import io.choerodon.kb.api.dao.PageAutoSaveVO;
import io.choerodon.kb.api.dao.PageCreateVO;
import io.choerodon.kb.api.dao.PageVO;
import io.choerodon.kb.infra.dataobject.PageContentDO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageService {

    Boolean checkPageCreate(Long id);

    void exportMd2Pdf(Long organizationId, Long projectId, Long pageId, HttpServletResponse response);

    String importDocx2Md(Long organizationId, Long projectId, MultipartFile file, String type);

    PageVO createPage(Long projectId, PageCreateVO create, String type);

    void autoSavePage(Long organizationId, Long projectId, Long pageId, PageAutoSaveVO autoSave);

    PageContentDO queryDraftContent(Long organizationId, Long projectId, Long pageId);

    void deleteDraftContent(Long organizationId, Long projectId, Long pageId);
}
