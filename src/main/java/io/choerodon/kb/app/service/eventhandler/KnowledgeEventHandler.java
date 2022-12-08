package io.choerodon.kb.app.service.eventhandler;

import java.io.*;
import java.util.ArrayList;
import java.util.Optional;

import com.alibaba.fastjson.JSONObject;
import com.yqcloud.core.oauth.ZKnowDetailsHelper;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.lang3.StringUtils;
import org.hzero.boot.file.dto.FileSimpleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import io.choerodon.asgard.saga.annotation.SagaTask;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.api.vo.KnowledgeBaseInfoVO;
import io.choerodon.kb.api.vo.PageCreateWithoutContentVO;
import io.choerodon.kb.api.vo.event.OrganizationCreateEventPayload;
import io.choerodon.kb.api.vo.event.ProjectEvent;
import io.choerodon.kb.api.vo.permission.OrganizationPermissionSettingVO;
import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.app.service.KnowledgeBaseService;
import io.choerodon.kb.app.service.WorkSpacePageService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.domain.entity.SecurityConfig;
import io.choerodon.kb.domain.repository.PermissionRangeKnowledgeBaseSettingRepository;
import io.choerodon.kb.domain.service.PermissionRangeKnowledgeBaseSettingService;
import io.choerodon.kb.infra.dto.PageDTO;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.dto.WorkSpacePageDTO;
import io.choerodon.kb.infra.enums.FileSourceType;
import io.choerodon.kb.infra.enums.OpenRangeType;
import io.choerodon.kb.infra.enums.PermissionConstants;
import io.choerodon.kb.infra.mapper.PageMapper;
import io.choerodon.kb.infra.mapper.WorkSpaceMapper;
import io.choerodon.kb.infra.utils.CommonUtil;
import io.choerodon.kb.infra.utils.FileUtil;

/**
 * @author zhaotianxin
 * @since 2020/1/13
 */
