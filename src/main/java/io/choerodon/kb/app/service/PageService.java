package io.choerodon.kb.app.service;

import io.choerodon.kb.api.vo.*;
import io.choerodon.kb.infra.dto.PageContentDTO;
import io.choerodon.kb.infra.dto.PageDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageService {

    /**
     * 创建空内容的页面
     *
     * @param organizationId
     * @param projectId
     * @param pageCreateVO
     * @return
     */
    PageDTO createPage(Long organizationId, Long projectId, PageCreateWithoutContentVO pageCreateVO);

    /**
     * 创建带有内容的页面
     *
     * @param organizationId
     * @param projectId
     * @param create
     * @return
     */
    WorkSpaceInfoVO createPageWithContent(Long organizationId, Long projectId, PageCreateVO create);

    void exportMd2Pdf(Long organizationId, Long projectId, Long pageId, HttpServletResponse response);

    String importDocx2Md(Long organizationId, Long projectId, MultipartFile file);

    void autoSavePage(Long organizationId, Long projectId, Long pageId, PageAutoSaveVO autoSave);

    PageContentDTO queryDraftContent(Long organizationId, Long projectId, Long pageId);

    void deleteDraftContent(Long organizationId, Long projectId, Long pageId);

    void createByTemplate(Long organizationId, Long projectId, Long id, Long templateBaseId);

    /**
     * 创建文档(可选择模板创建)
     * @param organizationId
     * @param projectId
     * @param pageCreateVO
     * @param templateWorkSpaceId
     * @return
     */
    WorkSpaceInfoVO createPageByTemplate(Long organizationId, Long projectId, PageCreateVO pageCreateVO, Long templateWorkSpaceId);
}
