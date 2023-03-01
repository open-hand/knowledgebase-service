package io.choerodon.kb.app.service;

/**
 * @author: 25499
 * @date: 2020/1/6 18:04
 * @description:
 */
public interface DataFixService {
    void fixData();

    void fixWorkspaceRoute();

    /**
     * v2.2.0修复旧的知识库/文档权限数据
     */
    void fixPermission();

    void fixWorkSpaceTemplate();

}
