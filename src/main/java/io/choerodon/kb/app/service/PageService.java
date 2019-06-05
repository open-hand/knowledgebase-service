package io.choerodon.kb.app.service;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageService {

    Boolean checkPageCreate(Long id);

    String pageToc(Long id);

    void exportPage2Pdf(Long organizationId, Long projectId, Long pageId, HttpServletResponse response);
}
