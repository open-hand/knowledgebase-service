package io.choerodon.kb.infra.utils;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import io.choerodon.kb.infra.feign.CustomFileRemoteService;
import io.choerodon.kb.infra.feign.vo.FileVO;

import org.hzero.boot.autoconfigure.file.BootFileConfigProperties;
import org.hzero.boot.file.FileClient;
import org.hzero.boot.file.feign.FileRemoteService;
import org.hzero.core.util.ResponseUtils;

/**
 * @author superlee
 * @since 2020-05-25
 */
public class ExpandFileClient extends FileClient {

    private final CustomFileRemoteService customFileRemoteService;

    @Autowired
    public ExpandFileClient(ObjectMapper objectMapper,
                            FileRemoteService fileRemoteService,
                            BootFileConfigProperties properties,
                            CustomFileRemoteService customFileRemoteService) {
        super(objectMapper, fileRemoteService, properties);
        this.customFileRemoteService = customFileRemoteService;
    }

    /**
     * 文件服务根据url集合删除文件，兼容了旧数据，即文件服务有文件，但是文件服务的file表中无数据的情况
     *
     * @param organizationId organizationId
     * @param bucketName bucketName
     * @param urls urls
     */
    public void deleteFileByUrlWithDbOptional(Long organizationId, String bucketName, List<String> urls) {
        ResponseUtils.getResponse(customFileRemoteService.deleteFileByUrl(organizationId, bucketName, urls), Void.class);
    }

    public List<FileVO> queryFileDTOByFileKeys(Long organizationId, List<String> fileKeys) {
       return ResponseUtils.getResponse(customFileRemoteService.queryFileDTOByFileKeys(organizationId, fileKeys), new TypeReference<List<FileVO>>() {});
    }

    public FileVO getFileDTOByFileKey(Long organizationId, String fileKey) {
        List<FileVO> fileVOS = queryFileDTOByFileKeys(organizationId, Collections.singletonList(fileKey));
        if (CollectionUtils.isEmpty(fileVOS)) {
            return new FileVO();
        }
        return fileVOS.get(0);
    }
}
