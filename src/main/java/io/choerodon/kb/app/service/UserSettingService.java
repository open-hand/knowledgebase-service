package io.choerodon.kb.app.service;

import io.choerodon.kb.api.dao.UserSettingDTO;

/**
 * Created by HuangFuqiang@choerodon.io on 2019/07/02.
 * Email: fuqianghuang01@gmail.com
 */
public interface UserSettingService {

    void createOrUpdate(UserSettingDTO userSettingDTO);
}
