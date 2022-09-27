package io.choerodon.kb.infra.common;

/**
 * Copyright (c) 2022. ZKNOW Enterprise Solution. All right reserved.
 *
 * @author zongqi.hao@zknow.com
 * @since 2022/9/26
 */
public interface ChoerodonRole {

    interface Label {

        /**
         * 租户角色标签
         */
        String TENANT_ROLE_LABEL = "TENANT_ROLE";
        /**
         * 项目角色标签
         */
        String PROJECT_ROLE_LABEL = "PROJECT_ROLE";
    }

    interface RoleCode {

        /**
         * 租户管理员
         */
        String TENANT_ADMIN = "administrator";
        /**
         * 租户成员
         */
        String TENANT_MEMBER = "member";
        /**
         * 项目管理员
         */
        String PROJECT_ADMIN = "project-admin";
        /**
         * 项目成员
         */
        String PROJECT_MEMBER = "project-member";
    }

}
