package io.choerodon.kb.app.service;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by wangxiang on 2022/5/6
 */
public interface OnlyOfficeService {
    void saveFile(Long projectId, JSONObject obj);
}
