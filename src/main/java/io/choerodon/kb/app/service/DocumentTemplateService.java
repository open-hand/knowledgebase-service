package io.choerodon.kb.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.kb.api.vo.*;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author zhaotianxin
 * @since 2020/1/2
 */
public interface DocumentTemplateService {
    /**
     * 在知识库下创建模板文档
     *
     * @param projectId
     * @param organizationId
     * @param pageCreateVO
     * @param baseTemplateId
     * @return
     */
    DocumentTemplateInfoVO createTemplate(Long projectId, Long organizationId, PageCreateWithoutContentVO pageCreateVO, Long baseTemplateId);

    /**
     * 在知识库下修改模板
     *
     * @param organizationId
     * @param projectId
     * @param id
     * @param searchStr
     * @param pageUpdateVO
     * @return
     */
    WorkSpaceInfoVO updateTemplate(Long organizationId, Long projectId, Long id, String searchStr, PageUpdateVO pageUpdateVO);

    /**
     * 分页查询知识库下面的模板文档
     *
     * @param organizationId
     * @param projectId
     * @param pageRequest
     * @param searchVO
     * @return
     */
    Page<DocumentTemplateInfoVO> listTemplate(Long organizationId, Long projectId, Long baseId, PageRequest pageRequest, SearchVO searchVO);

    /**
     * 查询系统内置的模板
     *
     * @param organizationId
     * @param projectId
     * @param searchVO
     * @return
     */
    List<KnowledgeBaseTreeVO> listSystemTemplate(Long organizationId, Long projectId, SearchVO searchVO);

    /**
     * 给模板上传附件
     *
     * @param organizationId
     * @param projectId
     * @param pageId
     * @param file
     * @return
     */
    List<PageAttachmentVO> createAttachment(Long organizationId, Long projectId, Long pageId, List<MultipartFile> file);

    /**
     * 模板删除附件
     *
     * @param organizationId
     * @param projectId
     * @param id
     */
    void deleteAttachment(long organizationId, Long projectId, Long id);

    /**
     * 将模板移到回收站
     *
     * @param organizationId
     * @param projectId
     * @param id
     * @param isAdmin
     */
    void removeWorkSpaceAndPage(Long organizationId, Long projectId, Long id, boolean isAdmin);

    /**
     * 基于已有模板去创建模板
     *
     * @param projectId
     * @param organizationId
     * @param pageCreateVO
     * @param templateId
     * @return
     */
    DocumentTemplateInfoVO createByTemplate(Long projectId, Long organizationId, PageCreateWithoutContentVO pageCreateVO, Long templateId);
}
