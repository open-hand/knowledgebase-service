package io.choerodon.kb.infra.enums;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Copyright (c) 2022. Zknow Enterprise Solution. All right reserved.
 *
 * @author zongqi.hao@zknow.com
 * @since 2022/9/23
 */
public enum PermissionRangeType {

    MANAGER,
    MEMBER,
    USER,
    ROLE,
    WORK_GROUP,
    PUBLIC,
    ;

    //    public static final Set<String> CREATE_SETTING_TYPES;
    public static final Set<String> WORKSPACE_AND_BASE_RANGE_TYPES;

    static {
        WORKSPACE_AND_BASE_RANGE_TYPES =
                Sets.newHashSet(
                        USER.name(),
                        ROLE.name(),
                        WORK_GROUP.name(),
                        PUBLIC.name()
                );
    }

    public static PermissionRangeType of(String value) {
        return PermissionRangeType.valueOf(value);
    }

}
