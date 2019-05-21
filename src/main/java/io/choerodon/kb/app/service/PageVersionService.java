package io.choerodon.kb.app.service;

import io.choerodon.kb.api.dao.PageVersionCompareDTO;
import io.choerodon.kb.api.dao.PageVersionDTO;
import io.choerodon.kb.api.dao.PageVersionInfoDTO;

import java.util.List;

/**
 * @author shinan.chen
 * @since 2019/5/16
 */
public interface PageVersionService {
    /**
     * 查询某个页面的所有版本
     *
     * @param organizationId
     * @param projectId
     * @param pageId
     * @return
     */
    List<PageVersionDTO> queryByPageId(Long organizationId, Long projectId, Long pageId);

    /**
     * 创建版本并处理存储diff
     *
     * @param pageId
     * @param isFirstVersion
     * @param isMinorEdit
     */
    Long createVersionAndContent(Long pageId, String content, Long oldVersionId, Boolean isFirstVersion, Boolean isMinorEdit);

    /**
     * 查询版本详情
     *
     * @param organizationId
     * @param projectId
     * @param pageId
     * @return
     */
    PageVersionInfoDTO queryById(Long organizationId, Long projectId, Long pageId, Long versionId);

    /**
     * 版本比较
     *
     * @param organizationId
     * @param pageId
     * @param firstVersionId
     * @param secondVersionId
     * @return
     */
    PageVersionCompareDTO compareVersion(Long organizationId,Long projectId, Long pageId, Long firstVersionId, Long secondVersionId);
}
