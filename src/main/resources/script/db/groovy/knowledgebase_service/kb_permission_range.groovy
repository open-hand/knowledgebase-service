package script.db.groovy.knowledgebase_service

databaseChangeLog(logicalFilePath: 'script/db/kb_permission_range.groovy') {
    changeSet(id: '2022-09-22-kb_permission_range', author: 'gaokuo.dai@zknow.com') {
        if (helper.dbType().isSupportSequence()) {
            createSequence(sequenceName: 'KB_PERMISSION_RANGE_S', startValue: "1")
        }

        createTable(tableName: "KB_PERMISSION_RANGE", remarks: '知识库权限应用范围') {
            column(name: 'ID', type: 'BIGINT UNSIGNED', remarks: '主键', autoIncrement: true) {
                constraints(primaryKey: true, primaryKeyName: 'PK_KB_PERMISSION_RANGE')
            }
            column(name: 'ORGANIZATION_ID', type: 'BIGINT UNSIGNED', remarks: '组织ID') {
                constraints(nullable: false)
            }
            column(name: 'PROJECT_ID', type: 'BIGINT UNSIGNED', remarks: '项目ID') {
                constraints(nullable: false)
            }
            column(name: 'TARGET_TYPE', type: 'VARCHAR(30)', remarks: '控制对象类型') {
                constraints(nullable: false)
            }
            column(name: 'TARGET_VALUE', type: 'BIGINT UNSIGNED', remarks: '控制对象') {
                constraints(nullable: false)
            }
            column(name: 'RANGE_TYPE', type: 'VARCHAR(30)', remarks: '授权对象类型') {
                constraints(nullable: false)
            }
            column(name: 'RANGE_VALUE', type: 'BIGINT UNSIGNED', remarks: '授权对象') {
                constraints(nullable: false)
            }
            column(name: 'PERMISSION_ROLE_CODE', type: 'VARCHAR(30)', remarks: '授权角色') {
                constraints(nullable: false)
            }
            column(name: "OWNER_FLAG", type: "TINYINT(1)", defaultValue: "0", remarks: "所有者标识") {
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

        addUniqueConstraint(columnNames: "ORGANIZATION_ID,PROJECT_ID,TARGET_TYPE,TARGET_VALUE,RANGE_TYPE,RANGE_VALUE", tableName: "KB_PERMISSION_RANGE", constraintName: "KB_PERMISSION_RANGE_U1")
    }
}
