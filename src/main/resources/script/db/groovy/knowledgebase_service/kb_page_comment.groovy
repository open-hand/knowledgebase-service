package script.db.groovy.knowledgebase_service

databaseChangeLog(logicalFilePath: 'script/db/kb_page_comment.groovy') {
    changeSet(id: '2019-04-28-kb-page-comment', author: 'Zenger') {
        if (helper.dbType().isSupportSequence()) {
            createSequence(sequenceName: 'KB_PAGE_COMMENT_S', startValue: "1")
        }

        createTable(tableName: "KB_PAGE_COMMENT", remarks: '知识库页面评论表') {
            column(name: 'ID', type: 'BIGINT UNSIGNED', remarks: '主键', autoIncrement: true) {
                constraints(primaryKey: true, primaryKeyName: 'PK_KB_PAGE_COMMENT')
            }
            column(name: 'PAGE_ID', type: 'BIGINT UNSIGNED', remarks: '页面ID') {
                constraints(nullable: false)
            }
            column(name: 'COMMENT', type: 'LONGTEXT', remarks: '评论内容') {
                constraints(nullable: false)
            }

            column(name: "OBJECT_VERSION_NUMBER", type: "BIGINT", defaultValue: "1")
            column(name: "CREATED_BY", type: "BIGINT", defaultValue: "0")
            column(name: "CREATION_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "LAST_UPDATED_BY", type: "BIGINT", defaultValue: "0")
            column(name: "LAST_UPDATE_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
}