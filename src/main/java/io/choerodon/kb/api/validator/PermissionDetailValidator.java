package io.choerodon.kb.api.validator;

import static io.choerodon.kb.infra.enums.PermissionConstants.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.SetUtils;
import org.springframework.util.ObjectUtils;

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

    private static final Set<String> SECURITY_CONFIG_PERMISSION_CODE = Stream.of(PermissionConstants.PermissionTargetType.WORKSPACE_AND_BASE_TARGET_TYPES)
            .map(PermissionConstants.SecurityConfigAction::buildPermissionCodeByType)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

    private static final Set<Integer> AUTHORIZE_FLAGS = SetUtils.unmodifiableSet(BaseConstants.Flag.YES, BaseConstants.Flag.NO);

    public static void validate(PermissionDetailVO permissionDetailVO) {
        String targetType = permissionDetailVO.getTargetType();
        Set<String> targetTypes = PermissionTargetType.WORKSPACE_AND_BASE_TARGET_TYPES;
        validateByValues(targetType, targetTypes, "error.illegal.permission.range.target.type");
        Set<String> rangeTypes = PermissionRangeType.WORKSPACE_AND_BASE_RANGE_TYPES;
        List<PermissionRange> permissionRanges = permissionDetailVO.getPermissionRanges();
        if (!ObjectUtils.isEmpty(permissionRanges)) {
            for (PermissionRange permissionRange : permissionRanges) {
                String thisTargetType = permissionRange.getTargetType();
                validateByValues(thisTargetType, targetTypes, "error.illegal.permission.range.target.type");
                String rangeType = permissionRange.getRangeType();
                validateByValues(rangeType, rangeTypes, "error.illegal.permission.range.type");
                String permissionRoleCode = permissionRange.getPermissionRoleCode();
                Set<String> permissionRoleCodes = new HashSet<>(Arrays.asList(PermissionRole.PERMISSION_ROLE_CONFIG_CODES));
                validateByValues(permissionRoleCode, permissionRoleCodes, "error.illegal.permission.role.code");
            }
        }
        List<SecurityConfig> securityConfigs = permissionDetailVO.getSecurityConfigs();
        if (!ObjectUtils.isEmpty(securityConfigs)) {
            for (SecurityConfig securityConfig : securityConfigs) {
                String thisTargetType = securityConfig.getTargetType();
                validateByValues(thisTargetType, targetTypes, "error.illegal.permission.range.target.type");
                String permissionCode = securityConfig.getPermissionCode();
                validateByValues(permissionCode, SECURITY_CONFIG_PERMISSION_CODE, "error.illegal.permission.security.config.code");
                Integer authorizeFlag = securityConfig.getAuthorizeFlag();
                if (authorizeFlag == null) {
                    throw new CommonException("error.illegal.permission.security.config.authorizeFlag.null");
                }
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
