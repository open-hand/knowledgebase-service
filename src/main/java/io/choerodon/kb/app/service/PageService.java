package io.choerodon.kb.app.service;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageService {

    Boolean checkPageCreate(Long id);

    String pageToc(Long id);

    void exportMd2Pdf(Long organizationId, Long projectId, Long pageId, HttpServletResponse response);

    String importDocx2Md(Long organizationId, Long projectId, MultipartFile file, String type);
}
