package io.choerodon.kb.infra.utils;

import io.choerodon.kb.infra.feign.CustomFileRemoteService;
import org.hzero.boot.file.FileClient;
import org.hzero.boot.file.feign.FileRemoteService;
import org.hzero.core.util.ResponseUtils;

import java.util.List;

/**
 * @author superlee
 * @since 2020-05-25
 */
public class ExpandFileClient extends FileClient {

    private CustomFileRemoteService customFileRemoteService;

    public ExpandFileClient(FileRemoteService fileRemoteService,
                            CustomFileRemoteService customFileRemoteService) {
        super(fileRemoteService);
        this.customFileRemoteService = customFileRemoteService;
    }

    /**
     * 文件服务根据url集合删除文件，兼容了旧数据，即文件服务有文件，但是文件服务的file表中无数据的情况
     *
     * @param organizationId
     * @param bucketName
     * @param urls
     */
    public void deleteFileByUrlWithDbOptional(Long organizationId, String bucketName, List<String> urls) {
        ResponseUtils.getResponse(customFileRemoteService.deleteFileByUrl(organizationId, bucketName, urls), String.class);
    }
}
