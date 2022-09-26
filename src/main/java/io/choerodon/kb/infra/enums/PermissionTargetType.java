package io.choerodon.kb.infra.enums;

import java.util.Set;

import com.google.common.collect.Sets;

/**
 * Copyright (c) 2022. Zknow Enterprise Solution. All right reserved.
 *
 * @author zongqi.hao@zknow.com
 * @since 2022/9/23
 */
public enum PermissionTargetType {

    KNOWLEDGE_CREATE_ORG,
    KNOWLEDGE_CREATE_PROJECT,
    KNOWLEDGE_DEFAULT_ORG,
    KNOWLEDGE_DEFAULT_PROJECT,
    ;


    public static final Set<String> CREATE_SETTING_TYPES;

    static {
        CREATE_SETTING_TYPES = Sets.newHashSet(
                KNOWLEDGE_CREATE_ORG.name(),
                KNOWLEDGE_CREATE_PROJECT.name(),
                KNOWLEDGE_DEFAULT_ORG.name(),
                KNOWLEDGE_DEFAULT_PROJECT.name());
    }

    public static PermissionTargetType of(String value) {
        return PermissionTargetType.valueOf(value);
    }

}
