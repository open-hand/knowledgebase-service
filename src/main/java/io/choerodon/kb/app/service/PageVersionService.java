package io.choerodon.kb.app.service;

import io.choerodon.kb.api.vo.PageVersionCompareVO;
import io.choerodon.kb.api.vo.PageVersionInfoVO;
import io.choerodon.kb.api.vo.PageVersionVO;
import io.choerodon.kb.infra.dto.PageVersionDTO;

import java.util.List;

/**
 * @author shinan.chen
 * @since 2019/5/16
 */
public interface PageVersionService {

    PageVersionDTO baseCreate(PageVersionDTO create);

    void baseDelete(Long versionId);

    void baseUpdate(PageVersionDTO update);

    PageVersionDTO queryByVersionId(Long versionId, Long pageId);

    /**
     * 查询某个页面的所有版本
     *
     * @param organizationId
     * @param projectId
     * @param pageId
     * @return
     */
    List<PageVersionVO> queryByPageId(Long organizationId, Long projectId, Long pageId);

    /**
     * 创建版本并处理存储diff
     *
     * @param pageId
     * @param isFirstVersion
     * @param isMinorEdit
     */
    Long createVersionAndContent(Long pageId, String title, String content, Long oldVersionId, Boolean isFirstVersion, Boolean isMinorEdit);

    /**
     * 查询版本详情
     *
     * @param organizationId
     * @param projectId
     * @param pageId
     * @return
     */
    PageVersionInfoVO queryById(Long organizationId, Long projectId, Long pageId, Long versionId);

    /**
     * 版本比较
     *
     * @param organizationId
     * @param pageId
     * @param firstVersionId
     * @param secondVersionId
     * @return
     */
    PageVersionCompareVO compareVersion(Long organizationId, Long projectId, Long pageId, Long firstVersionId, Long secondVersionId);

    /**
     * 版本回退
     *
     * @param organizationId
     * @param projectId
     * @param pageId
     * @param versionId
     */
    void rollbackVersion(Long organizationId, Long projectId, Long pageId, Long versionId);
}
