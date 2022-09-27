package io.choerodon.kb.infra.repository.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Repository;

import io.choerodon.kb.api.vo.permission.OrganizationPermissionSettingVO;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.repository.PermissionRangeKnowledgeBaseSettingRepository;
import io.choerodon.kb.infra.enums.PermissionConstants;

import org.hzero.mybatis.domian.Condition;
import org.hzero.mybatis.util.Sqls;

/**
 * Copyright (c) 2022. Zknow Enterprise Solution. All right reserved.
 *
 * @author zongqi.hao@zknow.com
 * @since 2022/9/23
 */
@Repository
public class PermissionRangeKnowledgeBaseSettingRepositoryImpl extends PermissionRangeBaseRepositoryImpl implements PermissionRangeKnowledgeBaseSettingRepository {

    @Override
    public OrganizationPermissionSettingVO queryOrgPermissionSetting(Long organizationId) {
        OrganizationPermissionSettingVO organizationPermissionSettingVO = new OrganizationPermissionSettingVO();
        List<PermissionRange> permissionRanges = this.selectSettingByOrganizationId(organizationId);
        // 组装常规数据, (user, role, work_group)
        super.assemblyRangeData(organizationId, permissionRanges);
        // 根据项目和组织进行分组，如果只有一个则为单角色，如果有多个则为选择范围, 设置到不同的属性
        Map<String, List<PermissionRange>> targetMap = permissionRanges.stream().collect(Collectors.groupingBy(PermissionRange::getTargetType));
        for (Map.Entry<String, List<PermissionRange>> rangeEntry : targetMap.entrySet()) {
            List<PermissionRange> groupRanges = rangeEntry.getValue();
            for (PermissionRange groupRange : groupRanges) {
                // TODO 填充聚合信息 eg. 角色下包含的人数
            }
            switch (PermissionConstants.PermissionTargetType.of(rangeEntry.getKey())) {
                case KNOWLEDGE_BASE_CREATE_ORG:
                    Optional<PermissionRange> any = groupRanges.stream()
                            .filter(permissionRange -> PermissionConstants.PermissionRangeType.RADIO_RANGES.contains(permissionRange.getRangeType()))
                            .findFirst();
                    organizationPermissionSettingVO.setOrganizationCreateRangeType(any.map(PermissionRange::getRangeType).orElse(PermissionConstants.PermissionRangeType.SPECIFY_RANGE.toString()));
                    organizationPermissionSettingVO.setOrganizationCreateSetting(groupRanges);
                    break;
                case KNOWLEDGE_BASE_CREATE_PROJECT:
                    any = groupRanges.stream()
                            .filter(permissionRange -> PermissionConstants.PermissionRangeType.RADIO_RANGES.contains(permissionRange.getRangeType()))
                            .findFirst();
                    organizationPermissionSettingVO.setProjectCreateRangeType(any.map(PermissionRange::getRangeType).orElse(PermissionConstants.PermissionRangeType.SPECIFY_RANGE.toString()));
                    organizationPermissionSettingVO.setProjectCreateSetting(groupRanges);
                    break;
                case KNOWLEDGE_BASE_DEFAULT_ORG:
                    organizationPermissionSettingVO.setOrganizationDefaultPermissionRange(groupRanges);
                    break;
                case KNOWLEDGE_BASE_DEFAULT_PROJECT:
                    organizationPermissionSettingVO.setProjectDefaultPermissionRange(groupRanges);
                default:
                    break;
            }
        }
        return organizationPermissionSettingVO;
    }

    @Override
    public void initOrganizationPermissionRangeKnowledgeBaseSetting(Long organizationId, List<PermissionRange> defaultRanges) {
        List<PermissionRange> initData = generateDefaultKnowledgeBaseCreateSettingForOrg(organizationId);
        initData.addAll(defaultRanges);
        batchInsertSelective(initData);
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
                .andIn(PermissionRange.FIELD_TARGET_TYPE, PermissionConstants.PermissionTargetType.CREATE_SETTING_TYPES)
        ).build());
    }

}
