package io.choerodon.kb.app.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.kb.api.vo.permission.OrganizationPermissionSettingVO;
import io.choerodon.kb.app.service.PermissionRangeService;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.repository.PermissionRangeRepository;
import io.choerodon.kb.domain.repository.PermissionRangeTenantSettingRepository;
import io.choerodon.kb.infra.enums.PermissionRangeTargetType;

import org.hzero.core.base.BaseAppService;
import org.hzero.mybatis.helper.SecurityTokenHelper;

/**
 * 知识库权限应用范围应用服务默认实现
 *
 * @author gaokuo.dai@zknow.com 2022-09-22 17:14:46
 */
@Service
public class PermissionRangeServiceImpl extends BaseAppService implements PermissionRangeService {

    @Autowired
    private PermissionRangeRepository permissionRangeRepository;
    @Autowired
    private PermissionRangeTenantSettingRepository permissionRangeTenantSettingRepository;

    @Override
    public PermissionRange create(Long tenantId, PermissionRange permissionRange) {
        validObject(permissionRange);
        permissionRangeRepository.insertSelective(permissionRange);
        return permissionRange;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionRange update(Long tenantId, PermissionRange permissionRange) {
        SecurityTokenHelper.validToken(permissionRange);
        permissionRangeRepository.updateByPrimaryKeySelective(permissionRange);
        return permissionRange;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(PermissionRange permissionRange) {
        SecurityTokenHelper.validToken(permissionRange);
        permissionRangeRepository.deleteByPrimaryKey(permissionRange);
    }

    @Override
    public OrganizationPermissionSettingVO queryOrgPermissionSetting(Long organizationId) {
        OrganizationPermissionSettingVO organizationPermissionSettingVO = new OrganizationPermissionSettingVO();
        List<PermissionRange> permissionRanges = permissionRangeTenantSettingRepository.selectOrgSetting(organizationId);
        // 根据项目和组织进行分组，如果只有一个则为单角色，如果有多个则为选择范围, 设置到不同的属性
        Map<String, List<PermissionRange>> targetMap = permissionRanges.stream().collect(Collectors.groupingBy(PermissionRange::getTargetType));
        for (Map.Entry<String, List<PermissionRange>> rangeEntry : targetMap.entrySet()) {
            switch (PermissionRangeTargetType.of(rangeEntry.getKey())) {
                case KNOWLEDGE_CREATE_ORG:
                    organizationPermissionSettingVO.setOrganizationCreateSetting(rangeEntry.getValue());
                    break;
                case KNOWLEDGE_CREATE_PROJECT:
                    organizationPermissionSettingVO.setProjectCreateSetting(rangeEntry.getValue());
                    break;
                case KNOWLEDGE_DEFAULT_ORG:
                    organizationPermissionSettingVO.setOrganizationDefaultPermissionRange(rangeEntry.getValue());
                    break;
                case KNOWLEDGE_DEFAULT_PROJECT:
                    organizationPermissionSettingVO.setProjectDefaultPermissionRange(rangeEntry.getValue());
                default:
                    break;
            }
        }
        return organizationPermissionSettingVO;
    }

}
