package io.choerodon.kb.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.app.service.FilePathService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

/**
 * @author superlee
 * @since 2022-03-15
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class FilePathServiceImpl implements FilePathService {

    @Value("${services.attachment.url}")
    private String attachmentUrl;

    private static final String DIVIDING_LINE = "/";

    @Override
    public String generateRelativePath(String fullPath) {
        if (ObjectUtils.isEmpty(fullPath)) {
            throw new CommonException("error.file.full.url.empty");
        }
        if (!fullPath.startsWith(attachmentUrl)) {
            throw new CommonException("error.fullPath.not.match.attachmentUrl");
        }
        return fullPath.substring(attachmentUrl.length());
    }

    @Override
    public String generateFullPath(String relativePath) {
        if (ObjectUtils.isEmpty(relativePath)) {
            throw new CommonException("error.file.relativePath.empty");
        }
        StringBuilder builder = new StringBuilder();
        if (!relativePath.startsWith(DIVIDING_LINE)) {
            relativePath = DIVIDING_LINE + relativePath;
        }
        builder.append(attachmentUrl).append(relativePath);
        return builder.toString();
    }
}
