package io.choerodon.kb.infra.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import io.choerodon.core.exception.CommonException;

/**
 * @author superlee
 * @since 2022-03-15
 */
@Component
public class FilePathHelper {

    @Value("${services.attachment.url}")
    private String attachmentUrl;

    private static final String DIVIDING_LINE = "/";

    public String generateRelativePath(String fullPath) {
        if (ObjectUtils.isEmpty(fullPath)) {
            throw new CommonException("error.file.full.url.empty");
        }
        if (!fullPath.startsWith(attachmentUrl)) {
            throw new CommonException("error.fullPath.not.match.attachmentUrl");
        }
        return fullPath.substring(attachmentUrl.length());
    }

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
