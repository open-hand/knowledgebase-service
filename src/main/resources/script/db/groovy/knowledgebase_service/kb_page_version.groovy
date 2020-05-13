package script.db.groovy.knowledgebase_service

databaseChangeLog(logicalFilePath: 'script/db/kb_page_version.groovy') {
    changeSet(id: '2019-04-28-kb-page-version', author: 'Zenger') {
        if (helper.dbType().isSupportSequence()) {
            createSequence(sequenceName: 'KB_PAGE_VERSION_S', startValue: "1")
        }

        createTable(tableName: "KB_PAGE_VERSION", remarks: '知识库页面版本表') {
            column(name: 'ID', type: 'BIGINT UNSIGNED', remarks: '主键', autoIncrement: true) {
                constraints(primaryKey: true, primaryKeyName: 'PK_KB_PAGE_VERSION')
            }
            column(name: 'NAME', type: 'VARCHAR(30)', remarks: '版本名称') {
                constraints(nullable: false)
            }
            column(name: 'PAGE_ID', type: 'BIGINT UNSIGNED', remarks: '页面ID') {
                constraints(nullable: false)
            }

            column(name: "OBJECT_VERSION_NUMBER", type: "BIGINT", defaultValue: "1")
            column(name: "CREATED_BY", type: "BIGINT", defaultValue: "0")
            column(name: "CREATION_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "LAST_UPDATED_BY", type: "BIGINT", defaultValue: "0")
            column(name: "LAST_UPDATE_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(tableName: "KB_PAGE_VERSION", indexName: "idx_page_version_page_id") {
            column(name: "PAGE_ID", type: "BIGINT UNSIGNED")
        }
    }
}