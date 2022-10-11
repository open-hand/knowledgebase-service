package io.choerodon.kb.domain.repository;

import java.util.List;

import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.entity.UserInfo;

import org.hzero.mybatis.base.BaseRepository;

/**
 * 知识库权限应用范围资源库基础
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
public interface PermissionRangeBaseRepository extends BaseRepository<PermissionRange> {

    /**
     * 组装权限范围数据
     *
     * @param organizationId   租户id
     * @param permissionRanges 需要组装的权限范围数据
     * @return                 处理之后的数据
     */
    List<PermissionRange> assemblyRangeData(Long organizationId, List<PermissionRange> permissionRanges);

    /**
     * 查询用户的工作组和在组织/项目下的角色
     *
     * @param organizationId    组织ID
     * @param projectId         项目ID
     * @return                  查询结果
     */
    UserInfo queryUserInfo(Long organizationId,
                           Long projectId);

}
