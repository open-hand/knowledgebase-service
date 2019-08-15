# 知识库服务
猪齿鱼知识管理是一种内容管理工具，具有创建、编辑、导航、链接、搜索等功能,可以与猪齿鱼其他服务结合共同来帮助企业做好协助管理工作。

## 特性
- `文档管理 `：可以快速操作文档，并支持文档上传附件、评论、记录操作日志等功能

- `版本管理 `：记录用户每次保存文档的版本记录，并支持版本回滚、比较、删除操作

- `树状文档结构 `：文档结构以树的形式进行展示，不限制层级，可以无限向下创建子级。通过树形的展示方式，可以查看你所在的项目下的所有文档，包括文档之间的父子关系

- `操作简单快速 `

## 应用场景

* `知识沉淀`——沉淀软件开发过程中的需求、设计、规范等知识文档。

* `项目协同`——有效管理项目中的计划安排，会议记录等，加强项目成员之间的合作。

* `产品文档`——便捷地编写软件产品的概念说明、用户手册、快速入门等产品文档。

* `培训教材`——方便地编写软件功能使用等培训材料，甚至视频教程等。

## 基础需求

* Java8
* [MySQL](https://www.mysql.com)

## 安装和启动

1.初始化数据库


```sql
CREATE USER 'choerodon'@'%' IDENTIFIED BY "choerodon";
CREATE DATABASE agile_service DEFAULT CHARACTER SET utf8;
GRANT ALL PRIVILEGES ON agile_service.* TO choerodon@'%';
FLUSH PRIVILEGES;
```


1. 运行命令 `sh init-local-database.sh`
2. 运行如下命令 或者 在 IntelliJ IDEA 中运行 `KnowledgeBaseServiceApplication`

``` bash
mvn clean spring-boot:run
```
## 服务依赖

* `go-register-server`: 注册中心
* `mysql`: 知识服务数据库
* `api-gateway`: 网关服务
* `oauth-server`: 权限认证中心
* `manager-service`: 配置及路由管理服务
* `file-service` : 文件服务
* `iam-service`：用户、角色、权限、组织、项目、密码策略、快速代码、客户端、菜单、图标、多语言等管理服务
## 报告问题
如果您发现任何缺陷或bug，请在[issue](https://github.com/choerodon/choerodon/issues/new?template=issue_template.md)中描述它们。

##如何贡献
访问[Follow](https://github.com/choerodon/choerodon/blob/master/CONTRIBUTING.md)了解更多关于如何贡献的信息。
