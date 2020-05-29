package io.choerodon.kb.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import io.choerodon.kb.api.vo.RecycleVO;
import io.choerodon.kb.api.vo.SearchDTO;

/**
 * @author: 25499
 * @date: 2020/1/3 10:24
 * @description:
 */
public interface RecycleService {
    /**
     * 分页查询回收站（知识库和文档）
     * @param projectId
     * @param organizationId
     * @param pageRequest
     * @param searchDTO
     * @return
     */
    Page<RecycleVO> pageList(Long projectId, Long organizationId, PageRequest pageRequest, SearchDTO searchDTO);

    void restoreWorkSpaceAndPage(Long organizationId, Long projectId, String type, Long id, Long baseId);

    void deleteWorkSpaceAndPage(Long organizationId, Long projectId, String type, Long id);
}
