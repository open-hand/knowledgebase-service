package io.choerodon.kb.app.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.choerodon.kb.api.dao.*;
import io.choerodon.kb.app.service.PageAttachmentService;
import io.choerodon.kb.app.service.PageService;
import io.choerodon.kb.app.service.WikiMigrationService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.domain.kb.repository.IamRepository;
import io.choerodon.kb.domain.kb.repository.WorkSpacePageRepository;
import io.choerodon.kb.domain.service.IWikiPageService;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.common.enums.PageResourceType;
import io.choerodon.kb.infra.dto.MigrationDTO;
import io.choerodon.kb.infra.dto.PageAttachmentDTO;
import io.choerodon.kb.infra.dto.PageDTO;
import io.choerodon.kb.infra.dto.iam.OrganizationDO;
import io.choerodon.kb.infra.dto.iam.ProjectDO;
import io.choerodon.kb.infra.dto.iam.UserDO;
import io.choerodon.kb.infra.feign.UserFeignClient;
import io.choerodon.kb.infra.mapper.PageMapper;
import io.choerodon.kb.infra.utils.FileUtil;
import org.apache.commons.io.IOUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by Zenger on 2019/6/14.
 */
@Service
public class WikiMigrationServiceImpl implements WikiMigrationService {

    private static final Logger LOGGER = getLogger(WikiMigrationServiceImpl.class);
    private static Gson gson = new Gson();

    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private IWikiPageService iWikiPageService;
    @Autowired
    private WorkSpaceService workSpaceService;
    @Autowired
    private PageAttachmentService pageAttachmentService;
    @Autowired
    private PageService pageService;
    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private PageMapper pageMapper;
    @Autowired
    private WorkSpacePageRepository workSpacePageRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Async("xwiki-sync")
    @Override
    public void controllerMigration() {
        migration();
    }

