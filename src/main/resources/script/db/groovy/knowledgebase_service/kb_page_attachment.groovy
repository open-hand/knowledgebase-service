package script.db.groovy.knowledgebase_service

databaseChangeLog(logicalFilePath: 'script/db/kb_page_attachment.groovy') {
    changeSet(id: '2019-04-28-kb-page-attachment', author: 'Zenger') {
        if (helper.dbType().isSupportSequence()) {
            createSequence(sequenceName: 'KB_PAGE_ATTACHMENT_S', startValue: "1")
        }

        createTable(tableName: "KB_PAGE_ATTACHMENT", remarks: '知识库页面附件表') {
            column(name: 'ID', type: 'BIGINT UNSIGNED', remarks: '主键', autoIncrement: true) {
                constraints(primaryKey: true, primaryKeyName: 'PK_KB_PAGE_ATTACHMENT')
            }
            column(name: 'NAME', type: 'VARCHAR(255)', remarks: '名称') {
                constraints(nullable: false)
            }
            column(name: 'PAGE_ID', type: 'BIGINT UNSIGNED', remarks: '页面ID') {
                constraints(nullable: false)
            }
            column(name: 'SIZE', type: 'BIGINT UNSIGNED', remarks: '附件大小') {
                constraints(nullable: false)
            }
            column(name: 'URL', type: 'VARCHAR(255)', remarks: '附件路径') {
                constraints(nullable: false)
            }

            column(name: "OBJECT_VERSION_NUMBER", type: "BIGINT", defaultValue: "1")
            column(name: "CREATED_BY", type: "BIGINT", defaultValue: "0")
            column(name: "CREATION_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "LAST_UPDATED_BY", type: "BIGINT", defaultValue: "0")
            column(name: "LAST_UPDATE_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(id: '2019-06-05-modify-column', author: 'Zenger') {
        modifyDataType(tableName: 'KB_PAGE_ATTACHMENT', columnName: 'URL', newDataType: "VARCHAR(1000)")
    }
}