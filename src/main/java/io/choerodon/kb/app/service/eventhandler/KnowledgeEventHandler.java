package io.choerodon.kb.app.service.eventhandler;

import java.io.*;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import io.choerodon.asgard.saga.annotation.SagaTask;
import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.vo.PageCreateWithoutContentVO;
import io.choerodon.kb.api.vo.event.OrganizationCreateEventPayload;
import io.choerodon.kb.api.vo.event.ProjectEvent;
import io.choerodon.kb.app.service.KnowledgeBaseService;
import io.choerodon.kb.app.service.PermissionRangeService;
import io.choerodon.kb.app.service.WorkSpacePageService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.infra.dto.KnowledgeBaseDTO;
import io.choerodon.kb.infra.dto.PageDTO;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.dto.WorkSpacePageDTO;
import io.choerodon.kb.infra.enums.FileSourceType;
import io.choerodon.kb.infra.mapper.PageMapper;
import io.choerodon.kb.infra.mapper.WorkSpaceMapper;
import io.choerodon.kb.infra.utils.CommonUtil;
import io.choerodon.kb.infra.utils.FileUtil;

import org.hzero.boot.file.dto.FileSimpleDTO;

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
    private PermissionRangeService permissionRangeService;

    @Autowired
    private PageMapper pageMapper;


    @SagaTask(code = TASK_ORG_CREATE,
            description = "knowledge_base消费创建组织",
            sagaCode = ORG_CREATE, seq = 1)
    public String handleOrganizationCreateByConsumeSagaTask(String data) {
        LOGGER.info("消费创建组织消息{}", data);
        OrganizationCreateEventPayload organizationEventPayload = JSONObject.parseObject(data, OrganizationCreateEventPayload.class);
        LOGGER.info("初始化默认知识库");
        KnowledgeBaseDTO knowledgeBaseDTO = new KnowledgeBaseDTO(organizationEventPayload.getName(), "组织下默认知识库", "range_private", null, organizationEventPayload.getId());
        knowledgeBaseDTO.setCreatedBy(organizationEventPayload.getUserId());
        knowledgeBaseDTO.setLastUpdatedBy(organizationEventPayload.getUserId());
        KnowledgeBaseDTO baseDTO = knowledgeBaseService.baseInsert(knowledgeBaseDTO);
        LOGGER.info("初始化默认文件夹");
        knowledgeBaseService.createDefaultFolder(baseDTO.getOrganizationId(), baseDTO.getProjectId(), baseDTO);
        LOGGER.info("初始化默认权限");
        permissionRangeService.initOrgPermissionRange(organizationEventPayload.getId());
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
        ProjectEvent projectEvent = JSONObject.parseObject(message, ProjectEvent.class);
        KnowledgeBaseDTO knowledgeBaseDTO = new KnowledgeBaseDTO(projectEvent.getProjectName(), "项目下默认知识库", "range_private", projectEvent.getProjectId(), projectEvent.getOrganizationId());
        knowledgeBaseDTO.setCreatedBy(projectEvent.getUserId());
        knowledgeBaseDTO.setLastUpdatedBy(projectEvent.getUserId());
        KnowledgeBaseDTO baseDTO = knowledgeBaseService.baseInsert(knowledgeBaseDTO);
        knowledgeBaseService.createDefaultFolder(baseDTO.getOrganizationId(), baseDTO.getProjectId(), baseDTO);
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
