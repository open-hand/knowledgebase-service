package script.db.groovy.knowledgebase_service

databaseChangeLog(logicalFilePath: 'script/db/kb_workspace_share.groovy') {
    changeSet(id: '2019-04-28-kb-workspace-share', author: 'Zenger') {
        if (helper.dbType().isSupportSequence()) {
            createSequence(sequenceName: 'KB_WORKSPACE_SHARE_S', startValue: "1")
        }

        createTable(tableName: "KB_WORKSPACE_SHARE", remarks: '知识库工作空间分享表') {
            column(name: 'ID', type: 'BIGINT UNSIGNED', remarks: '主键', autoIncrement: true) {
                constraints(primaryKey: true, primaryKeyName: 'PK_KB_WORKSPACE_SHARE')
            }
            column(name: 'WORKSPACE_ID', type: 'BIGINT UNSIGNED', remarks: '工作空间ID') {
                constraints(nullable: false)
            }
            column(name: 'url', type: 'VARCHAR(255)', remarks: '分享地址') {
                constraints(nullable: false)
            }
            column(name: 'token', type: 'VARCHAR(255)', remarks: 'token') {
                constraints(nullable: false)
            }
            column(name: 'share_type', type: 'VARCHAR(30)', remarks: '分享类型') {
                constraints(nullable: false)
            }

            column(name: "OBJECT_VERSION_NUMBER", type: "BIGINT", defaultValue: "1")
            column(name: "CREATED_BY", type: "BIGINT", defaultValue: "0")
            column(name: "CREATION_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "LAST_UPDATED_BY", type: "BIGINT", defaultValue: "0")
            column(name: "LAST_UPDATE_DATE", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(id: '2019-06-10-modify-column', author: 'Zenger') {
        dropColumn(columnName: "url", tableName: "KB_WORKSPACE_SHARE")
        dropColumn(columnName: "share_type", tableName: "KB_WORKSPACE_SHARE")
        dropColumn(columnName: "token", tableName: "KB_WORKSPACE_SHARE")

        addColumn(tableName: 'KB_WORKSPACE_SHARE') {
            column(name: 'TOKEN', type: 'VARCHAR(255)', remarks: 'token', afterColumn: 'WORKSPACE_ID') {
                constraints(nullable: false)
            }
            column(name: 'IS_CONTAIN', type: 'TINYINT UNSIGNED', remarks: 'token', afterColumn: 'TOKEN') {
                constraints(nullable: false)
            }
        }

        addUniqueConstraint(tableName: 'KB_WORKSPACE_SHARE', constraintName: 'U_WS_WORKSPACE_ID', columnNames: 'WORKSPACE_ID')
        addUniqueConstraint(tableName: 'KB_WORKSPACE_SHARE', constraintName: 'U_WS_TOKEN', columnNames: 'TOKEN')
    }

    changeSet(id: '2019-06-12-modify-column', author: 'Zenger') {
        dropColumn(columnName: "IS_CONTAIN", tableName: "KB_WORKSPACE_SHARE")

        addColumn(tableName: 'KB_WORKSPACE_SHARE') {
            column(name: 'TYPE', type: 'VARCHAR(255)', remarks: '分享类型', afterColumn: 'TOKEN') {
                constraints(nullable: false)
            }
        }
    }
}