@Component
public class KnowledgeEventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(io.choerodon.kb.app.service.eventhandler.KnowledgeEventHandler.class);
    private static final String PROJECT_CREATE = "iam-create-project";
    private static final String TASK_PROJECT_CREATE = "kb-create-project";
    private static final String ORG_CREATE = "org-create-organization";
    private static final String TASK_ORG_CREATE = "kb-create-organization";
    private static final String KNOWLEDGE_UPLOAD_FILE = "knowledge-upload-file";
    private static final String KNOWLEDGE_UPLOAD_FILE_TASK = "knowledge-upload-file-task";
    private static final String MP4 = "mp4";

    @Value("${choerodon.file-server.upload.size-limit:1024}")
    private Long fileServerUploadSizeLimit;
    @Value("${choerodon.file-server.upload.prefix-strategy-code:CHOERODON-MINIO-FOLDER}")
    private String fileUploadPrefixStrategyCode;
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;
    @Autowired
    private WorkSpaceService workSpaceService;
    @Autowired
    private WorkSpaceMapper workSpaceMapper;
    @Autowired
    private WorkSpacePageService workSpacePageService;
    @Autowired
    private PermissionRangeKnowledgeBaseSettingService permissionRangeKnowledgeBaseSettingService;
    @Autowired
    private PermissionRangeKnowledgeBaseSettingRepository permissionRangeKnowledgeBaseSettingRepository;
    @Autowired
    private PageMapper pageMapper;

    @SagaTask(code = TASK_ORG_CREATE,
            description = "knowledge_base消费创建组织",
            sagaCode = ORG_CREATE, seq = 1)
    @Transactional(rollbackFor = Exception.class)
    public String handleOrganizationCreateByConsumeSagaTask(String data) {
        LOGGER.info("消费创建组织消息{}", data);
        OrganizationCreateEventPayload organizationEventPayload = JSONObject.parseObject(data, OrganizationCreateEventPayload.class);
        final Long organizationId = organizationEventPayload.getId();
        // 模拟用户
        DetailsHelper.setCustomUserDetails(organizationEventPayload.getUserId(), null);
        CustomUserDetails customUserDetails = DetailsHelper.getUserDetails();
        ZKnowDetailsHelper.setRequestSource(customUserDetails, ZKnowDetailsHelper.VALUE_CHOERODON);
        DetailsHelper.getUserDetails().setOrganizationId(organizationId);

        LOGGER.info("初始化组织设置默认权限");
        permissionRangeKnowledgeBaseSettingService.initPermissionRangeOnOrganizationCreate(organizationId);

        LOGGER.info("初始化默认知识库");
        // 默认知识库
        KnowledgeBaseInfoVO knowledgeBaseInfo = new KnowledgeBaseInfoVO()
                .setName(organizationEventPayload.getName())
                .setDescription("组织下默认知识库")
                .setOpenRange(OpenRangeType.RANGE_PRIVATE.getType())
                .setProjectId(null)
                .setOrganizationId(organizationId)
                // 默认知识库权限
                .setPermissionDetailVO(PermissionDetailVO.of(
                        PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_ORG.getCode(),
                        null,
                        // 数据库中存储的组织默认权限范围设置
                        Optional.ofNullable(this.permissionRangeKnowledgeBaseSettingRepository.queryOrgPermissionSetting(organizationId))
                                .map(OrganizationPermissionSettingVO::getOrganizationDefaultPermissionRange)
                                .orElse(new ArrayList<>()),
                        // 默认安全设置(硬编码)
                        SecurityConfig.generateDefaultSecurityConfig(
                                organizationId,
                                PermissionConstants.EMPTY_ID_PLACEHOLDER,
                                PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_ORG,
                                null
                )
        ));
        // 保存
        knowledgeBaseService.create(organizationId, null, knowledgeBaseInfo, true);
//        有继承的逻辑在, 这里是不是不用初始化了 gaokuo.dai@zknow.com 2022-10-08
//        LOGGER.info("初始化默认知识库文件夹权限");
        return data;
    }


    /**
     * 创建项目事件
     *
     * @param message message
     */
    @SagaTask(code = TASK_PROJECT_CREATE,
            description = "knowledge_base消费创建项目事件初始化项目数据",
            sagaCode = PROJECT_CREATE,
            seq = 2)
    public String handleProjectInitByConsumeSagaTask(String message) {
        LOGGER.info("消费创建项目消息{}", message);
        ProjectEvent projectEvent = JSONObject.parseObject(message, ProjectEvent.class);
        final Long organizationId = projectEvent.getOrganizationId();
        final Long projectId = projectEvent.getProjectId();
        // 模拟用户
        DetailsHelper.setCustomUserDetails(projectEvent.getUserId(), null);
        CustomUserDetails customUserDetails = DetailsHelper.getUserDetails();
        ZKnowDetailsHelper.setRequestSource(customUserDetails, ZKnowDetailsHelper.VALUE_CHOERODON);
        DetailsHelper.getUserDetails().setOrganizationId(organizationId);

        LOGGER.info("初始化默认知识库");
        // 默认知识库
        KnowledgeBaseInfoVO knowledgeBaseInfo = new KnowledgeBaseInfoVO()
                .setName(projectEvent.getProjectName())
                .setDescription("项目下默认知识库")
                .setOpenRange(OpenRangeType.RANGE_PRIVATE.getType())
                .setProjectId(projectId)
                .setOrganizationId(organizationId)
                // 默认知识库权限
                .setPermissionDetailVO(PermissionDetailVO.of(
                        PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_PROJECT.getCode(),
                        null,
                        // 数据库中存储的组织默认权限范围设置
                        Optional.ofNullable(this.permissionRangeKnowledgeBaseSettingRepository.queryOrgPermissionSetting(organizationId))
                                .map(OrganizationPermissionSettingVO::getProjectDefaultPermissionRange)
                                .orElse(new ArrayList<>()),
                        // 默认安全设置(硬编码)
                        SecurityConfig.generateDefaultSecurityConfig(
                                organizationId,
                                projectId,
                                PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_PROJECT,
                                null
                        )
                ));
        // 保存
        knowledgeBaseService.create(organizationId, projectId, knowledgeBaseInfo, true);

        return message;
    }

    @SagaTask(code = KNOWLEDGE_UPLOAD_FILE_TASK,
            description = "知识服务上传大文件",
            sagaCode = KNOWLEDGE_UPLOAD_FILE,
            seq = 2)
    public String knowledgeUploadFile(String message) {
        PageCreateWithoutContentVO pageCreateWithoutContentVO = JSONObject.parseObject(message, PageCreateWithoutContentVO.class);
        upLoadFileServer(pageCreateWithoutContentVO.getOrganizationId(), pageCreateWithoutContentVO);
        //workSpaceId
        WorkSpaceDTO spaceDTO = workSpaceMapper.selectByPrimaryKey(pageCreateWithoutContentVO.getRefId());
        if (spaceDTO == null) {
            throw new CommonException("error.work.space.id.is.null");
        }
        if (StringUtils.isBlank(pageCreateWithoutContentVO.getFileKey())) {
            throw new CommonException("error.upload.file.key.is.null");
        }

        //回写title  fileKey
        spaceDTO.setFileKey(pageCreateWithoutContentVO.getFileKey());
        spaceDTO.setName(CommonUtil.getFileName(pageCreateWithoutContentVO.getFileKey()));
        workSpaceMapper.updateByPrimaryKey(spaceDTO);

        WorkSpacePageDTO spacePageDTO = workSpacePageService.selectByWorkSpaceId(spaceDTO.getId());
        PageDTO pageDTO = pageMapper.selectByPrimaryKey(spacePageDTO.getPageId());
        pageDTO.setTitle(CommonUtil.getFileName(pageCreateWithoutContentVO.getFileKey()));
        pageMapper.updateByPrimaryKey(pageDTO);
        return message;
    }


    private void upLoadFileServer(Long organizationId, PageCreateWithoutContentVO createVO) {
        if (org.apache.commons.lang3.StringUtils.equalsIgnoreCase(createVO.getFileSourceType(), FileSourceType.UPLOAD.getFileSourceType())) {
            //如果是上传的需要读文件
            File file = new File(createVO.getFilePath());
            try (InputStream inputStream = file != null ? new FileInputStream(file) : null;) {
                MultipartFile multipartFile = getMultipartFile(inputStream, createVO.getTitle());
                //校验文件的大小
                checkFileSize(FileUtil.StorageUnit.MB, multipartFile.getSize(), fileServerUploadSizeLimit);
                //大文件上传指定编码，使用目录前缀匹配,
                String fileType = CommonUtil.getFileType(multipartFile.getOriginalFilename());
                FileSimpleDTO fileSimpleDTO = null;
                LOGGER.info(">>>>>>>>>>>>>>>>fileType:{},fileUploadPrefixStrategyCode:{}", fileType, fileUploadPrefixStrategyCode);
                if (StringUtils.equalsIgnoreCase(fileType, MP4)) {
                    fileSimpleDTO = workSpaceService.uploadMultipartFileWithMD5(organizationId, null, createVO.getTitle(), null, fileUploadPrefixStrategyCode, multipartFile);
                } else {
                    fileSimpleDTO = workSpaceService.uploadMultipartFileWithMD5(organizationId, null, createVO.getTitle(), null, null, multipartFile);
                }
//                handFileKey(fileSimpleDTO);
                createVO.setFileKey(fileSimpleDTO.getFileKey());
            } catch (Exception e) {
                file.delete();
                throw new CommonException(e);
            } finally {
                file.delete();
            }
        }
    }


    public MultipartFile getMultipartFile(InputStream inputStream, String fileName) {
        FileItem fileItem = createFileItem(inputStream, fileName);
        //CommonsMultipartFile是feign对multipartFile的封装，但是要FileItem类对象
        return new CommonsMultipartFile(fileItem);
    }

    private void checkFileSize(String unit, Long fileSize, Long size) {
        if (FileUtil.StorageUnit.MB.equals(unit) && fileSize > size * FileUtil.ENTERING * FileUtil.ENTERING) {
            throw new CommonException(FileUtil.ERROR_FILE_SIZE, size + unit);
        } else if (FileUtil.StorageUnit.KB.equals(unit) && fileSize > size * FileUtil.ENTERING) {
            throw new CommonException(FileUtil.ERROR_FILE_SIZE, size + unit);
        }
    }

    public FileItem createFileItem(InputStream inputStream, String fileName) {
        FileItemFactory factory = new DiskFileItemFactory(16, null);
        String textFieldName = "file";
        //contentType为multipart/form-data  minio报400
        FileItem item = factory.createItem(textFieldName, MediaType.APPLICATION_OCTET_STREAM_VALUE, true, fileName);
        int bytesRead = 0;
        byte[] buffer = new byte[8192];
        OutputStream os = null;
        //使用输出流输出输入流的字节
        try {
            os = item.getOutputStream();
            while ((bytesRead = inputStream.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            inputStream.close();
        } catch (IOException e) {
            LOGGER.error("Stream copy exception", e);
            throw new IllegalArgumentException("文件上传失败");
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    LOGGER.error("Stream close exception", e);

                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.error("Stream close exception", e);
                }
            }
        }

        return item;
    }


}
