package io.choerodon.kb.domain.repository;

import java.util.List;

import io.choerodon.kb.domain.entity.PermissionRange;

import org.hzero.mybatis.base.BaseRepository;

/**
 * 知识库权限创建范围资源库
 *
 * @author zongqi.hao@zknow.com
 */
public interface PermissionRangeTenantSettingRepository extends BaseRepository<PermissionRange> {

    List<PermissionRange> selectOrgSetting(Long organizationId);
}
