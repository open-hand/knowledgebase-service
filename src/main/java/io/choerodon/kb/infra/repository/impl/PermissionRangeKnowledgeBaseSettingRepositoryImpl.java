package io.choerodon.kb.infra.repository.impl;

import java.util.List;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Repository;

import io.choerodon.kb.api.vo.permission.OrganizationPermissionSettingVO;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.repository.PermissionRangeKnowledgeBaseSettingRepository;
import io.choerodon.kb.infra.enums.PermissionConstants;

import org.hzero.mybatis.domian.Condition;
import org.hzero.mybatis.util.Sqls;

/**
 * 权限范围知识库配置 领域资源库实现
 * @author zongqi.hao@zknow.com 2022-09-23
 */
@Repository
public class PermissionRangeKnowledgeBaseSettingRepositoryImpl extends PermissionRangeBaseRepositoryImpl implements PermissionRangeKnowledgeBaseSettingRepository {

    @Override
    public OrganizationPermissionSettingVO queryOrgPermissionSetting(Long organizationId) {
        List<PermissionRange> permissionRanges = this.selectSettingByOrganizationId(organizationId);
        // 组装常规数据, (user, role, work_group)
        permissionRanges = this.assemblyRangeData(organizationId, permissionRanges);
        return OrganizationPermissionSettingVO.of(permissionRanges);
    }

    @Override
    public void initOrganizationPermissionRangeKnowledgeBaseSetting(Long organizationId, List<PermissionRange> defaultRanges) {
        List<PermissionRange> initData = generateDefaultKnowledgeBaseCreateSettingForOrg(organizationId);
        initData.addAll(defaultRanges);
        this.batchInsert(initData);
    }

    /**
     * 生成组织默认的创建知识库权限配置
     * @param organizationId    组织ID
     * @return                  创建结果
     */
    private List<PermissionRange> generateDefaultKnowledgeBaseCreateSettingForOrg(Long organizationId) {
        return Lists.newArrayList(
                // 组织层创建默认为组织管理员
                PermissionRange.of(
                        organizationId,
                        PermissionConstants.EMPTY_ID_PLACEHOLDER,
                        PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_CREATE_ORG.getCode(),
                        PermissionConstants.EMPTY_ID_PLACEHOLDER,
                        PermissionConstants.PermissionRangeType.MANAGER.toString(),
                        PermissionConstants.EMPTY_ID_PLACEHOLDER,
                        PermissionConstants.PermissionRole.NULL
                ),
                // 项目层创建默认为项目成员
                PermissionRange.of(
                        organizationId,
                        PermissionConstants.EMPTY_ID_PLACEHOLDER,
                        PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_CREATE_PROJECT.getCode(),
                        PermissionConstants.EMPTY_ID_PLACEHOLDER,
                        PermissionConstants.PermissionRangeType.MEMBER.toString(),
                        PermissionConstants.EMPTY_ID_PLACEHOLDER,
                        PermissionConstants.PermissionRole.NULL
                )
        );
    }

    /**
     * 根据组织ID查询组织层知识库设置原始值
     * @param organizationId    组织ID
     * @return                  组织层知识库设置原始值
     */
    private List<PermissionRange> selectSettingByOrganizationId(Long organizationId) {
        return selectByCondition(Condition.builder(PermissionRange.class).andWhere(Sqls.custom()
                .andEqualTo(PermissionRange.FIELD_ORGANIZATION_ID, organizationId)
                .andIn(PermissionRange.FIELD_TARGET_TYPE, PermissionConstants.PermissionTargetType.KNOWLEDGE_BASE_SETTING_TARGET_TYPES)
        ).build());
    }

}