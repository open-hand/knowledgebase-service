package io.choerodon.kb.app.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.choerodon.kb.api.dao.*;
import io.choerodon.kb.app.service.PageAttachmentService;
import io.choerodon.kb.app.service.WikiMigrationService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.domain.kb.repository.IamRepository;
import io.choerodon.kb.domain.service.IWikiPageService;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.common.enums.PageResourceType;
import io.choerodon.kb.infra.common.utils.FileUtil;
import io.choerodon.kb.infra.dataobject.MigrationDO;
import io.choerodon.kb.infra.dataobject.PageAttachmentDO;
import io.choerodon.kb.infra.dataobject.iam.OrganizationDO;
import io.choerodon.kb.infra.dataobject.iam.ProjectDO;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Zenger on 2019/6/14.
 */
@Service
public class WikiMigrationServiceImpl implements WikiMigrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WikiMigrationServiceImpl.class);
    private static Gson gson = new Gson();

    private IamRepository iamRepository;
    private IWikiPageService iWikiPageService;
    private WorkSpaceService workSpaceService;
    private PageAttachmentService pageAttachmentService;

    public WikiMigrationServiceImpl(IamRepository iamRepository,
                                    IWikiPageService iWikiPageService,
                                    WorkSpaceService workSpaceService,
                                    PageAttachmentService pageAttachmentService) {
        this.iamRepository = iamRepository;
        this.iWikiPageService = iWikiPageService;
        this.workSpaceService = workSpaceService;
        this.pageAttachmentService = pageAttachmentService;
    }

    @Override
    @Async("xwiki-sync")
    public void migration() {
        List<OrganizationDO> organizationDOList = iamRepository.pageByOrganization(0, 0);
        for (OrganizationDO organizationDO : organizationDOList) {
            try {
                if (organizationDO.getEnabled()) {
                    MigrationDO migrationDO = new MigrationDO();
                    migrationDO.setReference(BaseStage.O + organizationDO.getName());
                    migrationDO.setType(PageResourceType.ORGANIZATION.getResourceType());
                    wikiDataMigration(migrationDO, organizationDO.getId(), PageResourceType.ORGANIZATION.getResourceType());

                    if (organizationDO.getProjectCount() > 0) {
                        List<ProjectDO> projectEList = iamRepository.pageByProject(organizationDO.getId());
                        for (ProjectDO project : projectEList) {
                            if (project.getEnabled()) {
                                MigrationDO migration = new MigrationDO();
                                migration.setReference(BaseStage.O + organizationDO.getName() + "." + BaseStage.P + project.getName());
                                migration.setType(PageResourceType.PROJECT.getResourceType());
                                wikiDataMigration(migrationDO, project.getId(), PageResourceType.PROJECT.getResourceType());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error(String.valueOf(e));
            }
        }
    }

    @Override
    @Async("xwiki-sync")
    public void levelMigration(MigrationDTO migrationDTO, Long resourceId, String type) {
        MigrationDO migrationDO = new MigrationDO();
        if (PageResourceType.ORGANIZATION.getResourceType().equals(type)) {
            if (migrationDTO.getData() != null && !migrationDTO.getData().isEmpty()) {
                migrationDO.setReference(migrationDTO.getData());
                migrationDO.setType(type);
            } else {
                OrganizationDO organizationDO = iamRepository.queryOrganizationById(resourceId);
                LOGGER.info("organization info:{}", organizationDO.toString());
                migrationDO.setReference(BaseStage.O + organizationDO.getName());
                migrationDO.setType(type);
            }
        } else {
            if (migrationDTO.getData() != null && !migrationDTO.getData().isEmpty()) {
                migrationDO.setReference(migrationDTO.getData());
                migrationDO.setType(BaseStage.APPOINT);
            } else {
                ProjectDO projectDO = iamRepository.queryIamProject(resourceId);
                LOGGER.info("project info:{}", projectDO.toString());
                OrganizationDO organizationDO = iamRepository.queryOrganizationById(projectDO.getOrganizationId());
                LOGGER.info("organization info:{}", organizationDO.toString());
                migrationDO.setReference(BaseStage.O + organizationDO.getName() + "." + BaseStage.P + projectDO.getName());
                migrationDO.setType(type);
            }
        }

        wikiDataMigration(migrationDO, resourceId, type);
    }

    private void wikiDataMigration(MigrationDO migrationDO, Long resourceId, String type) {
        String data = iWikiPageService.getWikiPageMigration(migrationDO);
        Map<String, WikiPageInfoDTO> map = gson.fromJson(data,
                new TypeToken<Map<String, WikiPageInfoDTO>>() {
                }.getType());
        WikiPageInfoDTO wikiPageInfo = map.get("top");
        if (wikiPageInfo != null) {
            PageCreateDTO pageCreateDTO = new PageCreateDTO();
            pageCreateDTO.setParentWorkspaceId(0L);
            pageCreateDTO.setTitle(wikiPageInfo.getTitle());
            pageCreateDTO.setContent(wikiPageInfo.getContent());
            PageDTO parentPage = workSpaceService.create(resourceId, pageCreateDTO, type);
            parentPage = replaceContentImageFormat(wikiPageInfo, parentPage, resourceId, type);
            if (wikiPageInfo.getHasChildren()) {
                hasChildWikiPage(wikiPageInfo.getChildren(), map, parentPage.getWorkSpace().getId(), resourceId, type);
            }
        }
    }

    private void hasChildWikiPage(List<String> wikiPages,
                                  Map<String, WikiPageInfoDTO> map,
                                  Long parentWorkSpaceId,
                                  Long resourceId,
                                  String type) {
        for (String child : wikiPages) {
            WikiPageInfoDTO childWikiPageInfo = map.get(child);
            if (childWikiPageInfo != null) {
                PageCreateDTO childCreatePage = new PageCreateDTO();
                childCreatePage.setParentWorkspaceId(parentWorkSpaceId);
                childCreatePage.setTitle(childWikiPageInfo.getTitle());
                childCreatePage.setContent(childWikiPageInfo.getContent());
                PageDTO childPage = workSpaceService.create(resourceId, childCreatePage, type);
                childPage = replaceContentImageFormat(childWikiPageInfo, childPage, resourceId, type);
                if (childWikiPageInfo.getHasChildren()) {
                    hasChildWikiPage(childWikiPageInfo.getChildren(), map, childPage.getWorkSpace().getId(), resourceId, type);
                }
            }
        }
    }

    private PageDTO replaceContentImageFormat(WikiPageInfoDTO wikiPageInfo,
                                              PageDTO parentPage,
                                              Long resourceId,
                                              String type) {
        List<PageAttachmentDO> pageAttachmentDOList = new ArrayList<>();
        if (wikiPageInfo.getHasAttachment()) {
            String data = iWikiPageService.getWikiPageAttachment(wikiPageInfo.getDocId());

            List<WikiPageAttachmentDTO> attachmentDTOList = gson.fromJson(data,
                    new TypeToken<List<WikiPageAttachmentDTO>>() {
                    }.getType());

            if (attachmentDTOList != null && !attachmentDTOList.isEmpty()) {
                for (WikiPageAttachmentDTO attachment : attachmentDTOList) {
                    pageAttachmentDOList.add(pageAttachmentService.insertPageAttachment(attachment.getName(),
                            parentPage.getPageInfo().getId(),
                            attachment.getSize(),
                            pageAttachmentService.dealUrl(attachment.getUrl())));
                }

                if (parentPage.getPageInfo().getSouceContent().contains("![[")) {
                    Map<String, String> params = new HashMap<>();
                    attachmentDTOList.stream().forEach(attach -> {
                        params.put("![[" + attach.getName() + "|" + attach.getName() + "]]",
                                "![" + attach.getName() + "](" + attach.getUrl() + ")");
                    });
                    String content = FileUtil.replaceReturnString(IOUtils.toInputStream(parentPage.getPageInfo().getSouceContent()), params);
                    PageUpdateDTO pageUpdateDTO = new PageUpdateDTO();
                    pageUpdateDTO.setContent(content);
                    pageUpdateDTO.setMinorEdit(false);
                    pageUpdateDTO.setObjectVersionNumber(parentPage.getObjectVersionNumber());
                    return workSpaceService.update(resourceId, parentPage.getWorkSpace().getId(), pageUpdateDTO, type);
                }
            }
        }

        return parentPage;
    }
}
