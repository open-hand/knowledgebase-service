package script.db.groovy.knowledgebase_service

databaseChangeLog(logicalFilePath: 'script/db/kb_page_content.groovy') {
    changeSet(id: '2019-04-28-kb-page-content', author: 'Zenger') {
        if (helper.dbType().isSupportSequence()) {
            createSequence(sequenceName: 'KB_PAGE_CONTENT_S', startValue: "1")
        }

        createTable(tableName: "KB_PAGE_CONTENT", remarks: '知识库页面版本内容表') {
            column(name: 'ID', type: 'BIGINT UNSIGNED', remarks: '主键', autoIncrement: true) {
                constraints(primaryKey: true, primaryKeyName: 'PK_KB_PAGE_CONTENT')
            }
            column(name: 'VERSION_ID', type: 'BIGINT UNSIGNED', remarks: '版本id') {
                constraints(nullable: false)
            }
            column(name: 'PAGE_ID', type: 'BIGINT UNSIGNED', remarks: '页面ID') {
                constraints(nullable: false)
            }
            column(name: 'CONTENT', type: 'LONGTEXT', remarks: '源码内容')
            column(name: 'DRAW_CONTENT', type: 'LONGTEXT', remarks: '内容')

            column(name: "OBJECT_VERSION_NUMBER", type: "BIGINT", defaultValue: "1")
            column(name: "CREATED_BY", type: "BIGINT", defaultValue: "0")
            column(name: "CREATION_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "LAST_UPDATED_BY", type: "BIGINT", defaultValue: "0")
            column(name: "LAST_UPDATE_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(tableName: "KB_PAGE_CONTENT", indexName: "idx_page_content_page_id") {
            column(name: "PAGE_ID", type: "BIGINT UNSIGNED")
        }
        addUniqueConstraint(tableName: 'KB_PAGE_CONTENT', constraintName: 'U_VERSION_ID', columnNames: 'VERSION_ID')
    }

    changeSet(id: '2019-06-26-kb-drop-constraint', author: 'shinan.chenX@gmail.com') {
        dropUniqueConstraint(tableName: 'KB_PAGE_CONTENT', constraintName: 'U_VERSION_ID')
    }

    changeSet(id: '2019-09-16-kb-modify-table', author: 'shinan.chenX@gmail.com') {
        dropColumn(columnName: "DRAW_CONTENT", tableName: "KB_PAGE_CONTENT")
        addColumn(tableName: 'KB_PAGE_CONTENT') {
            column(name: 'TITLE', type: 'VARCHAR(255)', remarks: '标题', afterColumn: 'CONTENT')
        }
        sql(stripComments: true, splitStatements: false, endDelimiter: ';') {
            "update kb_page_content pc set pc.TITLE = (select p.TITLE from kb_page p where p.id = pc.PAGE_ID);"
        }
    }

    changeSet(id: '2019-10-21-add-index-version-id', author: 'shinan.chenX@gmail.com') {
        createIndex(tableName: "KB_PAGE_CONTENT", indexName: "idx_page_content_version_id") {
            column(name: "VERSION_ID")
        }
    }
}
