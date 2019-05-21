package io.choerodon.kb.app.service;

/**
 * Created by Zenger on 2019/4/30.
 */
public interface PageService {

    Boolean checkPageCreate(Long id);

    String pageToc(Long id);
}
