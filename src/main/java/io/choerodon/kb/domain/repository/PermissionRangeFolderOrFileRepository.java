package io.choerodon.kb.domain.repository;

import java.util.List;

import io.choerodon.kb.domain.entity.PermissionRange;

import org.hzero.mybatis.base.BaseRepository;

/**
 * 知识库权限创建范围资源库
 *
 * @author zongqi.hao@zknow.com
 */
public interface PermissionRangeFolderOrFileRepository extends BaseRepository<PermissionRange> {

    List<PermissionRange> queryFolderOrFileCollaborator(Long organizationId, Long projectId, String targetType, Long targetValue);
}
