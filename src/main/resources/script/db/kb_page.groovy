package script.db

databaseChangeLog(logicalFilePath: 'script/db/kb_page.groovy') {
    changeSet(id: '2019-04-28-kb-page', author: 'Zenger') {
        if (helper.dbType().isSupportSequence()) {
            createSequence(sequenceName: 'KB_PAGE_S', startValue: "1")
        }

        createTable(tableName: "KB_PAGE", remarks: '知识库页面表') {
            column(name: 'ID', type: 'BIGINT UNSIGNED', remarks: '主键', autoIncrement: true) {
                constraints(primaryKey: true, primaryKeyName: 'PK_KB_PAGE')
            }
            column(name: 'TITLE', type: 'VARCHAR(255)', remarks: '标题') {
                constraints(nullable: false)
            }
            column(name: 'LATEST_VERSION_ID', type: 'BIGINT UNSIGNED', remarks: '最新的页面版本ID') {
                constraints(nullable: false)
            }
            column(name: 'ORGANIZATION_ID', type: 'BIGINT UNSIGNED', remarks: '组织ID')
            column(name: 'PROJECT_ID', type: 'BIGINT UNSIGNED', remarks: '项目ID')

            column(name: "OBJECT_VERSION_NUMBER", type: "BIGINT", defaultValue: "1")
            column(name: "CREATED_BY", type: "BIGINT", defaultValue: "0")
            column(name: "CREATION_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "LAST_UPDATED_BY", type: "BIGINT", defaultValue: "0")
            column(name: "LAST_UPDATE_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
}