package io.choerodon.kb.infra.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author superlee
 * @since 2022-09-26
 */
public enum PermissionRoleCode {

    /**
     * 可管理角色
     */
    MANAGER,
    /**
     * 可编辑角色
     */
    EDITOR,
    /**
     * 只读角色
     */
    READER,
    /**
     * 空值
     */
    NULL,
    ;

    public static Set<String> names() {
        return new HashSet<>(Arrays.asList(
                MANAGER.name(),
                EDITOR.name(),
                READER.name(),
                NULL.name()
        ));
    }

}