    @Override
    public void migration() {
        List<OrganizationDO> organizationDOList = iamRepository.pageByOrganization(0, 0);
        organizationDOList = organizationDOList.stream().sorted(Comparator.comparing(OrganizationDO::getId)).collect(Collectors.toList());
        for (OrganizationDO organizationDO : organizationDOList) {
            try {
                if (organizationDO.getEnabled()) {
                    MigrationDTO migrationDTO = new MigrationDTO();
                    migrationDTO.setReference(BaseStage.O + organizationDO.getName());
                    migrationDTO.setType(PageResourceType.ORGANIZATION.getResourceType());
                    wikiDataMigration(migrationDTO, organizationDO.getId(), PageResourceType.ORGANIZATION.getResourceType());

                    if (organizationDO.getProjectCount() > 0) {
                        List<ProjectDO> projectEList = iamRepository.pageByProject(organizationDO.getId());
                        projectEList = projectEList.stream().sorted(Comparator.comparing(ProjectDO::getId)).collect(Collectors.toList());
                        for (ProjectDO project : projectEList) {
                            if (project.getEnabled()) {
                                MigrationDTO migration = new MigrationDTO();
                                migration.setReference(BaseStage.O + organizationDO.getName() + "." + BaseStage.P + project.getName());
                                migration.setType(PageResourceType.PROJECT.getResourceType());
                                wikiDataMigration(migration, project.getId(), PageResourceType.PROJECT.getResourceType());
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
    public void levelMigration(MigrationVO migrationVO, Long resourceId, String type) {
        MigrationDTO migrationDTO = new MigrationDTO();
        if (PageResourceType.ORGANIZATION.getResourceType().equals(type)) {
            if (migrationVO.getData() != null && !migrationVO.getData().isEmpty()) {
                migrationDTO.setReference(migrationVO.getData());
                migrationDTO.setType(type);
            } else {
                OrganizationDO organizationDO = iamRepository.queryOrganizationById(resourceId);
                LOGGER.info("organization info:{}", organizationDO.toString());
                migrationDTO.setReference(BaseStage.O + organizationDO.getName());
                migrationDTO.setType(type);
            }
        } else {
            if (migrationVO.getData() != null && !migrationVO.getData().isEmpty()) {
                migrationDTO.setReference(migrationVO.getData());
                migrationDTO.setType(BaseStage.APPOINT);
            } else {
                ProjectDO projectDO = iamRepository.queryIamProject(resourceId);
                LOGGER.info("project info:{}", projectDO.toString());
                OrganizationDO organizationDO = iamRepository.queryOrganizationById(projectDO.getOrganizationId());
                LOGGER.info("organization info:{}", organizationDO.toString());
                migrationDTO.setReference(BaseStage.O + organizationDO.getName() + "." + BaseStage.P + projectDO.getName());
                migrationDTO.setType(type);
            }
        }

        wikiDataMigration(migrationDTO, resourceId, type);
    }

    private void wikiDataMigration(MigrationDTO migrationDTO, Long resourceId, String type) {

        String data = iWikiPageService.getWikiPageMigration(migrationDTO);
        Map<String, WikiPageInfoVO> map = gson.fromJson(data,
                new TypeToken<Map<String, WikiPageInfoVO>>() {
                }.getType());
        Map<String, UserDO> userMap = handleUserData(map);
        WikiPageInfoVO wikiPageInfo = map.get("top");
        if (wikiPageInfo != null && wikiPageInfo.getTitle() != null && !"".equals(wikiPageInfo.getTitle().trim())) {
            PageCreateVO pageCreateVO = new PageCreateVO();
            pageCreateVO.setParentWorkspaceId(0L);
            pageCreateVO.setTitle(wikiPageInfo.getTitle());
            pageCreateVO.setContent(wikiPageInfo.getContent());
            PageVO parentPage = pageService.createPage(resourceId, pageCreateVO, type);
            parentPage = replaceContentImageFormat(wikiPageInfo, parentPage, resourceId, type);
            updateBaseData(parentPage.getPageInfo().getId(), wikiPageInfo, userMap);
            if (wikiPageInfo.getHasChildren()) {
                hasChildWikiPage(wikiPageInfo.getChildren(), map, parentPage.getWorkSpace().getId(), resourceId, type, userMap);
            }
        }
    }

    private Map<String, UserDO> handleUserData(Map<String, WikiPageInfoVO> map) {
        Set<String> createLoginNames = map.entrySet().stream().map(x -> x.getValue().getCreateLoginName()).collect(Collectors.toSet());
        Set<String> updateLoginNames = map.entrySet().stream().map(x -> x.getValue().getCreateLoginName()).collect(Collectors.toSet());
        createLoginNames.addAll(updateLoginNames);
        List<UserDO> userDOList = userFeignClient.listUsersByLogins(createLoginNames.toArray(new String[createLoginNames.size()]), false).getBody();
        return userDOList.stream().collect(Collectors.toMap(UserDO::getLoginName, x -> x));
    }

    /**
     * 迁移的数据，更新基础字段数据
     *
     * @param pageId
     * @param wikiPageInfo
     * @param userMap
     */
    private void updateBaseData(Long pageId, WikiPageInfoVO wikiPageInfo, Map<String, UserDO> userMap) {
        UserDO createUser = userMap.get(wikiPageInfo.getCreateLoginName());
        UserDO updateUser = userMap.get(wikiPageInfo.getUpdateLoginName());
        PageDTO base = new PageDTO();
        base.setCreatedBy(createUser != null ? createUser.getId() : 1L);
        base.setCreationDate(wikiPageInfo.getCreationDate());
        base.setLastUpdatedBy(updateUser != null ? updateUser.getId() : 1L);
        base.setLastUpdateDate(wikiPageInfo.getUpdateDate());
        pageMapper.updateBaseData(pageId, base);
    }

    private void hasChildWikiPage(List<String> wikiPages,
                                  Map<String, WikiPageInfoVO> map,
                                  Long parentWorkSpaceId,
                                  Long resourceId,
                                  String type,
                                  Map<String, UserDO> userMap) {
        for (String child : wikiPages) {
            WikiPageInfoVO childWikiPageInfo = map.get(child);
            if (childWikiPageInfo != null && childWikiPageInfo.getTitle() != null && !"".equals(childWikiPageInfo.getTitle().trim())) {
                PageCreateVO childCreatePage = new PageCreateVO();
                childCreatePage.setParentWorkspaceId(parentWorkSpaceId);
                childCreatePage.setTitle(childWikiPageInfo.getTitle());
                childCreatePage.setContent(childWikiPageInfo.getContent());
                PageVO childPage = pageService.createPage(resourceId, childCreatePage, type);
                childPage = replaceContentImageFormat(childWikiPageInfo, childPage, resourceId, type);
                updateBaseData(childPage.getPageInfo().getId(), childWikiPageInfo, userMap);
                if (childWikiPageInfo.getHasChildren()) {
                    hasChildWikiPage(childWikiPageInfo.getChildren(), map, childPage.getWorkSpace().getId(), resourceId, type, userMap);
                }
            }
        }
    }

    private PageVO replaceContentImageFormat(WikiPageInfoVO wikiPageInfo,
                                             PageVO parentPage,
                                             Long resourceId,
                                             String type) {
        List<PageAttachmentDTO> pageAttachmentDTOList = new ArrayList<>();
        if (wikiPageInfo.getHasAttachment()) {
            String data = iWikiPageService.getWikiPageAttachment(wikiPageInfo.getDocId());

            List<WikiPageAttachmentVO> attachmentVOList = gson.fromJson(data,
                    new TypeToken<List<WikiPageAttachmentVO>>() {
                    }.getType());

            if (attachmentVOList != null && !attachmentVOList.isEmpty()) {
                for (WikiPageAttachmentVO attachment : attachmentVOList) {
                    pageAttachmentDTOList.add(pageAttachmentService.insertPageAttachment(attachment.getName(),
                            parentPage.getPageInfo().getId(),
                            attachment.getSize(),
                            pageAttachmentService.dealUrl(attachment.getUrl())));
                }

                if (parentPage.getPageInfo().getSouceContent().contains("![[")) {
                    Map<String, String> params = new HashMap<>();
                    attachmentVOList.stream().forEach(attach -> {
                        params.put("![[" + attach.getName() + "|" + attach.getName() + "]]",
                                "![" + attach.getName() + "](" + attach.getUrl() + ")");
                    });
                    String content = FileUtil.replaceReturnString(IOUtils.toInputStream(parentPage.getPageInfo().getSouceContent()), params);
                    PageUpdateVO pageUpdateVO = new PageUpdateVO();
                    pageUpdateVO.setContent(content);
                    pageUpdateVO.setMinorEdit(false);
                    pageUpdateVO.setObjectVersionNumber(parentPage.getObjectVersionNumber());
                    return workSpaceService.update(resourceId, parentPage.getWorkSpace().getId(), pageUpdateVO, type);
                }
            }
        }

        return parentPage;
    }

    @Async("xwiki-sync")
    @Override
    public void controllerMigrationFix() {
        LOGGER.info("开始修复基础数据");
        List<OrganizationDO> organizationDOList = iamRepository.pageByOrganization(0, 0);
        organizationDOList = organizationDOList.stream().sorted(Comparator.comparing(OrganizationDO::getId)).collect(Collectors.toList());
        for (OrganizationDO organizationDO : organizationDOList) {
            try {
                if (organizationDO.getEnabled()) {
                    MigrationDTO migrationDTO = new MigrationDTO();
                    migrationDTO.setReference(BaseStage.O + organizationDO.getName());
                    migrationDTO.setType(PageResourceType.ORGANIZATION.getResourceType());
                    wikiDataMigrationFix(migrationDTO, organizationDO.getId(), null);

                    if (organizationDO.getProjectCount() > 0) {
                        List<ProjectDO> projectEList = iamRepository.pageByProject(organizationDO.getId());
                        projectEList = projectEList.stream().sorted(Comparator.comparing(ProjectDO::getId)).collect(Collectors.toList());
                        for (ProjectDO project : projectEList) {
                            if (project.getEnabled()) {
                                MigrationDTO migration = new MigrationDTO();
                                migration.setReference(BaseStage.O + organizationDO.getName() + "." + BaseStage.P + project.getName());
                                migration.setType(PageResourceType.PROJECT.getResourceType());
                                wikiDataMigrationFix(migration, organizationDO.getId(), project.getId());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error(String.valueOf(e));
            }
        }
        LOGGER.info("完成修复基础数据");
    }

    private void wikiDataMigrationFix(MigrationDTO migrationDTO, Long organizationId, Long projectId) {

        String data = iWikiPageService.getWikiPageMigration(migrationDTO);
        Map<String, WikiPageInfoVO> map = gson.fromJson(data,
                new TypeToken<Map<String, WikiPageInfoVO>>() {
                }.getType());
        Map<String, UserDO> userMap = handleUserData(map);
        for (Map.Entry<String, WikiPageInfoVO> entrySet : map.entrySet()) {
            WikiPageInfoVO wikiPageInfo = entrySet.getValue();
            if (wikiPageInfo != null && wikiPageInfo.getTitle() != null && !"".equals(wikiPageInfo.getTitle().trim())) {
                PageDTO select = new PageDTO();
                select.setTitle(wikiPageInfo.getTitle());
                select.setOrganizationId(organizationId);
                select.setProjectId(projectId);
                List<PageDTO> list = pageMapper.select(select);
                if (!list.isEmpty()) {
                    PageDTO parentPage = list.get(0);
                    updateBaseData(parentPage.getId(), wikiPageInfo, userMap);
                    LOGGER.info("修复文章orgId:{},proId:{},title:{},pageId:{}的基础字段信息", organizationId, projectId, wikiPageInfo.getTitle(), parentPage.getId());
                }
            }
        }
    }
}
