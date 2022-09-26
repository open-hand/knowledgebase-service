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

    /**
     * 查询组织知识库设置
     *
     * @param organizationId 租户id
     * @return 组织层知识库配置集
     */
    List<PermissionRange> selectOrgSetting(Long organizationId);

    /**
     * 初始化创建和默认设置
     *
     * @param organizationId 租户id
     * @param defaultRanges  组装好的默认权限数据
     */
    void initSetting(Long organizationId, List<PermissionRange> defaultRanges);
}
