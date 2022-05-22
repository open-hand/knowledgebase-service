package io.choerodon.kb.app.service.impl;

import java.util.Arrays;
import org.hzero.boot.file.dto.FileSimpleDTO;
import org.hzero.core.base.BaseConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.feign.vo.FileVO;
import io.choerodon.kb.infra.mapper.WorkSpaceMapper;
import io.choerodon.kb.infra.utils.ExpandFileClient;
import io.choerodon.onlyoffice.service.impl.AbstractOnlyOfficeFileHandler;
import io.choerodon.onlyoffice.vo.DocumentEditCallback;

/**
 * Created by wangxiang on 2022/5/11
 */
@Service
public class OnlyOfficeFileHandler extends AbstractOnlyOfficeFileHandler {
    private Logger LOGGER = LoggerFactory.getLogger(OnlyOfficeFileHandler.class);

    @Autowired
    private WorkSpaceMapper workSpaceMapper;

    @Autowired
    private ExpandFileClient expandFileClient;


    @Override
    @Transactional(rollbackFor = Exception.class)
    protected void fileBusinessProcess(MultipartFile multipartFile, DocumentEditCallback documentEditCallback) {
        synchronized (this) {
            //上传文件
            WorkSpaceDTO spaceDTO = workSpaceMapper.selectByPrimaryKey(documentEditCallback.getBusinessId());
            if (spaceDTO == null) {
                LOGGER.error("workspace where onlyOffice was saved does not exist");
            }

            FileSimpleDTO fileSimpleDTO = expandFileClient.uploadFileWithMD5(spaceDTO.getOrganizationId(), BaseStage.BACKETNAME, null, spaceDTO.getName(), multipartFile);
            FileVO fileDTOByFileKey = expandFileClient.getFileDTOByFileKey(spaceDTO.getOrganizationId(), spaceDTO.getFileKey());
            //删除旧的
            expandFileClient.deleteFileByUrlWithDbOptional(spaceDTO.getOrganizationId(), BaseStage.BACKETNAME, Arrays.asList(fileDTOByFileKey.getFileUrl()));
            //修改workSpace
            spaceDTO.setFileKey(fileSimpleDTO.getFileKey());

            // 设置用户上下文
            CustomUserDetails customUserDetails = new CustomUserDetails("default", "default");
            if (documentEditCallback == null) {
                customUserDetails.setUserId(BaseConstants.ANONYMOUS_USER_ID);
            } else {
                customUserDetails.setUserId(documentEditCallback.getUserId());

            }
            customUserDetails.setOrganizationId(BaseConstants.DEFAULT_TENANT_ID);
            customUserDetails.setLanguage(BaseConstants.DEFAULT_LOCALE_STR);

            DetailsHelper.setCustomUserDetails(customUserDetails);
            workSpaceMapper.updateByPrimaryKey(spaceDTO);

        }
    }
}
