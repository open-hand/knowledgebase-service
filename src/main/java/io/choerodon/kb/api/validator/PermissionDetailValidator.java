package io.choerodon.kb.api.validator;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.springframework.util.Assert;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.domain.entity.SecurityConfig;
import io.choerodon.kb.infra.enums.PermissionConstants;

import org.hzero.core.base.BaseConstants;

/**
 * 知识库对象权限详情VO校验工具
 * @author superlee
 * @since 2022-09-26
 */
public class PermissionDetailValidator {

    /**
     * 知识库安全时设置操作Code集合
     */
    private static final Set<String> SECURITY_CONFIG_PERMISSION_CODE = Stream.of(PermissionConstants.PermissionTargetBaseType.values())
            .map(PermissionConstants.SecurityConfigAction::buildPermissionCodeByType)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

    /**
     * 授权标志集合
     */
    private static final Set<Integer> AUTHORIZE_FLAGS = SetUtils.unmodifiableSet(BaseConstants.Flag.YES, BaseConstants.Flag.NO);

    /**
     * 校验PermissionDetailVO, 填充真实的控制对象类型
     * @param permissionDetail          待处理的对象
     * @param validTargetTypes          合法的控制对象类型集合
     * @param validRangeTypes           合法的授权对象类型集合
     * @param validPermissionRoleCodes  合法的授权角色集合
     */
    public static void validateAndFillTargetType(PermissionDetailVO permissionDetail,
                                                 Set<String> validTargetTypes,
                                                 Set<String> validRangeTypes,
                                                 Set<String> validPermissionRoleCodes) {
        Assert.notNull(permissionDetail, BaseConstants.ErrorCode.NOT_NULL);
        Assert.isTrue(CollectionUtils.isNotEmpty(validTargetTypes), BaseConstants.ErrorCode.NOT_NULL);
        Assert.isTrue(CollectionUtils.isNotEmpty(validRangeTypes), BaseConstants.ErrorCode.NOT_NULL);

        String targetType = permissionDetail.getTargetType();
        validateByValues(targetType, validTargetTypes, "error.illegal.permission.range.target.type");
        List<PermissionRange> permissionRanges = permissionDetail.getPermissionRanges();
        if (CollectionUtils.isNotEmpty(permissionRanges)) {
            for (PermissionRange permissionRange : permissionRanges) {
                permissionRange.setTargetType(targetType);
                String rangeType = permissionRange.getRangeType();
                validateByValues(rangeType, validRangeTypes, "error.illegal.permission.range.type");
                String permissionRoleCode = permissionRange.getPermissionRoleCode();
                validateByValues(permissionRoleCode, validPermissionRoleCodes, "error.illegal.permission.role.code");
            }
        }
        List<SecurityConfig> securityConfigs = permissionDetail.getSecurityConfigs();
        if (CollectionUtils.isNotEmpty(securityConfigs)) {
            for (SecurityConfig securityConfig : securityConfigs) {
                securityConfig.setTargetType(targetType);
                String permissionCode = securityConfig.getPermissionCode();
                validateByValues(permissionCode, SECURITY_CONFIG_PERMISSION_CODE, "error.illegal.permission.security.config.code");
                Integer authorizeFlag = securityConfig.getAuthorizeFlag();
                if (!AUTHORIZE_FLAGS.contains(authorizeFlag)) {
                    throw new CommonException("error.illegal.permission.security.config.authorizeFlag");
                }
            }
        }
    }

    /**
     * Set校验工具
     * @param value     待校验值
     * @param values    合法值集合
     * @param msg       报错信息
     */
    private static void validateByValues(String value,
                                         Set<String> values,
                                         String msg) {
        if (!values.contains(value)) {
            throw new CommonException(msg, value);
        }
    }
}
