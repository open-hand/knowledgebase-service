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
    /**
     * 组织层知识库
     */
    KNOWLEDGE_BASE_ORG,
    /**
     * 项目层知识库
     */
    KNOWLEDGE_BASE_PROJECT,
    /**
     * 组织层文件夹
     */
    KNOWLEDGE_FOLDER_ORG,
    /**
     * 项目层文件夹
     */
    KNOWLEDGE_FOLDER_PROJECT,
    /**
     * 组织层文件，包含document和file
     */
    KNOWLEDGE_FILE_ORG,
    /**
     * 项目层文件，包含document和file
     */
    KNOWLEDGE_FILE_PROJECT,
    ;


    public static final Set<String> CREATE_SETTING_TYPES;
    /**
     * 知识库和知识库文档类型
     */
    public static final Set<String> WORKSPACE_AND_BASE_TARGET_TYPES;

    static {
        CREATE_SETTING_TYPES = Sets.newHashSet(
                KNOWLEDGE_CREATE_ORG.name(),
                KNOWLEDGE_CREATE_PROJECT.name(),
                KNOWLEDGE_DEFAULT_ORG.name(),
                KNOWLEDGE_DEFAULT_PROJECT.name());

        WORKSPACE_AND_BASE_TARGET_TYPES =
                Sets.newHashSet(
                        KNOWLEDGE_BASE_ORG.name(),
                        KNOWLEDGE_BASE_PROJECT.name(),
                        KNOWLEDGE_FOLDER_ORG.name(),
                        KNOWLEDGE_FOLDER_PROJECT.name(),
                        KNOWLEDGE_FILE_ORG.name(),
                        KNOWLEDGE_FILE_PROJECT.name()
                );
    }

    public static PermissionTargetType of(String value) {
        return PermissionTargetType.valueOf(value);
    }

}
