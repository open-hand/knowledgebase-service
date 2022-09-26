package io.choerodon.kb.api.validator;

import java.util.List;
import java.util.Set;

import org.springframework.util.ObjectUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.vo.permission.PermissionDetailVO;
import io.choerodon.kb.domain.entity.PermissionRange;
import io.choerodon.kb.infra.enums.PermissionConstants;

/**
 * @author superlee
 * @since 2022-09-26
 */
public class PermissionDetailValidator {

    public static void validate(PermissionDetailVO permissionDetailVO) {
        String targetType = permissionDetailVO.getTargetType();
        Set<String> targetTypes = PermissionConstants.PermissionTargetType.WORKSPACE_AND_BASE_TARGET_TYPES;
        validateTargetType(targetType, targetTypes);
        Set<String> rangeTypes = PermissionConstants.PermissionRangeType.WORKSPACE_AND_BASE_RANGE_TYPES;
        List<PermissionRange> permissionRanges = permissionDetailVO.getPermissionRanges();
        if (!ObjectUtils.isEmpty(permissionRanges)) {
            for (PermissionRange permissionRange : permissionRanges) {
                String thisTargetType = permissionRange.getTargetType();
                validateTargetType(thisTargetType, targetTypes);
                String rangeType = permissionRange.getRangeType();
                validateRangeType(rangeType, rangeTypes);
                String permissionRoleCode = permissionRange.getPermissionRoleCode();
                validateRoleCode(permissionRoleCode);
            }
        }
    }

    private static void validateRoleCode(String permissionRoleCode) {
        if (!PermissionConstants.PermissionRole.isValid(permissionRoleCode)) {
            throw new CommonException("error.illegal.permission.role.code", permissionRoleCode);
        }
    }

    private static void validateRangeType(String rangeType, Set<String> rangeTypes) {
        if (!rangeTypes.contains(rangeType)) {
            throw new CommonException("error.illegal.permission.range.type", rangeType);
        }
    }

    private static void validateTargetType(String targetType, Set<String> targetTypes) {
        if (!targetTypes.contains(targetType)) {
            throw new CommonException("error.illegal.permission.range.target.type", targetType);
        }
    }
}
