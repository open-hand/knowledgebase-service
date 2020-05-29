package script.db.groovy.knowledgebase_service

databaseChangeLog(logicalFilePath: 'script/db/kb_page_log.groovy') {
    changeSet(id: '2019-04-28-kb-page-log', author: 'Zenger') {
        if (helper.dbType().isSupportSequence()) {
            createSequence(sequenceName: 'KB_PAGE_LOG_S', startValue: "1")
        }

        createTable(tableName: "KB_PAGE_LOG") {
            column(name: 'ID', type: 'BIGINT UNSIGNED', autoIncrement: true, remarks: 'log id') {
                constraints(primaryKey: true, primaryKeyName: 'PK_KB_PAGE_LOG')
            }
            column(name: 'PAGE_ID', type: 'BIGINT UNSIGNED', remarks: '页面ID') {
                constraints(nullable: false)
            }
            column(name: 'OPERATION', type: 'VARCHAR(255)', remarks: '操作')
            column(name: 'FIELD', type: 'VARCHAR(255)', remarks: '领域')
            column(name: 'OLD_VALUE', type: 'TEXT', remarks: 'OLD VALUE')
            column(name: 'OLD_STRING', type: 'TEXT', remarks: 'OLD STRING')
            column(name: 'NEW_VALUE', type: 'TEXT', remarks: 'NEW VALUE')
            column(name: 'NEW_STRING', type: 'TEXT', remarks: 'NEW STRING')

            column(name: "OBJECT_VERSION_NUMBER", type: "BIGINT", defaultValue: "1")
            column(name: "CREATED_BY", type: "BIGINT", defaultValue: "0")
            column(name: "CREATION_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "LAST_UPDATED_BY", type: "BIGINT", defaultValue: "0")
            column(name: "LAST_UPDATE_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        createIndex(indexName: "IDX_LOG_PAGE_ID", tableName: "KB_PAGE_LOG",) {
            column(name: "PAGE_ID", type: "BIGINT UNSIGNED")
        }
        createIndex(indexName: "IDX_LOG_FIELD", tableName: "KB_PAGE_LOG",) {
            column(name: "FIELD", type: "VARCHAR(255)")
        }
    }
}