package script.db

databaseChangeLog(logicalFilePath: 'script/db/kb_workspace_page.groovy') {
    changeSet(id: '2019-04-28-kb-workspace-page', author: 'Zenger') {
        if (helper.dbType().isSupportSequence()) {
            createSequence(sequenceName: 'KB_WORKSPACE_PAGE_S', startValue: "1")
        }

        createTable(tableName: "KB_WORKSPACE_PAGE", remarks: '知识库工作空间与页面关联表') {
            column(name: 'ID', type: 'BIGINT UNSIGNED', remarks: '主键', autoIncrement: true) {
                constraints(primaryKey: true, primaryKeyName: 'PK_KB_WORKSPACE_PAGE')
            }
            column(name: 'WORKSPACE_ID', type: 'BIGINT UNSIGNED', remarks: '工作空间ID') {
                constraints(nullable: false)
            }
            column(name: 'PAGE_ID', type: 'BIGINT UNSIGNED', remarks: '页面ID')
            column(name: 'REFERENCE_TYPE', type: 'VARCHAR(20)', remarks: '引用类型') {
                constraints(nullable: false)
            }
            column(name: 'REFERENCE_URL', type: 'VARCHAR(255)', remarks: '引用路径')

            column(name: "OBJECT_VERSION_NUMBER", type: "BIGINT", defaultValue: "1")
            column(name: "CREATED_BY", type: "BIGINT", defaultValue: "0")
            column(name: "CREATION_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "LAST_UPDATED_BY", type: "BIGINT", defaultValue: "0")
            column(name: "LAST_UPDATE_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        addUniqueConstraint(tableName: 'KB_WORKSPACE_PAGE', constraintName: 'U_WP_WORKSPACE_ID', columnNames: 'WORKSPACE_ID')
    }
}