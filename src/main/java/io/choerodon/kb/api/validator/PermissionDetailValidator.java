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
 * @author superlee
 * @since 2022-09-26
 */
public class PermissionDetailValidator {

    private static final Set<String> SECURITY_CONFIG_PERMISSION_CODE = Stream.of(PermissionConstants.PermissionTargetBaseType.values())
            .map(PermissionConstants.SecurityConfigAction::buildPermissionCodeByType)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

    private static final Set<Integer> AUTHORIZE_FLAGS = SetUtils.unmodifiableSet(BaseConstants.Flag.YES, BaseConstants.Flag.NO);

    public static void validate(PermissionDetailVO permissionDetailVO, Set<String> validTargetTypes, Set<String> validRangeTypes, Set<String> validPermissionRoleCodes) {
        Assert.notNull(permissionDetailVO, BaseConstants.ErrorCode.NOT_NULL);
        Assert.isTrue(CollectionUtils.isNotEmpty(validTargetTypes), BaseConstants.ErrorCode.NOT_NULL);
        Assert.isTrue(CollectionUtils.isNotEmpty(validRangeTypes), BaseConstants.ErrorCode.NOT_NULL);

        String targetType = permissionDetailVO.getTargetType();
        validateByValues(targetType, validTargetTypes, "error.illegal.permission.range.target.type");
        List<PermissionRange> permissionRanges = permissionDetailVO.getPermissionRanges();
        if (CollectionUtils.isNotEmpty(permissionRanges)) {
            for (PermissionRange permissionRange : permissionRanges) {
                String thisTargetType = permissionRange.getTargetType();
                validateByValues(thisTargetType, validTargetTypes, "error.illegal.permission.range.target.type");
                String rangeType = permissionRange.getRangeType();
                validateByValues(rangeType, validRangeTypes, "error.illegal.permission.range.type");
                String permissionRoleCode = permissionRange.getPermissionRoleCode();
                validateByValues(permissionRoleCode, validPermissionRoleCodes, "error.illegal.permission.role.code");
            }
        }
        List<SecurityConfig> securityConfigs = permissionDetailVO.getSecurityConfigs();
        if (CollectionUtils.isNotEmpty(securityConfigs)) {
            for (SecurityConfig securityConfig : securityConfigs) {
                String thisTargetType = securityConfig.getTargetType();
                validateByValues(thisTargetType, validTargetTypes, "error.illegal.permission.range.target.type");
                String permissionCode = securityConfig.getPermissionCode();
                validateByValues(permissionCode, SECURITY_CONFIG_PERMISSION_CODE, "error.illegal.permission.security.config.code");
                Integer authorizeFlag = securityConfig.getAuthorizeFlag();
                if (!AUTHORIZE_FLAGS.contains(authorizeFlag)) {
                    throw new CommonException("error.illegal.permission.security.config.authorizeFlag");
                }
            }
        }
    }

    private static void validateByValues(String value,
                                         Set<String> values,
                                         String msg) {
        if (!values.contains(value)) {
            throw new CommonException(msg, value);
        }
    }
}
