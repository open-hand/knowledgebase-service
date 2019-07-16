package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.api.dao.UserSettingVO;

/**
 * Created by HuangFuqiang@choerodon.io on 2019/07/02.
 * Email: fuqianghuang01@gmail.com
 */
public interface UserSettingrepository {

    void baseCreate(UserSettingVO userSettingVO);

    void baseUpdateBySelective(UserSettingVO userSettingVO);
}
