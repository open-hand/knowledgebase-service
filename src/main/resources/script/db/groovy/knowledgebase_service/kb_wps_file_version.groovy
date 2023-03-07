package script.db

databaseChangeLog(logicalFilePath: 'script/db/kb_wps_file_version.groovy') {
    changeSet(author: "wx@wx.com", id: "2022-05-24-kb_wps_file_version") {
        createTable(tableName: "kb_wps_file_version", remarks: "文档的多版本表") {
            column(name: "id", type: "bigint", autoIncrement: true, remarks: "id") {
                constraints(primaryKey: true)
            }
            column(name: "file_id", type: "varchar(32)", remarks: "文件的file_id") {
                constraints(nullable: "false")
            }
            column(name: "name", type: "varchar(100)", remarks: "名称") {
                constraints(nullable: "false")
            }
            column(name: "version", type: "int", remarks: "文档发布的版本号"){
                constraints(nullable: "false")
            }
            column(name: "file_size", type: "bigint", remarks: "文件大小")
            column(name: "file_key", type: "varchar(240)", remarks: "对象KEY") { constraints(nullable: "false") }
            column(name: "md5", type: "varchar(60)", remarks: "文件MD5")
            column(name: "file_url", type: "varchar(480)", remarks: "文件地址") { constraints(nullable: "false") }
            

            // 以下是标准字段
            column(name: "tenant_id", type: "bigint", defaultValue: "0", remarks: "租户id") {
                constraints(nullable: "false")
            }
            column(name: "object_version_number", type: "bigint", defaultValue: "1", remarks: "版本锁") {
                constraints(nullable: "false")
            }
            column(name: "created_by", type: "bigint", defaultValue: "-1", remarks: "") {
                constraints(nullable: "false")
            }
            column(name: "creation_date", type: "datetime", defaultValueComputed: "CURRENT_TIMESTAMP", remarks: "") {
                constraints(nullable: "false")
            }
            column(name: "last_updated_by", type: "bigint", defaultValue: "-1", remarks: "") {
                constraints(nullable: "false")
            }
            column(name: "last_update_date", type: "datetime", defaultValueComputed: "CURRENT_TIMESTAMP", remarks: "") {
                constraints(nullable: "false")
            }
            column(name: 'domain_id', type: 'bigint', remarks: '域ID', defaultValue: "0") {
                constraints(nullable: false)
            }
        }
    }

    changeSet(id: '2023-03-07-del-wps-column', author: 'wx') {
       sql("""
            ALTER TABLE kb_wps_file_version  DROP file_url
       """)
    }
}