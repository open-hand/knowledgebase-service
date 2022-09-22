package script.db.groovy.knowledgebase_service

databaseChangeLog(logicalFilePath: 'script/db/kb_pms_role_config.groovy') {
    changeSet(id: '2022-09-22-kb_pms_role_config', author: 'gaokuo.dai@zknow.com') {
        if (helper.dbType().isSupportSequence()) {
            createSequence(sequenceName: 'KB_PMS_ROLE_CONFIG_S', startValue: "1")
        }

        createTable(tableName: "KB_PMS_ROLE_CONFIG", remarks: '知识库权限矩阵') {
            column(name: 'ID', type: 'BIGINT UNSIGNED', remarks: '主键', autoIncrement: true) {
                constraints(primaryKey: true, primaryKeyName: 'PK_KB_PMS_ROLE_CONFIG')
            }
            column(name: 'ORGANIZATION_ID', type: 'BIGINT UNSIGNED', remarks: '组织ID') {
                constraints(nullable: false)
            }
            column(name: 'PROJECT_ID', type: 'BIGINT UNSIGNED', remarks: '项目ID') {
                constraints(nullable: false)
            }
            column(name: 'PERMISSION_CODE', type: 'VARCHAR(60)', remarks: '操作权限Code') {
                constraints(nullable: false)
            }
            column(name: 'PERMISSION_ROLE_CODE', type: 'VARCHAR(30)', remarks: '授权角色') {
                constraints(nullable: false)
            }
            column(name: "AUTHORIZE_FLAG", type: "TINYINT(1)", defaultValue: "1", remarks: "授权标识") {
                constraints(nullable: false)
            }

            column(name: "OBJECT_VERSION_NUMBER", type: "BIGINT", defaultValue: "1") {
                constraints(nullable: false)
            }
            column(name: "CREATED_BY", type: "BIGINT", defaultValue: "0") {
                constraints(nullable: false)
            }
            column(name: "CREATION_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP") {
                constraints(nullable: false)
            }
            column(name: "LAST_UPDATED_BY", type: "BIGINT", defaultValue: "0") {
                constraints(nullable: false)
            }
            column(name: "LAST_UPDATE_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP") {
                constraints(nullable: false)
            }
        }

        addUniqueConstraint(columnNames: "ORGANIZATION_ID,PROJECT_ID,PERMISSION_CODE,PERMISSION_ROLE_CODE", tableName: "KB_PMS_ROLE_CONFIG",  constraintName: "KB_PMS_ROLE_CONFIG_U1")
    }
}
