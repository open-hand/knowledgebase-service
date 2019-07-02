package io.choerodon.kb.domain.kb.repository;

import io.choerodon.kb.api.dao.UserSettingDTO;

/**
 * Created by HuangFuqiang@choerodon.io on 2019/07/02.
 * Email: fuqianghuang01@gmail.com
 */
public interface UserSettingrepository {

    void insert(UserSettingDTO userSettingDTO);

    void updateBySelective(UserSettingDTO userSettingDTO);

}
