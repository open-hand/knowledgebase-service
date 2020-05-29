package script.db.groovy.knowledgebase_service

/**
 * @author zhaotianxin* @since 2019/12/30
 */
databaseChangeLog(logicalFilePath: 'script/db/kb_knowledge_base.groovy') {
    changeSet(id: '2019-12-30-kb_knowledge_base', author: 'zhaotianxin') {
        createTable(tableName: "kb_knowledge_base", remarks: '知识库表') {
            column(name: 'ID', type: 'BIGINT UNSIGNED', remarks: '主键', autoIncrement: true) {
                constraints(primaryKey: true, primaryKeyName: 'PK_KB_PAGE')
            }
            column(name: 'NAME', type: 'VARCHAR(255)', remarks: '知识库名称') {
                constraints(nullable: false)
            }
            column(name: 'DESCRIPTION', type: 'text', remarks: '知识库描述')
            column(name: 'OPEN_RANGE',type: 'VARCHAR(255)', remarks: '公开范围')
            column(name: 'RANGE_PROJECT',type: 'VARCHAR(500)', remarks: '公开到项目')

            column(name: 'ORGANIZATION_ID', type: 'BIGINT UNSIGNED', remarks: '组织ID')
            column(name: 'PROJECT_ID', type: 'BIGINT UNSIGNED', remarks: '项目ID')
            column(name: 'is_delete', type: 'TINYINT UNSIGNED(1)', remarks: '是否删除', defaultValue: "0"){
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
