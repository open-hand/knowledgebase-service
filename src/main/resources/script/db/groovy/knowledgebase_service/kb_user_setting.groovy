package script.db.groovy.knowledgebase_service

databaseChangeLog(logicalFilePath: 'script/db/kb_user_setting.groovy') {
    changeSet(id: '2019-07-02-kb-user-setting', author: 'fuqianghuang01@gmail.com') {
        if (helper.dbType().isSupportSequence()) {
            createSequence(sequenceName: 'KB_USER_SETTING_S', startValue: "1")
        }

        createTable(tableName: "KB_USER_SETTING", remarks: '个人设置') {
            column(name: 'ID', type: 'BIGINT UNSIGNED', remarks: '主键', autoIncrement: true) {
                constraints(primaryKey: true, primaryKeyName: 'PK_KB_USER_SETTING')
            }
            column(name: 'ORGANIZATION_ID', type: 'BIGINT UNSIGNED', remarks: '组织ID')
            column(name: 'PROJECT_ID', type: 'BIGINT UNSIGNED', remarks: '项目ID')
            column(name: 'TYPE', type: 'VARCHAR(255)', remarks: '设置类型') {
                constraints(nullable: false)
            }
            column(name: 'USER_ID', type: 'BIGINT UNSIGNED', remarks: '用户ID') {
                constraints(nullable: false)
            }
            column(name: 'EDIT_MODE', type: 'VARCHAR(255)', remarks: '编辑模式')

            column(name: "OBJECT_VERSION_NUMBER", type: "BIGINT", defaultValue: "1")
            column(name: "CREATED_BY", type: "BIGINT", defaultValue: "0")
            column(name: "CREATION_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "LAST_UPDATED_BY", type: "BIGINT", defaultValue: "0")
            column(name: "LAST_UPDATE_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
}