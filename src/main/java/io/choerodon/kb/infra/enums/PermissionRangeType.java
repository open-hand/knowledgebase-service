package io.choerodon.kb.infra.enums;

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

    static {
//        CREATE_SETTING_TYPES = Sets.newHashSet(
//                MANAGER.name(),
//                MEMBER.name(),
//                USER.name(),
//                KNOWLEDGE_DEFAULT_PROJECT.name());
    }

    public static PermissionRangeType of(String value) {
        return PermissionRangeType.valueOf(value);
    }

}
