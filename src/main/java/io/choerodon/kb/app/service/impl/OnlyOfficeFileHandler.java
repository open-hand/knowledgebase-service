package io.choerodon.kb.app.service.impl;

import java.util.Arrays;
import org.hzero.boot.file.dto.FileSimpleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.kb.api.vo.PageCreateWithoutContentVO;
import io.choerodon.kb.api.vo.WorkSpaceInfoVO;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.enums.WorkSpaceType;
import io.choerodon.kb.infra.feign.vo.FileVO;
import io.choerodon.kb.infra.mapper.WorkSpaceMapper;
import io.choerodon.kb.infra.utils.ExpandFileClient;
import io.choerodon.onlyoffice.service.impl.AbstractOnlyOfficeFileHandler;

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

    @Autowired
    private WorkSpaceService workSpaceService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    protected void fileBusinessProcess(MultipartFile multipartFile, Long businessId) {
        //上传文件
        WorkSpaceDTO spaceDTO = workSpaceMapper.selectByPrimaryKey(businessId);
        if (spaceDTO == null) {
            LOGGER.error("workspace where onlyOffice was saved does not exist");
        }

        FileSimpleDTO fileSimpleDTO = expandFileClient.uploadFileWithMD5(spaceDTO.getOrganizationId(), BaseStage.BACKETNAME, null, spaceDTO.getName(), multipartFile);
        FileVO fileDTOByFileKey = expandFileClient.getFileDTOByFileKey(spaceDTO.getOrganizationId(), spaceDTO.getFileKey());
        //删除旧的
        expandFileClient.deleteFileByUrlWithDbOptional(spaceDTO.getOrganizationId(), BaseStage.BACKETNAME, Arrays.asList(fileDTOByFileKey.getFileUrl()));
        //修改workSpace
        spaceDTO.setFileKey(fileSimpleDTO.getFileKey());
        workSpaceMapper.updateByPrimaryKey(spaceDTO);
    }
}
