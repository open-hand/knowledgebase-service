package script.db.groovy.knowledgebase_service

databaseChangeLog(logicalFilePath: 'script/db/kb_workspace.groovy') {
    changeSet(id: '2019-04-28-kb-workspace', author: 'Zenger') {
        if (helper.dbType().isSupportSequence()) {
            createSequence(sequenceName: 'KB_WORKSPACE_S', startValue: "1")
        }

        createTable(tableName: "KB_WORKSPACE", remarks: '知识库工作空间') {
            column(name: 'ID', type: 'BIGINT UNSIGNED', remarks: '主键', autoIncrement: true) {
                constraints(primaryKey: true, primaryKeyName: 'PK_KB_WORKSPACE')
            }
            column(name: 'NAME', type: 'VARCHAR(255)', remarks: '工作空间名') {
                constraints(nullable: false)
            }
            column(name: 'ORGANIZATION_ID', type: 'BIGINT UNSIGNED', remarks: '组织ID')
            column(name: 'PROJECT_ID', type: 'BIGINT UNSIGNED', remarks: '项目ID')
            column(name: 'ROUTE', type: 'VARCHAR(255)', remarks: '路径')
            column(name: 'PARENT_ID', type: 'BIGINT UNSIGNED', remarks: '父亲ID', defaultValue: "0")
            column(name: 'RANK', type: 'VARCHAR(30)', remarks: '顺序') {
                constraints(nullable: false)
            }
            column(name: 'BOOK_ID', type: 'BIGINT UNSIGNED', remarks: '知识库Book ID')

            column(name: "OBJECT_VERSION_NUMBER", type: "BIGINT", defaultValue: "1")
            column(name: "CREATED_BY", type: "BIGINT", defaultValue: "0")
            column(name: "CREATION_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "LAST_UPDATED_BY", type: "BIGINT", defaultValue: "0")
            column(name: "LAST_UPDATE_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(id: '2019-10-29-add-column-is-delete', author: 'shinan.chenX@gmail.com') {
        addColumn(tableName: 'KB_WORKSPACE') {
            column(name: 'is_delete', type: 'TINYINT UNSIGNED(1)', remarks: '是否删除', defaultValue: "0") {
                constraints(nullable: false)
            }
        }
    }

    changeSet(id: '2019-12-31-add-column-base-id', author: 'zhaotianxin') {
        addColumn(tableName: 'KB_WORKSPACE') {
            column(name: 'base_id', type: 'BIGINT UNSIGNED', remarks: '所属的知识库Id')
        }
    }

    changeSet(id: '2019-12-31-add-column-description', author: 'zhaotianxin') {
        addColumn(tableName: 'KB_WORKSPACE') {
            column(name: 'description', type: 'text', remarks: '描述')
        }
    }

    changeSet(id: '2022-04-27-add-column-type', author: 'wx') {
        addColumn(tableName: 'KB_WORKSPACE') {
            column(name: 'type', type: 'VARCHAR(64)', remarks: '空间类型', afterColumn: 'NAME', defaultValue: "document")
            column(name: 'file_key', type: 'VARCHAR(255)', remarks: '文件key', afterColumn: 'NAME', defaultValue: "")
        }
    }

    changeSet(id: '2023-03-01-add-column-template', author: 'wx') {
        addColumn(tableName: 'KB_WORKSPACE') {
            column(name: 'TEMPLATE_CATEGORY', type: 'VARCHAR(255)', remarks: '模板分类的名称', afterColumn: 'TYPE', defaultValue: "")

            column(name: 'TEMPLATE_FLAG',type: 'TINYINT UNSIGNED(1)', remarks: '是否是模板', afterColumn: 'TYPE', defaultValue: "0"){
                constraints(nullable: false)
            }
        }
        sql("""
          UPDATE kb_workspace 
          SET TEMPLATE_CATEGORY = 'develop_manage',
          TEMPLATE_FLAG = 1 
          WHERE
            organization_id = 0 
            AND project_id = 0
        """)
    }

}
