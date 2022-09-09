package io.choerodon.kb.domain.repository;

import java.util.List;

import io.choerodon.kb.api.vo.ProjectDTO;

/**
 * 敏捷服务远程资源库
 * @author gaokuo.dai@zknow.com 2022-08-15 21:06:34
 */
public interface AgileRemoteRepository {
    List<ProjectDTO> deleteByWorkSpaceId(Long projectId, Long spaceId);
}
