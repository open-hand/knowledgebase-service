package io.choerodon.kb.app.service;

import io.choerodon.kb.api.vo.UserSettingVO;

/**
 * Created by HuangFuqiang@choerodon.io on 2019/07/02.
 * Email: fuqianghuang01@gmail.com
 */
public interface UserSettingService {

    UserSettingVO createOrUpdate(Long organizationId, Long projectId, UserSettingVO userSettingVO);
}
