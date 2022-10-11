package io.choerodon.kb.api.vo.permission;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.infra.enums.PermissionConstants;

/**
 * 组织级知识库权限设置VO
 * @author zongqi.hao@zknow.com
 * @since 2022/9/23
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganizationPermissionSettingVO {

    /**
     * 根据权限范围数据生成VO
     * @param permissionRanges 权限范围数据
     * @return VO
     */
    public static OrganizationPermissionSettingVO of(List<PermissionRange> permissionRanges) {
        OrganizationPermissionSettingVO organizationPermissionSettingVO = new OrganizationPermissionSettingVO();
        Map<String, List<PermissionRange>> targetMap = permissionRanges.stream().collect(Collectors.groupingBy(PermissionRange::getTargetType));
        for (Map.Entry<String, List<PermissionRange>> rangeEntry : targetMap.entrySet()) {
            List<PermissionRange> groupRanges = rangeEntry.getValue();
            String organizationCreateRangeType;
            switch (PermissionConstants.PermissionTargetType.of(rangeEntry.getKey())) {
                case KNOWLEDGE_BASE_CREATE_ORG:
                    organizationCreateRangeType = groupRanges.stream()
                            .filter(permissionRange -> PermissionConstants.PermissionRangeType.RADIO_RANGES_TYPES_FOR_FRONT.contains(permissionRange.getRangeType()))
                            .findFirst()
                            .map(PermissionRange::getRangeType)
                            .orElse(PermissionConstants.PermissionRangeType.SPECIFY_RANGE.toString());
                    organizationPermissionSettingVO.setOrganizationCreateRangeType(organizationCreateRangeType);
                    organizationPermissionSettingVO.setOrganizationCreateSetting(groupRanges);
                    break;
                case KNOWLEDGE_BASE_CREATE_PROJECT:
                    organizationCreateRangeType = groupRanges.stream()
                            .filter(permissionRange -> PermissionConstants.PermissionRangeType.RADIO_RANGES_TYPES_FOR_FRONT.contains(permissionRange.getRangeType()))
                            .findFirst()
                            .map(PermissionRange::getRangeType)
                            .orElse(PermissionConstants.PermissionRangeType.SPECIFY_RANGE.toString());
                    organizationPermissionSettingVO.setProjectCreateRangeType(organizationCreateRangeType);
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

    @ApiModelProperty(value = "组织创建权限范围类型")
    private String organizationCreateRangeType;
    @ApiModelProperty(value = "组织创建权限设置")
    private List<PermissionRange> organizationCreateSetting;
    @ApiModelProperty(value = "项目创建权限范围类型")
    private String projectCreateRangeType;
    @ApiModelProperty(value = "项目创建知识库权限设置")
    private List<PermissionRange> projectCreateSetting;
    @ApiModelProperty(value = "组织知识库默认权限配置")
    private List<PermissionRange> organizationDefaultPermissionRange;
    @ApiModelProperty(value = "项目知识库默认权限配置")
    private List<PermissionRange> projectDefaultPermissionRange;

    /**
     * @return 组织创建权限设置
     */
    public List<PermissionRange> getOrganizationCreateSetting() {
        return organizationCreateSetting;
    }

    public void setOrganizationCreateSetting(List<PermissionRange> organizationCreateSetting) {
        this.organizationCreateSetting = organizationCreateSetting;
    }

    /**
     * @return 项目创建知识库权限设置
     */
    public List<PermissionRange> getProjectCreateSetting() {
        return projectCreateSetting;
    }

    public void setProjectCreateSetting(List<PermissionRange> projectCreateSetting) {
        this.projectCreateSetting = projectCreateSetting;
    }

    /**
     * @return 项目知识库默认权限配置
     */
    public List<PermissionRange> getProjectDefaultPermissionRange() {
        return projectDefaultPermissionRange;
    }

    public void setProjectDefaultPermissionRange(List<PermissionRange> projectDefaultPermissionRange) {
        this.projectDefaultPermissionRange = projectDefaultPermissionRange;
    }

    /**
     * @return 组织知识库默认权限配置
     */
    public List<PermissionRange> getOrganizationDefaultPermissionRange() {
        return organizationDefaultPermissionRange;
    }

    public void setOrganizationDefaultPermissionRange(List<PermissionRange> organizationDefaultPermissionRange) {
        this.organizationDefaultPermissionRange = organizationDefaultPermissionRange;
    }

    /**
     * @return 组织创建权限范围类型
     */
    public String getOrganizationCreateRangeType() {
        return organizationCreateRangeType;
    }

    public void setOrganizationCreateRangeType(String organizationCreateRangeType) {
        this.organizationCreateRangeType = organizationCreateRangeType;
    }

    /**
     * @return 项目创建权限范围类型
     */
    public String getProjectCreateRangeType() {
        return projectCreateRangeType;
    }

    public void setProjectCreateRangeType(String projectCreateRangeType) {
        this.projectCreateRangeType = projectCreateRangeType;
    }
}
