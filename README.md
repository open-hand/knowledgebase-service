# KnowledgeBase Service
 **`Knowledge Management`**  is a content management tool with the functions of creating, editing, navigation, linking and searching, which can be combined with pigtooth other services to help enterprises to do a good job in assisting management.

## Features

* **`Document Management `**: Documents can be quickly manipulated, and support documents upload attachments, comments, operation log and other functions

* **`Version Management `**: Record the version record saved by the user every time, and support version rollback, comparison and deletion

* **`Tree Document Structure `**: The document structure is presented in the form of a tree, with no limit on the hierarchy and unlimited creation of sub-levels. Through the tree presentation, you can view all the documents under your project, including the parent-child relationship between the documents

* **`Quick & easy maneuverability `**

## Application Scenarios

* **`Knowledge of precipitation`**——Precipitation software development process requirements, design, specifications and other knowledge documents.

* **`Project Cooperation`**——Effectively manage project scheduling, meeting minutes, etc., and enhance cooperation among project members.

* **`Product Documentation`**——Easy to write software product concept description, user manual, quick start product documentation.

* **`training material`**——Easy to write software function use and other training materials, even video tutorials.

## Requirements

* Java8
* [MySQL](https://www.mysql.com)

## Installation and Getting Started

1. init database

``` sql
CREATE USER 'choerodon'@'%' IDENTIFIED BY "choerodon";
CREATE DATABASE agile_service DEFAULT CHARACTER SET utf8;
GRANT ALL PRIVILEGES ON agile_service.* TO choerodon@'%';
FLUSH PRIVILEGES;
```

2. run command `sh init-local-database.sh`
3. run command as follow or run `KnowledgeBaseServiceApplication` in IntelliJ IDEA

``` bash
mvn clean spring-boot:run
```

## Dependencies

* `go-register-server`: Register server
* `iam-service`：iam service
* `mysql`: agile_service database
* `api-gateway`: api gateway server
* `oauth-server`: oauth server
* `manager-service`: manager service
* `file-service` : file service

## Reporting Issues

If you find any shortcomings or bugs, please describe them in the [issue](https://github.com/choerodon/choerodon/issues/new?template=issue_template.md).

## How to Contribute

Pull requests are welcome! [Follow](https://github.com/choerodon/choerodon/blob/master/CONTRIBUTING.md) to know for more information on how to contribute.