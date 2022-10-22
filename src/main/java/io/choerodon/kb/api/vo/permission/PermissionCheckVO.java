package io.choerodon.kb.api.vo.permission;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.Assert;

import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.entity.PermissionRoleConfig;
import io.choerodon.kb.infra.enums.PermissionConstants;

import org.hzero.core.base.BaseConstants;


/**
 * 知识库鉴权 VO
 * @author gaokuo.dai@zknow.com 2022-10-12
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel("知识库鉴权 VO")
public class PermissionCheckVO implements Cloneable {

    /**
     * 计算是否有任一权限
     * @param permissionCheckInfos  待计算的权限数据
     * @return 是否有任一权限
     */
    public static boolean hasAnyPermission(List<PermissionCheckVO> permissionCheckInfos) {
        if(CollectionUtils.isEmpty(permissionCheckInfos)) {
            return false;
        }
        return permissionCheckInfos.stream().anyMatch(checkInfo -> Boolean.TRUE.equals(checkInfo.approve));
    }

    /**
     * 生成管理者权限返回值
     * @param permissionWaitCheck   权限校验信息
     * @return                      管理者权限
     */
    public static List<PermissionCheckVO> generateManagerPermission(Collection<PermissionCheckVO> permissionWaitCheck) {
        return Optional.ofNullable(permissionWaitCheck)
                .orElse(Collections.emptyList())
                .stream()
                .map(checkInfo -> checkInfo.setApprove(Boolean.TRUE).setControllerType(PermissionConstants.PermissionRole.MANAGER))
                .collect(Collectors.toList());
    }

    /**
     * 生成无权限返回值
     * @param permissionWaitCheck   权限校验信息
     * @return                      无权限
     */
    public static List<PermissionCheckVO> generateNonPermission(Collection<PermissionCheckVO> permissionWaitCheck) {
        return Optional.ofNullable(permissionWaitCheck)
                .orElse(Collections.emptyList())
                .stream()
                .map(checkInfo -> checkInfo.setApprove(Boolean.FALSE).setControllerType(PermissionConstants.PermissionRole.NULL))
                .collect(Collectors.toList());
    }

    /**
     * 权限对象合并器--同意覆盖否决
     * @param reduce    累加值
     * @param current   当前值
     * @return          合并值
     */
    public static List<PermissionCheckVO> permissionPositiveAccumulator(List<PermissionCheckVO> reduce, List<PermissionCheckVO> current) {
        return permissionAccumulator(reduce, current, PermissionCheckVO::mergePermissionPositive);
    }

    /**
     * 权限对象合并器--否决覆盖同意
     * @param reduce    累加值
     * @param current   当前值
     * @return          合并值
     */
    public static List<PermissionCheckVO> permissionNegativeAccumulator(List<PermissionCheckVO> reduce, List<PermissionCheckVO> current) {
        return permissionAccumulator(reduce, current, PermissionCheckVO::mergePermissionNegative);
    }

    /**
     * 权限对象合并器
     * @param reduce        累加值
     * @param current       当前值
     * @param mergeFunction merge方法
     * @return              合并值
     */
    private static List<PermissionCheckVO> permissionAccumulator(
            List<PermissionCheckVO> reduce,
            List<PermissionCheckVO> current,
            BinaryOperator<PermissionCheckVO> mergeFunction
    ) {
        Assert.notNull(mergeFunction, BaseConstants.ErrorCode.NOT_NULL);
        // 累加值为空时需要初始化一个空集合
        reduce = Optional.ofNullable(reduce).orElse(new ArrayList<>());
        // 当前值为空则不用合并直接返回累加值
        if(CollectionUtils.isEmpty(current)) {
            return reduce;
        }
        // 直接累加
        reduce.addAll(current);
        // 按permissionCode合并
        final List<PermissionCheckVO> combineResult = new ArrayList<>(reduce.stream().collect(Collectors.toMap(
                PermissionCheckVO::getPermissionCode,
                Function.identity(),
                mergeFunction)
        ).values());
        // 注意, 必须将结果塞入reduce才能生效, 请勿改动以下看起来没啥必要的代码
        reduce.clear();
        reduce.addAll(combineResult);
        return reduce;
    }

    /**
     * 合并--同意覆盖否决
     * @param that  另一个permission check对象, 注意permissionCode必须一致否则会报错
     * @return      合并结果, approve: true > false, null视为false; controllerType: MANAGER>EDITOR>READER>NULL=空指针
     */
    private PermissionCheckVO mergePermissionPositive(PermissionCheckVO that) {
        if(that == null) {
            return this.clone();
        }
        Assert.isTrue(Objects.equals(this.permissionCode, that.permissionCode), BaseConstants.ErrorCode.DATA_INVALID);
        PermissionCheckVO result = this.clone();
        result.approve = (Boolean.TRUE.equals(this.approve)) || (Boolean.TRUE.equals(that.approve));
        result.controllerType = PermissionConstants.PermissionRole.compare(this.controllerType, that.controllerType) > 0
                ? this.controllerType
                : that.controllerType;
        return result;
    }

    /**
     * 合并--否决覆盖同意
     * @param that  另一个permission check对象, 注意permissionCode必须一致否则会报错
     * @return      合并结果, approve: null = false > true; controllerType: NULL=空指针>READER>EDITOR>MANAGER
     */
    private PermissionCheckVO mergePermissionNegative(PermissionCheckVO that) {
        if(that == null) {
            return this.clone();
        }
        Assert.isTrue(Objects.equals(this.permissionCode, that.permissionCode), BaseConstants.ErrorCode.DATA_INVALID);
        PermissionCheckVO result = this.clone();
        result.approve = (Boolean.TRUE.equals(this.approve)) && (Boolean.TRUE.equals(that.approve));
        result.controllerType = PermissionConstants.PermissionRole.compare(this.controllerType, that.controllerType) < 0
                ? this.controllerType
                : that.controllerType;
        return result;
    }

    /**
     * @see PermissionRoleConfig#getPermissionCode()
     */
    @ApiModelProperty("操作权限Code")
    @NotBlank
    private String permissionCode;
    @ApiModelProperty("是否有权限")
    private Boolean approve;
    /**
     * @see PermissionRange#getPermissionRoleCode()
     */
    @ApiModelProperty("授权角色")
    private String controllerType;

    /**
     * @see PermissionRoleConfig#getPermissionCode()
     * @return 操作权限Code
     */
    public String getPermissionCode() {
        return permissionCode;
    }

    public PermissionCheckVO setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
        return this;
    }

    /**
     * @return 是否有权限
     */
    public Boolean getApprove() {
        return approve;
    }

    public PermissionCheckVO setApprove(Boolean approve) {
        this.approve = approve;
        return this;
    }

    /**
     * @see PermissionRange#getPermissionRoleCode()
     * @return 授权角色
     */
    public String getControllerType() {
        return controllerType;
    }

    public PermissionCheckVO setControllerType(String controllerType) {
        this.controllerType = controllerType;
        return this;
    }

    @Override
    public PermissionCheckVO clone() {
        try {
            return (PermissionCheckVO) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
