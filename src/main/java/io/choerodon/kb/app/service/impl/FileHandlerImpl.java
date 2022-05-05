package io.choerodon.kb.app.service.impl;

import com.yqcloud.wps.dto.WpsFileDTO;
import com.yqcloud.wps.dto.WpsFileVersionDTO;
import com.yqcloud.wps.dto.WpsUserDTO;
import com.yqcloud.wps.maskant.adaptor.WPSFileAdaptor;
import com.yqcloud.wps.service.impl.AbstractFileHandler;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.kb.infra.dto.WorkSpaceDTO;
import io.choerodon.kb.infra.feign.vo.FileVO;
import io.choerodon.kb.infra.mapper.WorkSpaceMapper;
import io.choerodon.kb.infra.utils.ExpandFileClient;

/**
 * Created by wangxiang on 2022/5/5
 */
@Service
public class FileHandlerImpl extends AbstractFileHandler {


    @Autowired
    private ExpandFileClient expandFileClient;
    @Autowired
    private WorkSpaceMapper workSpaceMapper;
    @Autowired
    private WPSFileAdaptor wpsFileAdaptor;

    @Override
    protected void createFile() {

    }

    @Override
    public WpsFileDTO queryFileByFileKey(String fileKey, String fileSourceType, String sourceId) {
        WorkSpaceDTO spaceDTO = new WorkSpaceDTO();
        spaceDTO.setFileKey(fileKey);
        WorkSpaceDTO workSpaceDTO = workSpaceMapper.selectOne(spaceDTO);
        FileVO fileDTOByFileKey = expandFileClient.getFileDTOByFileKey(workSpaceDTO.getOrganizationId(), fileKey);
        WpsFileDTO wpsFileDTO = new WpsFileDTO();
        wpsFileDTO.setFileName(wpsFileAdaptor.getFileName(fileKey));
        wpsFileDTO.setFileKey(fileKey);
        wpsFileDTO.setVersion(0);
        wpsFileDTO.setCreate_time(fileDTOByFileKey.getCreationDate());
        wpsFileDTO.setCreator(String.valueOf(fileDTOByFileKey.getCreatedBy()));
        wpsFileDTO.setModifier(String.valueOf(fileDTOByFileKey.getLastUpdatedBy()));
        wpsFileDTO.setModify_time(fileDTOByFileKey.getLastUpdateDate());
        wpsFileDTO.setFileUrl(fileDTOByFileKey.getFileUrl());
        return wpsFileDTO;
    }

    @Override
    public WpsFileVersionDTO saveCurrentVersion(String fileName, String fileId, String fileKey, MultipartFile mFile, String bucketName, WpsUserDTO userInfo) {
        return null;
    }

    @Override
    public void rename(String fileKey, String userId, String newFileName) {

    }

    @Override
    public List<WpsFileVersionDTO> findAllByFileId(String fileId, Integer count, Integer offset) {
        return null;
    }

    @Override
    public WpsFileVersionDTO findFileVersionByVersionAndFileId(Integer version, String fileId) {
        return null;
    }
}
